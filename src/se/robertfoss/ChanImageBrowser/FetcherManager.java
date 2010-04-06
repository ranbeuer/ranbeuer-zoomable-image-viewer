package se.robertfoss.ChanImageBrowser;

import java.io.File;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class FetcherManager extends AsyncTask<Void, Void, Void> {

	private ProgressDialog dialog;
	private PicIndexer indexFetcher;
	private ArrayList<String> visitedUrls;
	private ArrayList<String> urlList;
	private ArrayList<Fetcher> fetchers;
	private final int NUMBER_OF_FETCHERS = 3;
	private Viewer parent;

	FetcherManager(Viewer view, ProgressDialog dialog) {
		Viewer.printDebug("Creating Fetchers \n");
		parent = view;
		visitedUrls = new ArrayList<String>();
		urlList = new ArrayList<String>();
		indexFetcher = new PicIndexer(this);
		fetchers = new ArrayList<Fetcher>();
		this.dialog = dialog;

		for (int i = 0; i < NUMBER_OF_FETCHERS; i++) {
			Viewer.printDebug("Fetcher-" + i + " created");
			Fetcher temp = new Fetcher(this);
			fetchers.add(temp);
		}
	}

	
	// can use UI thread here
	
	protected void onPreExecute() {
		dialog.setMessage("Fetching index...");
		dialog.show();
	}

	public Void doInBackground(Void... params) {
		indexFetcher.run();

		parent.runOnUiThread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dialog.hide();
			}
		});

		for (int i = 0; i < NUMBER_OF_FETCHERS; i++) {
			Viewer.printDebug("		Fetcher-" + i + " started");
			fetchers.get(i).start();
		}
		Viewer.printDebug("		All fetchers started, returning.");
		return null;
	}
	
	protected void onPostExecute() {
		dialog.hide();
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
