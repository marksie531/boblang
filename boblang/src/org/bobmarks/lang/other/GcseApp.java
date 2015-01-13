package org.bobmarks.lang.other;

import java.io.File;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Display words with special symbols in them.
 */
public class GcseApp {

    private static final String LANG_FILE = "lang.xml";

    private Element root;

    /**
     * Constructor.
     */
    public GcseApp() throws Exception {
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
        display(true);
    }

    @SuppressWarnings("unchecked")
    private void display(boolean showHint) {
        List<Element> courseElms = root.getChildren("course");
        for (Element courseElm : courseElms) {

            List<Element> lessonElms = courseElm.getChildren("lesson");

            for (Element lessonElm : lessonElms) {
                String lessonName = lessonElm.getAttributeValue("name");

                if ("GCSE Writing Exam".equals(lessonName)) {

                    List<Element> topicElms = lessonElm.getChildren("topic");

                    for (Element topicElm : topicElms) {
                        String topicName = topicElm.getAttributeValue("name");

                        List<Element> itemElms = topicElm.getChildren("item");

                        for (Element itemElm : itemElms) {
                            String answer = itemElm.getAttributeValue("a");
                            String question = itemElm.getAttributeValue("q");

                            System.out.println(question);
                            System.out.println(answer);
                            System.out.println();

                            // System.out.println(question);
                            //
                            // String [] words = answer.split(" ");
                            // int i = 0;
                            // for (String word : words) {
                            // i++;
                            // System.out.print(word.charAt(0) + (i % 5 == 0 ? " " : ""));
                            // }
                            // System.out.println();
                            // System.out.println();
                        }
                    }
                }
            }
        }
    }

    /**
     * Main method.
     * 
     * @param args
     */
    public static void main(String [] args) throws Exception {
        new GcseApp();
    }
}