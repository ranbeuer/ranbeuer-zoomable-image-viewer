package se.robertfoss.ChanImageBrowser.Fetcher;

import java.io.File;
import java.util.ArrayList;

import se.robertfoss.ChanImageBrowser.Viewer;
import se.robertfoss.ChanImageBrowser.Target.TargetUrl;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public class FetcherManager extends AsyncTask<String, Void, Void> {

	public final static int FIRST_RUN = 0;
	public final static int LATER_RUN = 1;
	
	private TargetUrl imageTarget;
	private TargetUrl linkTarget;
	
	private ProgressDialog dialog;
	private ArrayList<String> visitedUrls;
	private ArrayList<String> imageUrlList;
	private ArrayList<String> linkUrlList;
	
	private IndexFetcher indexfetcher;
	private ArrayList<ImageFetcher> imagefetchers;
	private ArrayList<ThreadFetcher> threadfetchers;
	
	private final int MAX_IMAGEFETCHERS = 3;
	private final int MAX_THREADFETCHERS = 1;
	private int currentRun;
	private Viewer parent;

	/**
	 * 
	 * @param view - Parent view
	 * @param runType - Type of run, first or any later run
	 * @param linkTarget - Urlpackage for the links
	 * @param imageTarget - Urlpackage for the images
	 */
	public FetcherManager(Viewer view, int runType, TargetUrl linkTarget, TargetUrl imageTarget) {
		Viewer.printDebug("Creating Fetchers \n");
		parent = view;
		visitedUrls = new ArrayList<String>();
		imageUrlList = new ArrayList<String>();
		linkUrlList = new ArrayList<String>();
		imagefetchers = new ArrayList<ImageFetcher>();
		threadfetchers = new ArrayList<ThreadFetcher>();
		dialog = new ProgressDialog(view);
		
		currentRun = runType;
		this.imageTarget = imageTarget;
		this.linkTarget = linkTarget;
		
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
	
		indexfetcher.run();
		for (int i = 0; i < MAX_THREADFETCHERS; i++) {
			Viewer.printDebug("ThreadFetcher-" + i + " resumed");
			threadfetchers.get(i).start();
		}
		for (int i = 0; i < MAX_IMAGEFETCHERS; i++) {
			Viewer.printDebug("ImageFetcher-" + i + " resumed");
			imagefetchers.get(i).start();
		}
		
		
		return null;
	}

	@Override
	protected void onPostExecute(Void param) {
		destroyDialog();
	}
	
	public void pause(){
		Viewer.printDebug("Threads owned by manager, PAUSED");
		for (ImageFetcher f : imagefetchers){
			f.done();
		}
		for (ThreadFetcher f : threadfetchers){
			f.done();
		}
	}
	
	public void resume(){
		Viewer.printDebug("Resuming threads owned by manager");
		for (int i = 0; i < MAX_IMAGEFETCHERS; i++) {
			Viewer.printDebug("ImageFetcher-" + i + " resumed");
			imagefetchers.get(i).moreWork();
		}
		
//		for (int i = 0; i < MAX_THREADFETCHERS; i++) {
//			Viewer.printDebug("ThreadFetcher-" + i + " resumed");
//			threadfetchers.get(i).start();
//		}
	}
	
	public synchronized void resumeImageFetchers(){
		for (int i = 0; i < MAX_IMAGEFETCHERS; i++) {
			Viewer.printDebug("ImageFetcher-" + i + " resumed");
			imagefetchers.get(i).moreWork();
		}
	}
	
	public void pauseImageFetchers(){
		Viewer.printDebug("ImageFetchers paused");
		for (ImageFetcher f : imagefetchers){
			f.done();
		}
	}
	
//	public synchronized void resumeThreadFetchers(){
//		for (int i = 0; i < MAX_THREADFETCHERS; i++) {
//			Viewer.printDebug("ThreadFetcher-" + i + " resumed");
//			threadfetchers.get(i).start();
//		}
//	}
	
	public void pauseThreadFetchers(){
		Viewer.printDebug("ThreadFetchers paused");
		for (ThreadFetcher f : threadfetchers){
			f.done();
		}
	}
	
	public synchronized void destroyFetchers(){
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
		indexfetcher = null;
	}
	
	public synchronized void addCompleteImage(File file) {
		Viewer.printDebug("Saving complete Image \n");
		final File temp = file;
		parent.runOnUiThread(new Runnable() {
			public void run() {
				parent.addCompleteImage(temp);
			}
		});
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
	
	public synchronized void toastInUI(final String str, final int duration){
		parent.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(parent, str, Toast.LENGTH_SHORT);
			}
		});
	}
	
	private void enableDialog(){
		if (currentRun == FIRST_RUN){
			parent.runOnUiThread(new Runnable() {
				public void run() {
					dialog.show();
				}
			});
			
		}
	}
	
	private void destroyDialog(){
		if (currentRun == FIRST_RUN){
			parent.runOnUiThread(new Runnable() {
				public void run() {
					dialog.dismiss();
				}
			});
		}
	}
	
	private void setDialogMessage(final String str){
		if (currentRun == FIRST_RUN){
			parent.runOnUiThread(new Runnable() {
				public void run() {
					dialog.setMessage(str);
				}
			});
		} else {
			toastInUI("str", Toast.LENGTH_SHORT);
		}
	}

}
