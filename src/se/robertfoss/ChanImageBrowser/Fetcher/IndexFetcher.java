package se.robertfoss.ChanImageBrowser.Fetcher;

import java.util.ArrayList;

import android.widget.Toast;

import se.robertfoss.ChanImageBrowser.Viewer;
import se.robertfoss.ChanImageBrowser.Target.TargetUrl;

public class IndexFetcher extends Thread {
	private FetcherManager manager;
	private TargetUrl imageTarget;
	private TargetUrl linkTarget;
	private boolean isDone;
	private final static int LINK_DOWNLOAD_THRESHOLD = 2;

	IndexFetcher(FetcherManager manager, TargetUrl linkTarget,
			TargetUrl imageTarget) {
		this.manager = manager;
		this.imageTarget = imageTarget;
		this.linkTarget = linkTarget;
		isDone = false;
	}

	public void run() {

		while (!isDone) {
			while (manager.getNbrUrlLinks() < LINK_DOWNLOAD_THRESHOLD) {
				String url = linkTarget.getIndex();
				String inputHtml = null;

				do {
					try {
						inputHtml = Viewer.getUrlContent(url);
					} catch (Exception e) {
						Viewer.printDebug(" 	Unable to fetch index - " + url);
						manager.toastInUI("Unable to fetch index - " + url,
								Toast.LENGTH_LONG);
						e.printStackTrace();
					}
				} while (inputHtml.equals("") || inputHtml == null);
				// "" Is what gets return if nothing gets returned, null is
				// returned if
				// something breaks :S

				// Parse for images and add to managers list
				Viewer.printDebug("Fetching images from " + url);
				ArrayList<String> tempList = Parser.parseForStrings(inputHtml,
						imageTarget);
				for (String str : tempList) {
					manager.addImageUrl(str);
				}
				
				yield();
				
				// Parse for links and add to managers list
				Viewer.printDebug("Fetching links from " + url);
				tempList = Parser.parseForStrings(inputHtml, linkTarget);
				for (String str : tempList) {
					manager.addLinkUrl(str);
				}
				
				yield();
			}
		}
	}

	public void done() {
		isDone = true;
	}
}
