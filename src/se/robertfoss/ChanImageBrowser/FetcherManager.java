package se.robertfoss.ChanImageBrowser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public class FetcherManager extends AsyncTask<String, Void, Void> {

	private ProgressDialog dialog;
	private ArrayList<String> visitedUrls;
	private ArrayList<String> imageUrlList;
	private ArrayList<String> linkUrlList;
	private ArrayList<Fetcher> fetchers;
	private final int NUMBER_OF_FETCHERS = 3;
	private Viewer parent;

	FetcherManager(Viewer view, ProgressDialog dialog) {
		Viewer.printDebug("Creating Fetchers \n");
		parent = view;
		visitedUrls = new ArrayList<String>();
		imageUrlList = new ArrayList<String>();
		linkUrlList = new ArrayList<String>();
		fetchers = new ArrayList<Fetcher>();
		this.dialog = dialog;

		for (int i = 0; i < NUMBER_OF_FETCHERS; i++) {
			Viewer.printDebug("Fetcher-" + i + " created");
			Fetcher temp = new Fetcher(this);
			fetchers.add(temp);
		}
	}

	
	@Override
	protected void onPreExecute() {
		dialog.setMessage("Fetching index...");
		dialog.show();
	}

	@Override
	public Void doInBackground(String... params) {
		

		final String url = params[0];
		String regex = params[1];
		String moreUrlsRegex = params[2];
		
		ArrayList<String> imageList = new ArrayList<String>();
		
		try {
			imageList =  Parser.parseForStrings(url, regex);
		} catch (IOException e) {
			parent.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(parent, "Error: Couldn't download " + url + "\n Exiting...", Toast.LENGTH_LONG);
					parent.finish();
				}
			});
			e.printStackTrace();
		}
		
		
		for (String s : imageList){
			addImageUrl(s);
		}


		for (int i = 0; i < NUMBER_OF_FETCHERS; i++) {
			Viewer.printDebug("Fetcher-" + i + " started");
			fetchers.get(i).start();
		}
		Viewer.printDebug("All fetchers started, returning.");
		return null;
	}

	@Override
	protected void onPostExecute(Void param) {
		dialog.dismiss();
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
			Viewer.printDebug("Delivered next url -  " + temp);
			visitedUrls.add(temp);
			Viewer.printDebug("Trying to remove url-" + imageUrlList.size()
					+ "  from urlList");
			imageUrlList.remove(imageUrlList.size() - 1);
			return temp;
		}
		return null;
	}

	public void addImageUrl(String url) {
		if (!imageUrlList.contains(url) && !visitedUrls.contains(url)) {
			System.out.println("Added url  -  " + url);
			imageUrlList.add(url);
		} else {
			Viewer.printDebug("		Couldn't add url - " + url);
		}
	}
	
	public void addLinkUrl(String url) {
		if (!imageUrlList.contains(url) && !visitedUrls.contains(url)) {
			System.out.println("Added url  -  " + url);
			linkUrlList.add(url);
		} else {
			Viewer.printDebug("		Couldn't add url - " + url);
		}
	}

}
