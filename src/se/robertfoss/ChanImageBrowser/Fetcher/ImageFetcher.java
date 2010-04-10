package se.robertfoss.ChanImageBrowser.Fetcher;

import java.io.File;

import android.widget.Toast;

import se.robertfoss.ChanImageBrowser.Viewer;

public class ImageFetcher extends Thread {

	private FetcherManager manager;
	private boolean isDone;
	private final static int threadSleeptime = 5000;
	private int counter;
	private final static int TTL = 60000;

	ImageFetcher(FetcherManager manager) {
		this.manager = manager;
		isDone = false;
		counter = 0;
	}

	public void done() {
		isDone = true;
	}
	public void moreWork() {
		isDone = false;
	}

	public void run() {
		isDone = false;
		String inputUrl;
		while (true && TTL > counter) {
			while (!isDone && (inputUrl = manager.getNextImageName()) != null) {
				counter = 0;

				Viewer.printDebug("Fetching picture  -  " + inputUrl);
				String[] fileName = inputUrl.split("/");

				File pic = null;
				try {
					pic = Viewer.getFileFromUrl(inputUrl,
							fileName[fileName.length - 1]);
				} catch (Exception e) {
					Viewer.printDebug(" 	Unable to fetch image - " + inputUrl);
					manager.toastInUI("Unable to fetch index - " + inputUrl,
							Toast.LENGTH_LONG);
					e.printStackTrace();
				}

				Viewer
						.printDebug("ImageFetcher is adding downloaded a picture");
				if (pic != null) {
					manager.addCompleteImage(pic);
				} else {
					Viewer.printDebug("	" + inputUrl
							+ " could'nt be parsed into a Bitmap");
				}
				inputUrl = manager.getNextImageName();
			}
			
			// Wait for new data
			try {
				Viewer.printDebug("An ImageFetcher is sleeping, time left to live: " + (TTL - counter));
				counter += threadSleeptime;
				Thread.sleep(threadSleeptime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
