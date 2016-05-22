package org.bobmarks.lang;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Little application to help me with my french course but 
 * can be used to help learn any language.
 * 
 * @author Bob Marks
 */
public class BobLang extends JFrame {
	
	private static final long serialVersionUID = -6252615033771245711L;
	private static final String LANG_FILE = "lang.xml";
	//private static final String LANG_FILE = "mt_advanced.xml";
	
	private BobLangPanel langPanel;
	
	/**
	 *  Constructor
	 */
	public BobLang() throws Exception {
		super("Bob Lang");
	
		File file = new File(LANG_FILE);

		// Parse XML
		SAXBuilder builder = new SAXBuilder();
		Document dom = builder.build(file);
		Element root = dom.getRootElement();
		
		// Create and add panel
		this.langPanel = new BobLangPanel (root);
		getContentPane().add (this.langPanel, BorderLayout.CENTER);
		
		this.setVisible(true);
		this.setLocation(100, 100);
		this.setSize(930, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Main method.
	 * 
	 * @param args
	 */
	public static void main (String [] args) throws Exception {
		new BobLang ();
	}
}