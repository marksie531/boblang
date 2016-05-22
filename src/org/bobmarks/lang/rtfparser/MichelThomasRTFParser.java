package org.bobmarks.lang.rtfparser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Michal Thomas RTF Parser.
 * 
 * @author Bob Marks
 */
public class MichelThomasRTFParser {

	private final Map<String,String>rtfMap;

	private int cd = 0;
	private int track = 0;

	/**
	 * @param filename
	 * @param course
	 * @throws Exception
	 */
	public MichelThomasRTFParser (String filename, String course) throws Exception {
		rtfMap = new HashMap<String, String>();
		rtfMap.put("\\'e0","�");
		rtfMap.put("\\'e2","�");
		rtfMap.put("\\'e7","�");
		rtfMap.put("\\'e9","�");
		rtfMap.put("\\'e8","�");
		rtfMap.put("\\'ea","�");
		rtfMap.put("\\'cb","�");
		rtfMap.put("\\'ee","�");
		rtfMap.put("\\'f4","�");
		rtfMap.put("\\'f9","�");
		rtfMap.put("\\'fb","�");
		rtfMap.put("\\'c0","�");
		rtfMap.put("\\'c2","�");
		rtfMap.put("\\'c7","�");
		rtfMap.put("\\'c9","�");
		rtfMap.put("\\'e8","�");
		rtfMap.put("\\'ea","�");
		rtfMap.put("\\'cb","�");
		rtfMap.put("\\'ce","�");
		rtfMap.put("\\'d4","�");
		rtfMap.put("\\'d9","�");
		rtfMap.put("\\'db","�");

		System.out.println ("<?xml version=\"1.0\"?>\n");
		System.out.println ("<!-- \n" + course + "\n-->");

		System.out.println ("<language name=\"french\" specialchars=\"�����������\">");
		System.out.println ("    <course name=\"" + course + "\">");

		BufferedReader br = new BufferedReader(new FileReader(filename));
		StringBuilder sb = new StringBuilder();
		String nextLine = null;        
		Pattern p = Pattern.compile("(.*?)\\\\b(.*?)\\\\b", Pattern.DOTALL);

		while ((nextLine = br.readLine()) != null) {
			if(sb.length() > 0)
				sb.append("\n");
			sb.append(nextLine);

			if (nextLine.startsWith("\\ul\\b CD ")) {
				cd = Integer.parseInt(nextLine.substring(9,10).trim());
				track = 1;
				//System.out.println (cd);
				//System.out.println ("\t" + track);
				if (cd != 0) {
					System.out.println ("            </topic>");
					System.out.println ("        </lesson>");
				}

				System.out.println ("        <lesson name=\"CD " + cd + "\">");
				System.out.println ("            <topic name=\"Track " + track + "\">");
			}
			else if (nextLine.matches("\\(\\d+\\)\\s\\d.*")) {
				track = Integer.parseInt(getRegex(nextLine, "\\((\\d+)\\)", 1));
				//System.out.println ("\t" + track);

				System.out.println ("            </topic>");
				System.out.println ("            <topic name=\"Track " + track + "\">");
			}
			else if (track != 0) {
				Matcher m = p.matcher(nextLine);
				if (m.find()) {
					String q = m.group(1).trim(); 
					String a = m.group(2).trim();

					System.out.println ("                <item a=\"" + encode(a) + "\" q=\"" + encode(q) + "\"/>");
				}
				else {
					System.out.println ("                <item a=\"\" q=\"\"/>");
					System.out.println ("!!! " + nextLine); 
				}
			}

			//System.out.println (sb.toString());
		}

		System.out.println ("            </topic>");
		System.out.println ("        </lesson>");
		System.out.println ("    </course>");
		System.out.println ("</language>");

		br.close();
	}

	private String encode(String input) {
		for (String key : rtfMap.keySet()) {
			if (input.contains(key)) {
				input = input.replace(key, rtfMap.get(key));
			}
		}
		if (input.contains("\\'")) {
			throw new RuntimeException ("Unmapped symbol: " + input);
		}

		return input;
	}

	public static String getRegex(String input, String regex, int group) {
		Pattern p = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = p.matcher(input);
		while (m.find()) {
			return m.group(group);
		}
		return null;
	}    

	public static void main(String[] args) throws Exception {
		new MichelThomasRTFParser ("MT French advanced.rtf", "Michel Thomas - Advanced");
	}
}