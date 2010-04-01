package se.robertfoss.ChanImageBrowser;

import java.io.File;
import android.os.AsyncTask;

public class Fetcher extends AsyncTask<Void, Void, Void> {
	
	private FetcherManager manager;
	
	Fetcher(FetcherManager manager){
		this.manager = manager; 
	}

	protected Void doInBackground(Void... params){
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
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				inputFile = manager.getNextImageName();
		}
		return (Void)null;
	}
}
