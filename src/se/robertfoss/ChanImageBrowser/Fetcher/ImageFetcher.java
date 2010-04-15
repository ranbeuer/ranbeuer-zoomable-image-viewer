package se.robertfoss.ChanImageBrowser.Fetcher;

import java.io.File;

import android.widget.Toast;

import se.robertfoss.ChanImageBrowser.Viewer;

public class ImageFetcher extends Thread {

	private FetcherManager manager;
	private boolean isDone;
	private final static int THREAD_SLEEP_TIME = 5000;


	ImageFetcher(FetcherManager manager) {
		this.manager = manager;
		isDone = false;
	}

	public void done() {
		isDone = true;
	}

	public void run() {
		isDone = false;
		String inputUrl;
		while (!isDone) {
			while (manager.getNbrImagesToDownload() > 0 && 
					(inputUrl = manager.getNextImageName()) != null) {

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
				yield();
			}
			
			// Wait for new data
			try {
				Thread.sleep(THREAD_SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			yield();
		}
	}
}
