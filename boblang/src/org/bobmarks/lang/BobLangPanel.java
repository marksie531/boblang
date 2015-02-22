package org.bobmarks.lang;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.jdom.Element;

/**
 * Language panel.
 * 
 * @author Bob Marks
 */
public class BobLangPanel extends JPanel implements ActionListener, CaretListener, FocusListener, KeyListener, MouseListener {

    private static final long serialVersionUID = 2484991827672131644L;

    private static final double PREF = TableLayout.PREFERRED;
    private static final double FILL = TableLayout.FILL;
    private static final Color COLOR_OK = new Color(230, 255, 220);
    private static final Color COLOR_ALMOST = new Color(255, 255, 210);
    private static final Color COLOR_WRONG = new Color(255, 230, 230);
    private static final Color COLOR_HINT = new Color(0, 192, 0);

    private static final Font FONT_SMALL = new Font("Arial", Font.PLAIN, 10);

    private static final String REPLACES = " -,?'.!";

    // GUI fields
    private JPanel mainPanel, coursePanel, lessonPanel, topicPanel;
    private JCheckBox shuffleCB, examMode, hintMode, reverseMode;
    private JTextComponent curInput = null, limitInput;
    private JButton startButton, hintButton, checkButton, editButton;
    private JLabel examLabel;

    // Other fields
    private boolean shift = false;
    private final Element root;
    private Map<String, String> itemMap;
    private Map<String, String> itemHintMap;
    private final String specialChars;

    /**
     * Constructor.
     */
    public BobLangPanel(Element root) throws Exception {
        this.root = root;
        this.specialChars = root.getAttributeValue("specialchars");
        loadGui(root);
    }

    /**
     * Load GUI elements.
     * 
     * @param root2
     */
    private void loadGui(Element root2) {
        mainPanel = new JPanel();
        setLayout(new BorderLayout());
        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getControlPanel(), new JScrollPane(mainPanel));

        setLayout(new BorderLayout());
        add(pane, BorderLayout.CENTER);
        add(getInputButtons(), BorderLayout.SOUTH);

