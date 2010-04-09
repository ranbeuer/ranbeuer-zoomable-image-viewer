package se.robertfoss.ChanImageBrowser;

import java.io.File;

public class Fetcher extends Thread {
	
	private FetcherManager manager;
	
	Fetcher(FetcherManager manager){
		this.manager = manager; 
	}

	public void run(){
		String inputUrl = manager.getNextImageName();
		while (inputUrl != null){
				try {
					Viewer.printDebug("Fetching picture  -  " + inputUrl);
					String[] fileName = inputUrl.split("/");
					
					File pic = Viewer.getFileFromUrl(inputUrl, fileName[fileName.length-1]);

					Viewer.printDebug("Fetcher is adding downloaded a picture");
					if (pic != null){
					manager.addCompleteImage(pic);
					} else {
						Viewer.printDebug("	" + inputUrl + " could'nt be parsed into a Bitmap");
					}
				} catch (Exception e) {
					Viewer.printDebug("An image wasnt downloaded correctly");
					e.printStackTrace();
				}
				inputUrl = manager.getNextImageName();
		}
	}
}
