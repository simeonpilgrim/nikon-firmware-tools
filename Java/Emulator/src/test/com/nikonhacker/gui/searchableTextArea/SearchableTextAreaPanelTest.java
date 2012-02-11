package com.nikonhacker.gui.searchableTextArea;

import com.nikonhacker.gui.component.SearchableTextAreaPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Inspired by
 * http://coding.derkeiler.com/Archive/Java/comp.lang.java.programmer/2008-04/msg01467.html
 */
public class SearchableTextAreaPanelTest {

    private static void createAndShowGUI() {

        JFrame frame = new JFrame("TextSearchTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextArea textArea = new JTextArea(50, 80);
        textArea.setEditable(false);
        String text = "This little line had some data,\n" +
                      "And this little line had none.\n" +
                      "Chorus:\n" +
                      "data data data data\n";

        textArea.setText(text);
        for (int i = 0; i < 300; i++) {
            textArea.append(text);
        }
        JPanel panel = new JPanel(new BorderLayout());

        final SearchableTextAreaPanel searchableTextAreaPanel = new SearchableTextAreaPanel(textArea, false);
        panel.add(searchableTextAreaPanel, BorderLayout.CENTER);
        //panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(makeButton("Test1", searchableTextAreaPanel, "this", false, false));
        topPanel.add(makeButton("Test2", searchableTextAreaPanel, "this", false, false));
        topPanel.add(makeButton("Test3", searchableTextAreaPanel, "This", true, false));
        topPanel.add(makeButton("Test4", searchableTextAreaPanel, "this", true, true));

        panel.add(topPanel, BorderLayout.NORTH);


        frame.add(panel);

        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
    }

    private static JButton makeButton(String text, final SearchableTextAreaPanel searchableTextAreaPanel, final String searchText, final boolean matchCase, final boolean highlightAll) {
        JButton testButton = new JButton(text);
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchableTextAreaPanel.search(searchText, matchCase, highlightAll);
            }
        });
        return testButton;
    }

    public static void main(String args[]) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