        refreshCoursePanel();
        updateStates();
    }

    /**
     * Return input buttons.
     * 
     * @return
     */
    private JPanel getInputButtons() {
        JPanel inputButtons = new JPanel();

        for (int i = 0; i < specialChars.length(); i++) {
            String sChar = String.valueOf(specialChars.charAt(i));
            JButton button = new JButton(sChar);
            button.setName("symbol:" + sChar);
            button.addActionListener(this);
            inputButtons.add(button);
            if (i % 4 == 3) {
                inputButtons.add(new JLabel("     "));
            }
        }

        examLabel = new JLabel();
        inputButtons.add(examLabel);

        return inputButtons;
    }

    /**
     * Return the control panel.
     * 
     * @return
     */
    private JSplitPane getControlPanel() {

        // Create header panels
        coursePanel = new JPanel();
        coursePanel.add(new JLabel("             "));
        lessonPanel = new JPanel();
        lessonPanel.add(new JLabel("             "));
        topicPanel = new JPanel();
        topicPanel.add(new JLabel("             "));

        // Add all to main control panel
        JSplitPane left = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getTitledScrolledPanel("Course", coursePanel, true), getTitledScrolledPanel(
            "Lesson", lessonPanel, true));
        JSplitPane right = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getTitledScrolledPanel("Topic", topicPanel, true), getTitledScrolledPanel(
            "Actions", getActionsPanel(), false));
        JSplitPane both = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        left.setDividerLocation(150);
        right.setDividerLocation(350);
        both.setDividerLocation(350);

        return both;
    }

    /**
     * Return a title scrolled pane
     * 
     * @param label
     * @param contentPanel
     * @param buttons
     * @return
     */
    private JPanel getTitledScrolledPanel(String label, JPanel contentPanel, boolean buttons) {
        double [][] sizes = { { 5, FILL, 5, 35, 5, 45, 5 }, { 5, 20, 5, FILL, 5 } };
        JPanel panel = new JPanel(new TableLayout(sizes));
        JScrollPane sp = new JScrollPane(contentPanel);
        JLabel titleLabel = new JLabel(label);

        panel.add(titleLabel, "1,1");
        panel.add(sp, "1,3,5,3");

        if (buttons) {
            JButton allButton = new JButton("All");
            allButton.setFont(FONT_SMALL);
            allButton.setMargin(new Insets(0, 0, 0, 0));
            JButton noneButton = new JButton("None");
            noneButton.setFont(FONT_SMALL);
            noneButton.setMargin(new Insets(0, 0, 0, 0));

            panel.add(allButton, "3,1");
            panel.add(noneButton, "5,1");

            allButton.addActionListener(this);
            allButton.setName("all_" + label.toLowerCase());
            noneButton.addActionListener(this);
            noneButton.setName("none_" + label.toLowerCase());
        }

        return panel;
    }

    @SuppressWarnings("unchecked")
    private void refreshCoursePanel() {
        coursePanel.removeAll();
        lessonPanel.removeAll();
        topicPanel.removeAll();

        List<Element> courseElms = root.getChildren("course");
        coursePanel.setLayout(new TableLayout(getTableLayoutSize(1, courseElms.size(), PREF, PREF)));
        for (int i = 0; i < courseElms.size(); i++) {
            Element courseElm = courseElms.get(i);
            String name = courseElm.getAttributeValue("name");
            JCheckBox courseCB = new JCheckBox(name);
            courseCB.setName("course:" + name);
            courseCB.addActionListener(this);
            coursePanel.add(courseCB, "0," + i);
        }
        coursePanel.revalidate();
        topicPanel.revalidate();
        lessonPanel.revalidate();

        mainPanel.getParent().invalidate();
        mainPanel.getParent().validate();
    }

    /**
     * Refresh lesson panel.
     */
    @SuppressWarnings("unchecked")
    private void refreshLessonPanel() {
        lessonPanel.removeAll();
        topicPanel.removeAll();

        List<Element> courseElms = root.getChildren("course");
        List<JComponent> lessonCBS = new ArrayList<JComponent>();
        for (Element courseElm : courseElms) {

            String courseName = courseElm.getAttributeValue("name");
            JCheckBox courseCB = getCourseCB(courseName);
            if (courseCB.isSelected()) {
                List<Element> lessonElms = courseElm.getChildren("lesson");
                JLabel courseLabel = new JLabel(courseName);
                courseLabel.setFont(FONT_SMALL);
                lessonCBS.add(courseLabel);
                for (int i = 0; i < lessonElms.size(); i++) {
                    String lessonName = lessonElms.get(i).getAttributeValue("name");
                    JCheckBox lessonCB = new JCheckBox(lessonName);
                    String lessonCBName = "lesson:" + lessonName + ",course:" + courseName;
                    lessonCB.setName(lessonCBName);
                    lessonCB.addActionListener(this);
                    lessonCBS.add(lessonCB);
                }
                lessonCBS.add(new JSeparator());
            }
        }

        if (lessonCBS.size() > 0) {
            TableLayout layout = new TableLayout(getTableLayoutSize(1, lessonCBS.size(), FILL, PREF));
            lessonPanel.setLayout(layout);
            for (int i = 0; i < lessonCBS.size(); i++) {
                JComponent comp = lessonCBS.get(i);
                if (comp instanceof JLabel)
                    lessonPanel.add(comp, "0," + i + ",c,c");
                else
                    lessonPanel.add(comp, "0," + i);
            }

            layout.layoutContainer(topicPanel.getParent());
        }
        else {
            lessonPanel.setLayout(new BorderLayout());
            lessonPanel.add(new JLabel("             "), BorderLayout.CENTER);
        }
        topicPanel.revalidate();
        lessonPanel.revalidate();

        mainPanel.getParent().invalidate();
        mainPanel.getParent().validate();
    }

    @SuppressWarnings("unchecked")
    private void refreshTopicPanel() {
        topicPanel.removeAll();

        List<Element> courseElms = root.getChildren("course");
        List<JComponent> topicCBS = new ArrayList<JComponent>();
        for (Element courseElm : courseElms) {

            String courseName = courseElm.getAttributeValue("name");
            JCheckBox courseCB = getCourseCB(courseName);
            if (courseCB.isSelected()) {

                List<Element> lessonElms = courseElm.getChildren("lesson");
                for (Element lessonElm : lessonElms) {
                    String lessonName = lessonElm.getAttributeValue("name");
                    String lessonCBName = "lesson:" + lessonName + ",course:" + courseName;
                    JCheckBox lessonCB = getLessonCB(lessonCBName);

                    if (lessonCB.isSelected()) {
                        List<Element> topicElms = lessonElm.getChildren("topic");

                        JLabel courseLabel = new JLabel(courseName + " - " + lessonName);
                        courseLabel.setFont(FONT_SMALL);
                        topicCBS.add(courseLabel);

                        for (Element topicElm : topicElms) {

                            String name = topicElm.getAttributeValue("name");
                            JCheckBox topicCB = new JCheckBox(name);
                            topicCB.setName("topic:" + name);
                            topicCB.addActionListener(this);
                            topicCBS.add(topicCB);
                        }
                        topicCBS.add(new JSeparator());
                    }
                }

                if (topicCBS.size() > 0) {
                    TableLayout layout = new TableLayout(getTableLayoutSize(1, topicCBS.size(), FILL, PREF));
                    topicPanel.setLayout(layout);
                    for (int i = 0; i < topicCBS.size(); i++) {
                        JComponent comp = topicCBS.get(i);
                        if (comp instanceof JLabel)
                            topicPanel.add(comp, "0," + i + ",c,c");
                        else
                            topicPanel.add(comp, "0," + i);

                    }

                    layout.layoutContainer(topicPanel.getParent());
                }
                else {
                    topicPanel.setLayout(new BorderLayout());
                    topicPanel.add(new JLabel("             "), BorderLayout.CENTER);
                }
            }
        }

        topicPanel.revalidate();
        lessonPanel.revalidate();

        mainPanel.getParent().invalidate();
        mainPanel.getParent().validate();
    }

    private JCheckBox getCourseCB(String name) {
        for (Component comp : coursePanel.getComponents()) {
            String compName = comp.getName();
            if (compName != null) {
                if (compName.indexOf(":" + name) != -1) {
                    return (JCheckBox) comp;
                }
            }
        }
        return null;
    }

    private JCheckBox getLessonCB(String name) {
        for (Component comp : lessonPanel.getComponents()) {
            String compName = comp.getName();
            if (compName != null) {
                if (name.equals(compName)) {
                    return (JCheckBox) comp;
                }
            }
        }
        return null;
    }

    private JCheckBox getTopicCB(String name) {
        for (Component comp : topicPanel.getComponents()) {
            String compName = comp.getName();
            if (compName != null) {
                if (compName.indexOf(":" + name) != -1) {
                    return (JCheckBox) comp;
                }
            }
        }
        return null;
    }

    private JTextField getInput(String name) {
        for (Component comp : mainPanel.getComponents()) {
            String compName = comp.getName();
            if (compName != null) {
                if (compName.equals(name)) {
                    return (JTextField) comp;
                }
            }
        }
        return null;
    }

    /**
     * Return OK panel.
     * 
     * @return
     */
    private JPanel getActionsPanel() {
        double [][] sizes = { { 5, PREF, 5, PREF, 5, PREF, 5 }, { 5, PREF, 5, PREF, 5, PREF, 5, PREF, 5 } };
        JPanel panel = new JPanel(new TableLayout(sizes));

        startButton = new JButton("Start");
        startButton.setFont(FONT_SMALL);
        startButton.setMargin(new Insets(0, 0, 0, 0));
        startButton.addActionListener(this);
        startButton.setName("start");

        hintButton = new JButton("Hint");
        hintButton.setFont(FONT_SMALL);
        hintButton.setMargin(new Insets(0, 0, 0, 0));
        hintButton.addActionListener(this);
        hintButton.setName("hint");

        checkButton = new JButton("Check");
        checkButton.setFont(FONT_SMALL);
        checkButton.setMargin(new Insets(0, 0, 0, 0));
        checkButton.addActionListener(this);
        checkButton.setName("check");

        editButton = new JButton("Edit");
        editButton.setFont(FONT_SMALL);
        editButton.setMargin(new Insets(0, 0, 0, 0));
        editButton.addActionListener(this);
        editButton.setName("edit");

        shuffleCB = new JCheckBox("Shuffle");
        examMode = new JCheckBox("Exam");
        examMode.setName("exam");
        examMode.addActionListener(this);

        reverseMode = new JCheckBox("Reverse");
        reverseMode.setName("reverse");
        reverseMode.addActionListener(this);

        limitInput = new JTextField(4);
        limitInput.setVisible(false);

        hintMode = new JCheckBox("Hints");
        hintMode.setName("hints");
        hintMode.addActionListener(this);

        panel.add(startButton, "1,1");
        panel.add(checkButton, "1,3");
        panel.add(hintButton, "1,5");
        panel.add(editButton, "1,7");
        panel.add(shuffleCB, "3,1");
        panel.add(hintMode, "3,3");
        panel.add(examMode, "3,5");
        panel.add(limitInput, "5,5");
        panel.add(reverseMode, "3,7");

        return panel;
    }

    /**
     * Start studing.
     */
    @SuppressWarnings("unchecked")
    private void start() {
        examLabel.setText("");
        this.itemMap = new LinkedHashMap<String, String>();
        this.itemHintMap = new LinkedHashMap<String, String>();

        List<Element> courseElms = root.getChildren("course");
        for (Element courseElm : courseElms) {

            String courseName = courseElm.getAttributeValue("name");
            JCheckBox courseCB = getCourseCB(courseName);
            if (courseCB.isSelected()) {

                List<Element> lessonElms = courseElm.getChildren("lesson");
                for (Element lessonElm : lessonElms) {
                    String lessonName = lessonElm.getAttributeValue("name");
                    String lessonCBName = "lesson:" + lessonName + ",course:" + courseName;
                    JCheckBox lessonCB = getLessonCB(lessonCBName);

                    if (lessonCB.isSelected()) {
                        List<Element> topicElms = lessonElm.getChildren("topic");

                        for (Element topicElm : topicElms) {
                            String name = topicElm.getAttributeValue("name");
                            JCheckBox topicCB = getTopicCB(name);

                            if (topicCB.isSelected()) {

                                List<Element> itemElms = topicElm.getChildren("item");
                                for (Element itemElm : itemElms) {
                                    String question = itemElm.getAttributeValue("q");
                                    String answer = itemElm.getAttributeValue("a");
                                    String hint = itemElm.getAttributeValue("h");
                                    if (question == null || "".equals(question.trim())) {
                                        System.out.println("Question is null for answer: " + answer);
                                    }
                                    if (answer == null || "".equals(answer.trim())) {
                                        System.out.println("Answer is null for question: " + question);
                                    }
                                    boolean include = true;
                                    if (hintMode.isSelected() && hint == null) {
                                        include = false;
                                    }

                                    if (include) {
                                        itemMap.put(question.trim(), answer.trim());
                                        itemHintMap.put(question.trim(), hint != null ? hint.trim() : null);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        mainPanel.removeAll();
        if (itemMap.size() > 0) {

            double [][] sizes = getTableLayoutSize(4, itemMap.size(), PREF, PREF);
            TableLayout layout = new TableLayout(sizes);
            mainPanel.setLayout(new TableLayout(sizes));

            List<String> keys = new ArrayList<String>(itemMap.keySet());
            if (shuffleCB.isSelected()) {
                Collections.shuffle(keys);
            }

            // figure out limit (if applicable)
            int limit = keys.size();
            if (examMode.isSelected() && limitInput.getText().length() > 0) {
                try {
                    limit = Integer.parseInt(limitInput.getText().trim());
                }
                catch (NumberFormatException nfEx) {
                    // no nothing
                }
            }

            // Add items
            boolean reverse = reverseMode.isSelected();
            for (int i = 0; i < limit; i++) {
                String key = keys.get(i);
                String value = itemMap.get(key);

                JLabel labelNum = new JLabel();
                labelNum.setText((i + 1) + "     ");
                labelNum.setFont(FONT_SMALL);

                JLabel labelQuestion = new JLabel();
                if (reverse) {
                    labelQuestion.addMouseListener(this);
                    labelQuestion.setName("reverse:" + key);
                    labelQuestion.setText(value + " ");
                }
                else {
                    labelQuestion.setText(key + " ");
                }

                JTextField inputAnswer = new JTextField(80);
                inputAnswer.setName("input:" + key);
                inputAnswer.addCaretListener(this);
                inputAnswer.addFocusListener(this);
                inputAnswer.addKeyListener(this);

                mainPanel.add(labelNum, "0," + i + ",l,c");
                mainPanel.add(labelQuestion, "1," + i + ",r,c");
                mainPanel.add(inputAnswer, "2," + i + ",l,c");

                // Add hint if applicable
                if (itemHintMap.get(key) != null && !itemHintMap.get(key).isEmpty()) {
                    JLabel labelHint = new JLabel(" (H) ");
                    labelHint.setForeground(COLOR_HINT);
                    labelHint.setFont(FONT_SMALL);
                    labelHint.setName("hint:" + key);
                    labelHint.addMouseListener(this);
                    mainPanel.add(labelHint, "3," + i + ",l,c");
                }
            }

            layout.layoutContainer(mainPanel.getParent());
        }
        mainPanel.getParent().invalidate();
        mainPanel.getParent().validate();
    }

    private void hint() {
        if (curInput != null) {
            String text = curInput.getText();
            String name = curInput.getName();
            String value = itemMap.get(name.substring(6));

            if (!text.equals("") && !value.startsWith(text)) {
                // find where abouts the text differs
                int index;
                for (index = 0; index < text.length(); index++) {
                    if (index < value.length()) {
                        String tc = text.toLowerCase().substring(index, index + 1);
                        String vc = value.toLowerCase().substring(index, index + 1);
                        if (!tc.equals(vc)) {
                            break;
                        }
                    }
                }

                text = text.substring(0, index);
            }
            int pos = text.length();
            if (pos < value.length()) {
                text = text + value.substring(pos, pos + 1);
            }
            curInput.setText(text);
            curInputRequestFocus();
        }
    }

    /**
     * Edit mode.
     */
    private void edit() {
        if (JOptionPane.showConfirmDialog(getParent(), "Are you sure you want to switch to edit mode", "Switch to edit mode",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            // Create Edit GUI items
            final JTextArea questionTA = new JTextArea();
            final JTextArea answerTA = new JTextArea();
            questionTA.setBorder(BorderFactory.createEtchedBorder());
            answerTA.setBorder(BorderFactory.createEtchedBorder());

            JButton button = new JButton("Topic XML");
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String [] question = questionTA.getText().split("\n");
                    String [] answer = answerTA.getText().split("\n");

                    if (question.length != answer.length) {
                        JOptionPane.showMessageDialog(getParent(), "Question [ " + question.length + " ] and Answer [ " + answer.length +
                            " ] rows different");
                        return;
                    }

                    String S = "            ";
                    StringBuffer sb = new StringBuffer(S + "<topic name=\"\">\n");
                    for (int i = 0; i < question.length; i++) {
                        sb.append(S + "    <item a=\"" + answer[i] + "\" q=\"" + question[i] + "\"/>\n");
                    }
                    sb.append(S + "</topic>");

                    StringSelection data = new StringSelection(sb.toString());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(data, data);

                    JOptionPane.showMessageDialog(getParent(), sb);
                }
            });

            // Add items to main panel
            mainPanel.removeAll();
            double [][] sizes = { { FILL }, { FILL, 5, PREF, 5 } };

            JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getEditTextPanel("Questions", questionTA), getEditTextPanel(
                "Answer", answerTA));
            pane.setDividerLocation(500);
            TableLayout layout = new TableLayout(sizes);
            mainPanel.setLayout(layout);
            mainPanel.add(pane, "0,0");
            mainPanel.add(button, "0,2,c,c");
            mainPanel.getParent().invalidate();
            mainPanel.getParent().validate();

            // Add listeners
            questionTA.addCaretListener(this);
            questionTA.addFocusListener(this);
            questionTA.addKeyListener(this);
            answerTA.addCaretListener(this);
            answerTA.addFocusListener(this);
            answerTA.addKeyListener(this);

            layout.layoutContainer(mainPanel.getParent());
        }
    }

    /**
     * Return edit panel.
     * 
     * @param label
     * @param textArea
     * @return
     */
    private JScrollPane getEditTextPanel(String label, JTextArea textArea) {
        double [][] sizes = { { FILL }, { PREF, 5, FILL } };
        JPanel panel = new JPanel(new TableLayout(sizes));
        panel.add(new JLabel(label), "0,0");
        panel.add(textArea, "0,2");
        return new JScrollPane(panel);
    }

    /**
     * Check language.
     * 
     * @param name
     */
    private void check(String name) {
        JTextField inputTF = getInput(name);

        if (inputTF != null) {
            String input = inputTF.getText().trim();
            String answer = itemMap.get(name.substring(6));
            if (answer.equalsIgnoreCase(input)) {
                inputTF.setBackground(COLOR_OK);
            }
            else {
                for (char c : REPLACES.toCharArray()) {
                    input = input.replace(String.valueOf(c), "");
                    answer = answer.replace(String.valueOf(c), "");
                }
                if (input.equalsIgnoreCase(answer)) {
                    inputTF.setBackground(COLOR_ALMOST);
                }
                else {
                    inputTF.setBackground(Color.white);
                }
            }
        }
    }

    /**
     * Check all text boxes.
     */
    private void examCheck() {
        int total = 0;
        int almost = 0;
        int correct = 0;
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JTextField) {
                total++;
                String name = comp.getName();
                JTextField inputTF = getInput(name);

                String input = inputTF.getText().trim();
                String answer = itemMap.get(name.substring(6));
                if (answer.equalsIgnoreCase(input)) {
                    inputTF.setBackground(COLOR_OK);
                    correct++;
                    almost++;
                }
                else {
                    String rInput = new String(input);
                    String rAnswer = new String(answer);
                    for (char c : REPLACES.toCharArray()) {
                        rInput = rInput.replace(String.valueOf(c), "");
                        rAnswer = rAnswer.replace(String.valueOf(c), "");
                    }
                    if (rInput.equalsIgnoreCase(rAnswer)) {
                        inputTF.setBackground(COLOR_ALMOST);
                        inputTF.setText(input + " => " + answer);
                        almost++;
                    }
                    else {
                        inputTF.setBackground(COLOR_WRONG);
                        inputTF.setText(input + " => " + answer);
                    }
                }
            }
        }
        // Display correct values
        String cp = String.valueOf((double) correct / (double) total * 100);
        if (cp.length() > 5) {
            cp = cp.substring(0, 5);
        }
        // Display almost values
        String ap = String.valueOf((double) almost / (double) total * 100);
        if (ap.length() > 5) {
            ap = ap.substring(0, 5);
        }
        examLabel.setText(correct + " / " + total + " = " + (cp) + "% correct, " + almost + " / " + total + " = " + (ap) + "% almost");
    }

    private void updateStates() {
        // Start button
        boolean isStart = false;
        for (Component comp : topicPanel.getComponents()) {
            if (comp instanceof JCheckBox && ((JCheckBox) comp).isSelected()) {
                isStart = true;
                break;
            }
        }
        startButton.setEnabled(isStart);

        // Exam button
        boolean isExam = examMode.isSelected();
        boolean allTextFieldsAreFilled = mainPanel.getComponents().length > 0;
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JTextField && ((JTextField) comp).getText().trim().equals("")) {
                allTextFieldsAreFilled = false;
                break;
            }
        }
        limitInput.setVisible(isExam);
        checkButton.setEnabled(isExam && allTextFieldsAreFilled);

        // Hint button
        boolean isHint = curInput != null && !isExam;
        hintButton.setEnabled(isHint);
    }

    /**
     * Return table layout sizes.
     * 
     * @param cols
     * @param rows
     * @param defaultCol
     * @param defaultRow
     * @return
     */
    private double [][] getTableLayoutSize(int cols, int rows, double defaultCol, double defaultRow) {
        double [][] sizes = new double [2] [];
        sizes[0] = new double [cols];
        sizes[1] = new double [rows];

        for (int i = 0; i < sizes[0].length; i++) {
            sizes[0][i] = defaultCol;
        }
        for (int i = 0; i < sizes[1].length; i++) {
            sizes[1][i] = defaultRow;
        }

        return sizes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        checkEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
     */
    @Override
    public void caretUpdate(CaretEvent event) {
        checkEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    @Override
    public void focusGained(FocusEvent event) {
        curInput = (JTextComponent) event.getSource();
        updateStates();
    }

    @Override
    public void focusLost(FocusEvent event) {
    }

    /**
     * @param event
     */
    private void checkEvent(EventObject event) {
        if (event.getSource() instanceof JComponent) {
            JComponent comp = (JComponent) event.getSource();
            String name = comp.getName();

            if (name != null) {
                if (name.equals("start")) {
                    start();
                }
                else if (name.equals("hint")) {
                    hint();
                }
                else if (name.equals("edit")) {
                    edit();
                }
                else if (name.startsWith("course")) {
                    refreshLessonPanel();
                }
                else if (name.startsWith("lesson:")) {
                    refreshTopicPanel();
                }
                else if (name.startsWith("input")) {
                    if (!examMode.isSelected()) {
                        check(name);
                    }
                }
                else if (name.startsWith("check")) {
                    if (examMode.isSelected()) {
                        examCheck();
                    }
                }
                else if (name.equals("exam")) {
                    updateStates();
                }
                else if (name.startsWith("all")) {
                    select(name, true);
                }
                else if (name.startsWith("none")) {
                    select(name, false);
                }
                else if (name.startsWith("symbol")) {
                    String symbol = shift ? name.substring(7).toUpperCase() : name.substring(7);
                    addSymbol(symbol);
                }
                updateStates();
            }
        }
    }

    private void select(String name, boolean state) {

        // topicPanel
        Component [] comps = null;
        if (name.indexOf("lesson") != -1)
            comps = lessonPanel.getComponents();
        else if (name.indexOf("topic") != -1)
            comps = topicPanel.getComponents();
        else if (name.indexOf("course") != -1) comps = coursePanel.getComponents();

        if (comps != null) {
            for (Component comp : comps) {
                if (comp instanceof JCheckBox) {
                    ((JCheckBox) comp).setSelected(state);
                }
            }

            if (name.indexOf("course") != -1)
                refreshLessonPanel();
            else if (name.indexOf("lesson") != -1) refreshTopicPanel();
        }
    }

    private void addSymbol(String symbol) {
        if (curInput != null) {
            int pos = curInput.getCaretPosition();
            try {
                curInput.replaceSelection("");
                curInput.getDocument().insertString(pos, symbol, null);
            }
            catch (BadLocationException e) {
                // do nothing
            }
            curInputRequestFocus();

        }
    }

    /**
     * Request focus of the current input.
     */
    private void curInputRequestFocus() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                curInput.requestFocus();
            }
        });
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0) {
            shift = true;
        }
        if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
            try {
                if (e.getKeyCode() == 32) {
                    hint();
                }
                // System.out.println (e.getKeyCode());
                if ((e.getKeyCode() >= 48 && e.getKeyCode() <= 57) || e.getKeyCode() == 45) {
                    // figure out number
                    int num = e.getKeyCode() - 49;
                    if (e.getKeyCode() == 45) {
                        num = 10;
                    }
                    else if (num < 0) {
                        num = 9;
                    }
                    if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0) {
                        addSymbol(String.valueOf(specialChars.toUpperCase().charAt(num)));
                    }
                    else {
                        addSymbol(String.valueOf(specialChars.charAt(num)));
                    }
                }
            }
            catch (NumberFormatException nfEx) {} // do nothing
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getModifiers() != 1) {
            shift = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (event.getSource() instanceof JLabel) {
            JLabel label = (JLabel) event.getSource();
            String name = label.getName();

            if (name != null) {
                if (name.startsWith("hint:")) {
                    String key = name.substring(5);
                    label.setText(" (" + itemHintMap.get(key) + " )");
                }
                else if (name.startsWith("reverse:")) {
                    String key = name.substring(8);

                    JTextField inputTF = getInput("input:" + key);
                    if (inputTF != null) {
                        inputTF.setText(key);
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
