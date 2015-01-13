package org.bobmarks.lang.rtfparser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

/**
 * Michal Thomas RTF Parser.
 * 
 * @author Bob Marks
 */
public class GcseVocabParser {

    /**
     * @param filename
     * @param course
     * @throws Exception
     */
    public GcseVocabParser() throws Exception {

        System.out.println("<?xml version=\"1.0\"?>\n");
        System.out.println("<!--GCSE Vocab List\n-->");

        System.out.println("<language name=\"french\" specialchars=\"àâçéèêëîôùû\">");
        System.out.println("    <course name=\"gcse_vocab\">");
        System.out.println("        <lesson name=\"General\">");

        BufferedReader br = new BufferedReader(new FileReader("gcse_vocab_list.txt"));
        String nextLine = null;

        int lesson = 1;
        while ((nextLine = br.readLine()) != null) {
            nextLine = nextLine.trim();
            if (nextLine.endsWith(":")) {
                String title = nextLine.replace(":", "");

                // Create title case
                StringTokenizer st = new StringTokenizer(title, " ");
                String sep = "";
                StringBuilder sb = new StringBuilder();
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (token.length() > 1) {
                        sb.append(sep + token.substring(0, 1).toUpperCase() + token.substring(1).toLowerCase());
                    }
                    else {
                        sb.append(sep + token.toUpperCase());
                    }
                    sep = " ";
                }
                title = lesson++ + ". " + sb.toString();

                title = title.replace("&", "and");
                System.out.println("           </topic>");
                System.out.println("           <topic name\"" + title + "\">");
            }
            else if (nextLine.contains("\t")) {
                String [] items = nextLine.split("\t");
                System.out.println("                <item q=\"" + items[1] + "\" a=\"" + items[0] + "\"/>");
                if (items.length == 4) {
                    System.out.println("                <item q=\"" + items[3] + "\" a=\"" + items[2] + "\"/>");
                }
            }
        }

        System.out.println("            </topic>");
        System.out.println("        </lesson>");
        System.out.println("    </course>");
        System.out.println("</language>");

        br.close();
    }

    public static void main(String [] args) throws Exception {
        new GcseVocabParser();
    }
}