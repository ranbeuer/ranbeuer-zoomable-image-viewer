package se.robertfoss.ChanImageBrowser.Target;

import java.util.ArrayList;
import java.util.regex.Matcher;

public class TargetUrl {
	
	private String index;
	private String commonUrl;
	private String regexp;
	private ArrayList<String> filler;
	private ArrayList<Integer> usableMatcherGroups;
	

	/**
	 * Handles the urls and regular expressions for the site and pictures you want
	 * 
	 * @param index - The index to pull links from
	 * @param commonUrl - The lowest common denominator of the urls  like "http://images.bollar.se/pics/"
	 * @param regexp - The regular expression used to parse a usable filename
	 */
	public TargetUrl(String index, String regexp, ArrayList<String> filler, ArrayList<Integer> usableMatcherGroups ){
		this.index = index;
		this.regexp = regexp;
		this.usableMatcherGroups = usableMatcherGroups;
		this.filler = filler;
		
	}
	
	public String getCompleteLinkUrl(Matcher matcher){
		StringBuilder sb = new StringBuilder();
		int maxLength;
		if (filler.size() > usableMatcherGroups.size()){
			maxLength = filler.size();
		} else {
			maxLength = usableMatcherGroups.size();
		}
		for (int i = 0; i < maxLength; i++){
			if (i < filler.size()){
				sb.append(filler.get(i));
			}
			if (i < usableMatcherGroups.size()){
				sb.append(matcher.group(usableMatcherGroups.get(i)));
			}
			
		}
		return sb.toString();
	}
	
	public String getIndex(){
		return index;
	}
	
	public String getRegexp(){
		return regexp;
	}
}
