package org.bobmarks.lang;

import java.awt.BorderLayout;
import java.io.StringReader;

import javax.swing.JApplet;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Little application to help me with my french course.
 * 
 * @author Bob Marks
 */
public class BobLangApplet extends JApplet {
	
	private static final long serialVersionUID = -6252615033771245711L;
	private BobLangPanel langPanel;
	
	/**
	 *  Constructor
	 */
	public BobLangApplet() throws Exception {
		// Parse XML

		SAXBuilder builder = new SAXBuilder();
		StringReader sr = new StringReader (BobLangInput.DATA.toString());
		Document dom = builder.build(sr);
		Element root = dom.getRootElement();
		
		this.langPanel = new BobLangPanel (root);
	}
	
	/**
	 * Init method.
	 * 
	 * @see java.applet.Applet#init()
	 */
	public void init ()  {
		getContentPane().add (this.langPanel, BorderLayout.CENTER);
		setSize (900, 600);
		this.setVisible(true);
	}
}