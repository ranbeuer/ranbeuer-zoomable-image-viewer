package se.robertfoss.ChanImageBrowser.Fetcher;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

	public synchronized static ArrayList<String> parseForStrings(String input, String regex) {
		ArrayList<String> matches = new ArrayList<String>();
				
		try {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher;


		if (input != null) {
			matcher = pattern.matcher(input);

			while (matcher.find()) {
				matches.add(matcher.group(1));
			}
		}
		matcher = null;
		} catch (Exception e){
			e.printStackTrace();
		}
		return matches;
	}
}
