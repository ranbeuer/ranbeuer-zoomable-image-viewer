package se.robertfoss.ChanImageBrowser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser{


	public static ArrayList<String> parseForStrings(String url, String regex) throws IOException{
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher;
		ArrayList<String> imageList = new ArrayList<String>();
		Viewer.printDebug("Running a indexfetcher thread");
		String html = "";
		
		html = Viewer.getUrlContent(url);

		matcher = pattern.matcher(html);
		
		while (matcher.find()){
			imageList.add(matcher.group(1)+ "." + matcher.group(2));
		}
		
		matcher = null;
		return imageList;
	}
}
