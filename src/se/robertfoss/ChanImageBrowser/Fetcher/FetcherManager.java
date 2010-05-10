package se.robertfoss.ChanImageBrowser.Fetcher;

import java.io.File;
import java.util.ArrayList;

import se.robertfoss.ChanImageBrowser.Viewer;
import se.robertfoss.ChanImageBrowser.Target.TargetUrl;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;

public class FetcherManager extends AsyncTask<String, Void, Void> {

	@SuppressWarnings("unused")
	private TargetUrl imageTarget;
	@SuppressWarnings("unused")
	private TargetUrl linkTarget;

	private ProgressDialog dialog;
	private ArrayList<String> visitedUrls;
	private ArrayList<String> imageUrlList;
	private ArrayList<String> linkUrlList;

	private IndexFetcher indexfetcher;
	private ArrayList<ImageFetcher> imagefetchers;
	private ArrayList<ThreadFetcher> threadfetchers;

	private final int MAX_IMAGEFETCHERS = 2;
	private final int MAX_THREADFETCHERS = 1;
	private Viewer parent;
	private int imagesToDownload;

	/**
	 * 
	 * @param view
	 *            - Parent view
	 * @param runType
	 *            - Type of run, first or any later run
	 * @param imagesToDownload
	 *            - The number of images to download
	 * @param linkTarget
	 *            - Urlpackage for the links
	 * @param imageTarget
	 *            - Urlpackage for the images
	 */
	public FetcherManager(Viewer view, int imagesToDownload,
			TargetUrl linkTarget, TargetUrl imageTarget) {
		Viewer.printDebug("Creating Fetchers \n");
		parent = view;
		visitedUrls = new ArrayList<String>();
		imageUrlList = new ArrayList<String>();
		linkUrlList = new ArrayList<String>();
		imagefetchers = new ArrayList<ImageFetcher>();
		threadfetchers = new ArrayList<ThreadFetcher>();
		dialog = new ProgressDialog(view);

		this.imageTarget = imageTarget;
		this.linkTarget = linkTarget;
		this.imagesToDownload = imagesToDownload;

		indexfetcher = new IndexFetcher(this, linkTarget, imageTarget);

		for (int i = 0; i < MAX_IMAGEFETCHERS; i++) {
			Viewer.printDebug("ImageFetcher-" + i + " created");
			ImageFetcher temp = new ImageFetcher(this);
			imagefetchers.add(temp);
		}

		for (int i = 0; i < MAX_THREADFETCHERS; i++) {
			Viewer.printDebug("ThreadFetcher-" + i + " created");
			ThreadFetcher temp = new ThreadFetcher(this, imageTarget);
			threadfetchers.add(temp);
		}
	}

	@Override
	protected void onPreExecute() {
		setDialogMessage("Fetching index..");
		enableDialog();
	}

	@Override
	public Void doInBackground(String... params) {

		indexfetcher.start();
		for (int i = 0; i < MAX_THREADFETCHERS; i++) {
			Viewer.printDebug("ThreadFetcher-" + i + " resumed");
			threadfetchers.get(i).start();
		}
		for (int i = 0; i < MAX_IMAGEFETCHERS; i++) {
			Viewer.printDebug("ImageFetcher-" + i + " resumed");
			imagefetchers.get(i).start();
		}

		// Wait until images can be downloaded
		while (imageUrlList.size() == 0) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		destroyDialog();

		return null;
	}

	@Override
	protected void onPostExecute(Void param) {
		destroyDialog();
	}

	public synchronized void destroyFetchers() {
		for (int i = 0; i < imagefetchers.size(); i++) {
			imagefetchers.get(i).done();
			imagefetchers.remove(i);

			Viewer.printDebug("ImageFetcher-" + i + " destroyed");
		}
		for (int i = 0; i < threadfetchers.size(); i++) {
			threadfetchers.get(i).done();
			threadfetchers.remove(i);
			Viewer.printDebug("ThreadFetcher-" + i + " destroyed");
		}
		indexfetcher.done();
		indexfetcher = null;
	}

	public synchronized void addCompleteImage(File file) {
		Viewer.printDebug("Saving complete Image \n");
		final File temp = file;

		// Test if the file is a legitimate image and then add
		Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		if (BitmapFactory.decodeFile(file.toString(), options) == null) {
			imagesToDownload -= 1;
			parent.runOnUiThread(new Runnable() {
				public void run() {
					parent.addCompleteImage(temp);
				}
			});
		}
	}

	public synchronized String getNextImageName() {
		if (imageUrlList.size() != 0) {
			String temp = imageUrlList.get(imageUrlList.size() - 1);
			Viewer.printDebug("Delivered next image-url " + temp);
			visitedUrls.add(temp);
			imageUrlList.remove(imageUrlList.size() - 1);
			return temp;
		}
		return null;
	}

	public synchronized String getNextUrl() {
		if (linkUrlList.size() != 0) {

			String temp = linkUrlList.get(linkUrlList.size() - 1);
			Viewer.printDebug("Delivered next link-url " + temp);
			visitedUrls.add(temp);
			linkUrlList.remove(linkUrlList.size() - 1);
			Viewer.printDebug("Link-urls left: " + linkUrlList.size());
			return temp;
		}
		return null;
	}

	public synchronized void addImageUrl(String url) {
		if (!imageUrlList.contains(url) && !visitedUrls.contains(url)) {
			System.out.println("Added image-url  -  " + url);
			imageUrlList.add(url);
		} else {
			Viewer.printDebug("		Couldn't image-add url - " + url);
		}
	}

	public synchronized void addLinkUrl(String url) {
		if (!linkUrlList.contains(url) && !visitedUrls.contains(url)) {
			System.out.println("Added link-url  -  " + url);
			linkUrlList.add(url);
		} else {
			Viewer.printDebug("		Couldn't add link-url - " + url);
		}
	}

	private void enableDialog() {
		parent.runOnUiThread(new Runnable() {
			public void run() {
				dialog.show();
			}
		});
	}

	private void destroyDialog() {
		parent.runOnUiThread(new Runnable() {
			public void run() {
				dialog.dismiss();
			}
		});
	}

	private void setDialogMessage(final String str) {
		parent.runOnUiThread(new Runnable() {
			public void run() {
				dialog.setMessage(str);
			}
		});
	}

	public void downloadXAdditionalImages(int number) {
		imagesToDownload += number;
	}

	public int getNumberOfImageToDownload() {
		return imagesToDownload;
	}

	public int getNbrImagesToDownload() {
		return imagesToDownload;
	}

	public int getNbrImageLinks() {
		return imageUrlList.size();
	}

	public int getNbrUrlLinks() {
		return linkUrlList.size();
	}

}
