package se.robertfoss.ChanImageBrowser.Fetcher;

import java.util.ArrayList;

import android.widget.Toast;

import se.robertfoss.ChanImageBrowser.Viewer;
import se.robertfoss.ChanImageBrowser.Target.TargetUrl;

public class ThreadFetcher extends Thread {
	private FetcherManager manager;
	private boolean isDone;
	private TargetUrl imageTarget;

	ThreadFetcher(FetcherManager manager, TargetUrl imageTarget) {
		this.manager = manager;
		this.imageTarget = imageTarget;
		isDone = false;
	}

	public void done() {
		isDone = true;
	}

	public void run(){
		isDone = false;
		String url = manager.getNextUrl();
		while (!isDone && url  != null) {
			
			String inputHtml = null;
			do {
				Viewer.printDebug("Fetching images from " + url);
				try {
					Thread.sleep(15000);
					inputHtml = Viewer.getUrlContent(url);
				} catch (Exception e) {
					Viewer.printDebug(" 	Unable to fetch thread - " + url);
					manager.toastInUI("Unable to fetch thread - " + url,
							Toast.LENGTH_LONG);
				}
			} while (inputHtml == null);
			// "" Is what gets return if nothing gets returned, null is returned
			// if something breaks :S

			// Parse for images and add to managers list
			ArrayList<String> tempList = Parser.parseForStrings(inputHtml,
					imageTarget);
			for (String str : tempList) {
				manager.addImageUrl(str);
			}

			url = manager.getNextUrl();
			manager.resumeImageFetchers();
			yield();
		}
	}
}
