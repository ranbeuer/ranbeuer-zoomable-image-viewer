package se.robertfoss.ChanImageBrowser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PicIndexer extends Thread {
	
	private FetcherManager manager;
	String regex = "http://images.4chan.org/b/src/(\\d*).(jpg|gif|png)";
	private Pattern imgID;
	private Matcher matcher;
	
	
	PicIndexer(FetcherManager manager){
		this.manager = manager;
		imgID = Pattern.compile(regex);
	}
	public void run(){
		Viewer.printDebug("Running a indexfetcher thread");
		String imgboard = "";
		try {
			imgboard = Viewer.getUrlContent("http://img.4chan.org/b/imgboard.html");
		} catch (Exception e) {
			Viewer.printDebug("Fuckballs! Can't download /b/imgboard.html");
			// Replace me with run on ui Viewer.Toast("img.4chan.org can't be resolved");
			e.printStackTrace();
		}

		matcher = imgID.matcher(imgboard);
		
		while (matcher.find()){
			manager.addUrl(matcher.group(1)+ "." + matcher.group(2));
		}
		
		matcher = null;
	}
}
