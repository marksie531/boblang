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
public class SpecialFormat {

	private static final String LANG_FILE = "lang.xml";
		
	private Element root;
	
	/**
	 * Constructor.
	 */
	public SpecialFormat() throws Exception {
		loadFile ();
		
		showSpecialSymbols ();
	}
	
	/**
	 * Load file.
	 * 
	 * @throws JDOMException
	 */
	private void loadFile () throws JDOMException {
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
	private void showSpecialSymbols () {
		display (true);
	}
	
	private void display (boolean showHint) {
		String format = "%1$-100s%2$-10s\n";
		List<Element>courseElms = root.getChildren("course"); 
		for (Element courseElm : courseElms) {
			String courseName = courseElm.getAttributeValue("name");

			if ("Level 3".equals (courseName)) {

				if (!showHint) {
					System.out.println ("\nCourse: " + courseName + "\n");
				}
				List<Element>lessonElms = courseElm.getChildren("lesson");

				for (Element lessonElm : lessonElms) {
					String lessonName = lessonElm.getAttributeValue("name");
					if (!showHint) {
						System.out.println ("\nLesson: " + lessonName + "\n");
					}

					List<Element>topicElms = lessonElm.getChildren("topic");

					for (Element topicElm : topicElms) {
						String topicName = topicElm.getAttributeValue("name");
						if (!showHint) {
							System.out.println ("\nTopic: " + topicName + "\n");
						}

						List<Element>itemElms = topicElm.getChildren("item");

						for (Element itemElm : itemElms) {
							String answer = itemElm.getAttributeValue("a");
							String question = itemElm.getAttributeValue("q");
							String hint = itemElm.getAttributeValue("h");
							
							if (hint != null && !hint.isEmpty() && showHint) {
								System.out.format (format, question + " - [ " + hint + " ]", answer); 
							}
							else if (!showHint) {
								System.out.format (format, question, answer);
							}
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
	public static void main (String [] args) throws Exception {
		new SpecialFormat ();
	}
}