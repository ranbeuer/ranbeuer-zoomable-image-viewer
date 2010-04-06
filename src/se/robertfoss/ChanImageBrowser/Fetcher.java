package se.robertfoss.ChanImageBrowser;

import java.io.File;

public class Fetcher extends Thread {
	
	private FetcherManager manager;
	
	Fetcher(FetcherManager manager){
		this.manager = manager; 
	}

	public void run(){
		String inputFile = manager.getNextImageName();
		while (inputFile != null){
				try {
					Viewer.printDebug("Fetching picture  -  " + inputFile);
					File pic = Viewer.getFileFromUrl("http://images.4chan.org/b/src/" + inputFile, inputFile);

					Viewer.printDebug("Fetcher is adding downloaded a picture");
					if (pic != null){
					manager.addCompleteImage(pic);
					} else {
						Viewer.printDebug("	" + inputFile + " could'nt be parsed into a Bitmap");
					}
				} catch (Exception e) {
					Viewer.printDebug("An image wasnt downloaded correctly");
					e.printStackTrace();
				}
				inputFile = manager.getNextImageName();
		}
	}
}
