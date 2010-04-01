package se.robertfoss.ChanImageBrowser;

import java.io.File;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class FetcherManager extends AsyncTask<Void, Void, Void> {

	private ProgressDialog dialog;
	private IndexFetcher indexFetcher;
	private ArrayList<String> visitedUrls;
	private ArrayList<String> urlList;
	private ArrayList<Fetcher> fetchers;
	private final int NUMBER_OF_FETCHERS = 3;
	private Viewer parent;

	FetcherManager(Viewer view) {
		Viewer.printDebug("Creating Fetchers \n");
		parent = view;
		visitedUrls = new ArrayList<String>();
		urlList = new ArrayList<String>();
		indexFetcher = new IndexFetcher(this);
		fetchers = new ArrayList<Fetcher>();
		dialog = new ProgressDialog(parent);

		for (int i = 0; i < NUMBER_OF_FETCHERS; i++) {
			Viewer.printDebug("Fetcher-" + i + " created");
			Fetcher temp = new Fetcher(this);
			fetchers.add(temp);
		}
	}

	
	// can use UI thread here
	
	protected void onPreExecute() {
		this.dialog.setMessage("Fetching index...");
		this.dialog.show();
	}

	public Void doInBackground(Void... params) {
		indexFetcher.run();

		parent.runOnUiThread(new Runnable() {
			public void run() {
				dialog.hide();
			}
		});

		for (int i = 0; i < NUMBER_OF_FETCHERS; i++) {
			Viewer.printDebug("		Fetcher-" + i + " started");
			fetchers.get(i).execute();
		}
		return null;
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
		if (urlList.size() != 0) {

			String temp = urlList.get(urlList.size() - 1);
			Viewer.printDebug("Delivered next url -  " + temp);
			visitedUrls.add(temp);
			Viewer.printDebug("Trying to remove url-" + urlList.size()
					+ "  from urlList");
			urlList.remove(urlList.size() - 1);
			return temp;
		}
		return null;
	}

	public synchronized void addUrl(String url) {
		Viewer.printDebug("Trying to add url");
		if (!urlList.contains(url) && !visitedUrls.contains(url)) {
			System.out.println("Added url  -  " + url);
			urlList.add(url);
		}
	}

}
