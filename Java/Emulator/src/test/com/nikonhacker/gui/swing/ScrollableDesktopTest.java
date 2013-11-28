package com.nikonhacker.gui.swing;

/**
 * Unknown author
 * Taken from http://www.java2s.com/Tutorial/Java/0240__Swing/extendsJDesktopPaneimplementsScrollable.htm
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ScrollableDesktopTest extends JFrame {

    private JScrollPane scrollPane;

    public static void main(String arg[]) {
        UIManager.put("SplitPane.background", new Color(0xff8080ff));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        ScrollableDesktopTest frame = new ScrollableDesktopTest();

        //Display the window.
        frame.setVisible(true);
    }

    public ScrollableDesktopTest() {
        super("ScrollableDesktopTest");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setJMenuBar(createMenuBar());

        JDesktopPane test = createAndPopulateScrollableDesktop();
        test.setBackground(Color.RED);
        test.setOpaque(true);

        JPanel forcedStretchPanel = new JPanel(new BorderLayout());
        forcedStretchPanel.add(test, BorderLayout.CENTER);

        JPanel contentPane1 = createTestContentPane(forcedStretchPanel);
        JPanel contentPane2 = createTestContentPane(createAndPopulateScrollableDesktop());


        JSplitPane contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, contentPane1, contentPane2);
        setContentPane(contentPane);

        setPreferredSize(new Dimension(600, 300));

        pack();
    }

    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem jMenuItem = new JMenuItem("Decode firmware");
        fileMenu.add(jMenuItem);
        fileMenu.add(new JSeparator());
        jMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.err.println("Hello");
            }
        });

        return menuBar;
    }

    private JPanel createTestContentPane(JComponent scrollableDesktop) {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(createToolBar(), BorderLayout.NORTH);
        scrollPane = new JScrollPane(scrollableDesktop);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        JLabel statusBar = new JLabel("status");
        statusBar.setOpaque(true);
        statusBar.setBackground(Color.GREEN);
        contentPane.add(statusBar, BorderLayout.SOUTH);
        return contentPane;
    }

    private JPanel createToolBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new ModifiedFlowLayout(FlowLayout.LEFT, 0, 0));
        bar.add(new JButton("Load image"));
        bar.add(Box.createRigidArea(new Dimension(10, 0)));
        bar.add(new JButton("Play"));
        return bar;
    }

    private JDesktopPane createAndPopulateScrollableDesktop() {
        JDesktopPane scrollableDesktop = new ScrollableDesktop();
        scrollableDesktop.add(getInternalFrame("Frame 1", "This is frame f1"), new Integer(10));
        scrollableDesktop.add(getInternalFrame("Frame 2", "Content for f2"), new Integer(20));
        JInternalFrame f3 = getInternalFrame("Frame 3", "Content for f3");
        scrollableDesktop.add(f3, new Integer(30));
        return scrollableDesktop;
    }

    private JInternalFrame getInternalFrame(String title, String text) {
        JInternalFrame f1 = new JInternalFrame(title);
        f1.getContentPane().add(new JLabel(text));
        f1.setResizable(true);
        f1.pack();
        f1.setVisible(true);
        return f1;
    }

}