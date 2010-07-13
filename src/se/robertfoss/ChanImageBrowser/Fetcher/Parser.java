package se.robertfoss.ChanImageBrowser.Fetcher;

import java.util.ArrayList;
import java.util.HashMap;

import dk.brics.automaton.*;

public class Parser {

	private static HashMap<String, RunAutomaton> regexAutomatonMap;
	
	static {
		regexAutomatonMap = new HashMap<String, RunAutomaton>();
	}
	
	public static ArrayList<String> parseForStrings(String input, String regex, int resultFromIndex) {
		RunAutomaton automaton = regexAutomatonMap.get(regex);
		if (automaton == null){
			automaton = new RunAutomaton(new RegExp(regex).toAutomaton());
			regexAutomatonMap.put(regex, automaton);
		}
		
		ArrayList<String> matches = new ArrayList<String>();

		if (input != null) {
			AutomatonMatcher automMatcher = automaton.newMatcher(input);

			while (automMatcher.find()) {
				matches.add(automMatcher.group(0).substring(resultFromIndex));
			}
		}
		return matches;
	}
	
	
}
