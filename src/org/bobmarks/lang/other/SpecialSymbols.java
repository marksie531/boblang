package org.bobmarks.lang.other;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Display words with special symbols in them.
 */
public class SpecialSymbols {

    private static final String LANG_FILE = "lang.xml";
    private static final String SPECIAL_CHARS = "אגחיטךכמפשû"; // [א ב ג] [ח] [י ט ך כ] [מ ן] [ף פ] [ש û] [ז]

    private Element root;

    /**
     * Constructor.
     */
    public SpecialSymbols() throws Exception {
        loadFile();

        showSpecialSymbols();
    }

    /**
     * Load file.
     * 
     * @throws JDOMException
     */
    private void loadFile() throws JDOMException {
        File file = new File(LANG_FILE);

        // Parse XML
        SAXBuilder builder = new SAXBuilder();
        Document dom = builder.build(file);

        this.root = dom.getRootElement();
    }

    /**
     * Load GUI elements.
     */
    @SuppressWarnings("unchecked")
    private void showSpecialSymbols() {
        Map<String, Set<String>> pairs = new HashMap<String, Set<String>>();
        Map<String, String> examples = new HashMap<String, String>();
        for (int i = 0; i < SPECIAL_CHARS.length(); i++) {
            String sChar = String.valueOf(SPECIAL_CHARS.charAt(i));
            Set<String> list = new HashSet<String>();
            pairs.put(sChar, list);
        }

        List<Element> courseElms = root.getChildren("course");
        for (Element courseElm : courseElms) {
            List<Element> lessonElms = courseElm.getChildren("lesson");

            for (Element lessonElm : lessonElms) {
                List<Element> topicElms = lessonElm.getChildren("topic");

                for (Element topicElm : topicElms) {
                    List<Element> itemElms = topicElm.getChildren("item");

                    for (Element itemElm : itemElms) {
                        String answer = itemElm.getAttributeValue("a");
                        String question = itemElm.getAttributeValue("q");
                        StringTokenizer st = new StringTokenizer(answer, ",?.'- ");
                        while (st.hasMoreTokens()) {
                            String word = st.nextToken().toLowerCase();

                            for (int i = 0; i < SPECIAL_CHARS.length(); i++) {
                                String sChar = String.valueOf(SPECIAL_CHARS.charAt(i));
                                if (word.indexOf(sChar) != -1) {
                                    if (!pairs.get(sChar).contains(word)) {
                                        pairs.get(sChar).add(word);
                                        examples.put(word, answer + " => " + question);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        String format = "%1$-20s%2$-10s\n";
        for (int i = 0; i < SPECIAL_CHARS.length(); i++) {
            String sChar = String.valueOf(SPECIAL_CHARS.charAt(i));
            Set<String> sWords = pairs.get(sChar);

            System.out.println("================================================================");
            System.out.println(sChar + " (" + sWords.size() + ")");
            System.out.println("================================================================\n");

            Collections.sort(new ArrayList<String>(sWords));
            for (String word : sWords) {
                System.out.format(format, word, examples.get(word));
            }
            System.out.println();
        }
    }

    /**
     * Main method.
     * 
     * @param args
     */
    public static void main(String [] args) throws Exception {
        new SpecialSymbols();
    }
}