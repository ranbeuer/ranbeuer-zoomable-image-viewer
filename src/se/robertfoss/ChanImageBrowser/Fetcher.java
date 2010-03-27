package se.robertfoss.ChanImageBrowser;

import java.io.File;

import android.graphics.Bitmap;

public class Fetcher extends Thread {
	
	private FetcherManager manager;
	
	Fetcher(FetcherManager manager){
		this.manager = manager; 
	}

	public void run(){
		Viewer.printDebug("Running a Fetcher thread");
		String inputFile = manager.getNextImageName();
		while (inputFile != null){
				try {
					Viewer.printDebug("Fetching picture  -  " + inputFile);
					Bitmap pic = Viewer.getImgFromUrl("http://images.4chan.org/b/src/" + inputFile);

					Viewer.printDebug("Fetcher is adding downloaded a picture");
					if (pic != null){
					manager.addCompleteImage(new File(inputFile), pic);
					} else {
						Viewer.printDebug(inputFile + " could'nt be parsed into a Bitmap");
					}
				} catch (Exception e) {
					Viewer.printDebug("An image wasnt downloaded correctly");
					e.printStackTrace();
				}
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				inputFile = manager.getNextImageName();
		}
	}
}