package com.nikonhacker.gui.searchableTextArea;

import com.nikonhacker.gui.swing.SearchableTextAreaPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Inspired by
 * http://coding.derkeiler.com/Archive/Java/comp.lang.java.programmer/2008-04/msg01467.html
 */
public class SearchableTextAreaPanelTest {
    static String text = "This little line had some data,\n" +
            "And this little line had none.\n" +
            "Chorus:\n" +
            "data data data data\n";

    static String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris nisl odio, accumsan \n" +
            "ullamcorper malesuada eget, luctus commodo tellus. Aliquam erat volutpat. Morbi nec leo \n" +
            "ultricies tellus porttitor tempus nec id velit. Fusce tristique volutpat laoreet. \n" +
            "Vestibulum congue tincidunt turpis at luctus. Sed sit amet dolor eget nisl consequat \n" +
            "sodales vitae ut leo. Duis vitae orci nulla.\n" +
            "\n" +
            "Integer vitae justo id felis iaculis faucibus. Class aptent taciti sociosqu ad litora \n" +
            "torquent per conubia nostra, per inceptos himenaeos. Class aptent taciti sociosqu ad \n" +
            "litora torquent per conubia nostra, per inceptos himenaeos. Etiam nibh mi, aliquam nec \n" +
            "congue eu, suscipit sagittis augue. Sed lectus turpis, scelerisque et mattis vitae, \n" +
            "porttitor vehicula ipsum. In quis est mi. Sed molestie, lorem at laoreet dignissim, sem \n" +
            "nisl blandit dui, at euismod leo massa ut tortor. Integer egestas porttitor purus, nec \n" +
            "euismod dolor porta ut.\n" +
            "\n" +
            "Nulla sed mi lectus. Cras massa nisi, gravida a facilisis vitae, imperdiet eu eros. \n" +
            "Nunc at ligula tortor, a ornare dui. Aliquam dictum turpis nec dui luctus euismod. \n" +
            "Donec viverra scelerisque est sed vehicula. Aenean lorem orci, tempor quis molestie \n" +
            "quis, tempus sit amet lacus. Ut sit amet magna enim. Ut odio purus, fringilla ac \n" +
            "fermentum tincidunt, euismod quis neque. Aliquam erat volutpat. Quisque eu mi eu quam \n" +
            "blandit fermentum non sed libero.\n" +
            "\n" +
            "Pellentesque erat nunc, ornare eget eleifend sit amet, dapibus vitae lectus. Ut quis \n" +
            "dui id nisl adipiscing mattis. Sed vel est tempor nibh imperdiet auctor sit amet \n" +
            "consequat sem. Phasellus auctor hendrerit tincidunt. Nulla tempus justo in velit \n" +
            "porttitor a luctus orci sollicitudin. Pellentesque habitant morbi tristique senectus et \n" +
            "netus et malesuada fames ac turpis egestas. Pellentesque habitant morbi tristique \n" +
            "senectus et netus et malesuada fames ac turpis egestas. Vestibulum commodo nunc sit \n" +
            "amet turpis eleifend ut consectetur turpis suscipit. Cum sociis natoque penatibus et \n" +
            "magnis dis parturient montes, nascetur ridiculus mus.\n" +
            "\n" +
            "Mauris velit metus, varius vitae tincidunt eu, suscipit ut enim. Nam posuere metus a \n" +
            "ante consequat ullamcorper. Donec non lacus mi. Aliquam et ipsum non diam malesuada \n" +
            "aliquet sit amet id magna. Donec ullamcorper nibh id lectus tincidunt euismod. Fusce \n" +
            "eget dui est, eu vehicula tortor. Phasellus non risus risus, at gravida eros. In hac \n" +
            "habitasse platea dictumst. Vestibulum ante ipsum primis in faucibus orci luctus et \n" +
            "ultrices posuere cubilia Curae; Sed porttitor magna vitae diam pharetra eu facilisis \n" +
            "elit adipiscing. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis eu \n" +
            "tincidunt quam. Nullam mattis, leo sit amet volutpat posuere, lectus mauris pulvinar \n" +
            "odio, sed consequat metus nulla non purus. Sed accumsan nisl ac neque condimentum \n" +
            "suscipit. Mauris quis elit ac diam ullamcorper bibendum sed ut neque. Sed sit amet \n" +
            "lectus lacus, et semper augue.";

    private static void createAndShowGUI() {

        JFrame frame = new JFrame("TextSearchTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTextArea textArea = new JTextArea(50, 80);
        textArea.setEditable(false);

        textArea.setText(text);
        for (int i = 0; i < 300; i++) {
            textArea.append(text);
        }
        JPanel panel = new JPanel(new BorderLayout());

        final SearchableTextAreaPanel searchableTextAreaPanel = new SearchableTextAreaPanel(textArea, false);
        panel.add(searchableTextAreaPanel, BorderLayout.CENTER);
        //panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(makeButton("this,0,0", searchableTextAreaPanel, "this", false, false));
        topPanel.add(makeButton("this,0,1", searchableTextAreaPanel, "this", false, true));
        topPanel.add(makeButton("this,1,1", searchableTextAreaPanel, "this", true, true));
        topPanel.add(makeButton("This,1,0", searchableTextAreaPanel, "This", true, true));
        JButton loremIpsumButton = new JButton("Lorem Ipsum");
        loremIpsumButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea.setText(loremIpsum);
            }
        });
        
        topPanel.add(loremIpsumButton);

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
