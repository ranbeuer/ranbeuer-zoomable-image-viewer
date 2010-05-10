package se.robertfoss.ChanImageBrowser.Fetcher;

import java.io.File;

import se.robertfoss.ChanImageBrowser.NetworkData;
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
				System.out.println("Images left to download: " + manager.getNbrImagesToDownload());
				Viewer.printDebug("Fetching picture  -  " + inputUrl);
				String[] fileName = inputUrl.split("/");

				File pic = null;
				try {
					pic = NetworkData.getFileFromUrl(inputUrl,
							fileName[fileName.length - 1]);
				} catch (Exception e) {
					Viewer.printDebug(" 	Unable to fetch image - " + inputUrl);
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
				try {
					yield();
					Thread.sleep(125);
				} catch (InterruptedException e) {}
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
