package se.robertfoss.ChanImageBrowser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Dialog;

public class Parser{
	
	/*
	private FetcherManager manager;
	String regex = "http://images.4chan.org/b/src/(\\d*).(jpg|gif|png)";
	private Pattern imgID;
	private Matcher matcher;*/
	

	public static ArrayList<String> parseForStrings(String url, String regex){
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher;
		ArrayList<String> imageList = new ArrayList<String>();
		Viewer.printDebug("Running a indexfetcher thread");
		String html = "";
		try {
			html = Viewer.getUrlContent(url);
		} catch (Exception e) {
			Viewer.printDebug("Fuckballs! Can't download " + url);
			// Replace me with run on ui Viewer.Toast("img.4chan.org can't be resolved");
			e.printStackTrace();
		}

		matcher = pattern.matcher(html);
		
		while (matcher.find()){
			imageList.add(matcher.group(1)+ "." + matcher.group(2));
		}
		
		matcher = null;
		return imageList;
	}
}
