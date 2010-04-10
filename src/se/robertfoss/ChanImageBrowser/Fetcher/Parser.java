package se.robertfoss.ChanImageBrowser.Fetcher;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.robertfoss.ChanImageBrowser.Target.TargetUrl;

public class Parser {

	public synchronized static ArrayList<String> parseForStrings(String html, TargetUrl target) {
		ArrayList<String> imageList = new ArrayList<String>();
		
		try {
		Pattern pattern = Pattern.compile(target.getRegexp());
		Matcher matcher;


		if (html != null) {
			matcher = pattern.matcher(html);

			while (matcher.find()) {
				imageList.add(target.getCompleteLinkUrl(matcher));
			}
		}
		matcher = null;
		} catch (Exception e){
			e.printStackTrace();
		}
		return imageList;
	}
}
