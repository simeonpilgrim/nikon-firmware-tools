package com.nikonhacker.gui.component.screenEmulator;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.emu.peripherials.lcd.fr.FrLcd;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.DocumentFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ScreenEmulatorFrame extends DocumentFrame implements ActionListener {

    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private AffineTransform resizeTransform;
    private int previousW, previousH;

    private int yStart, uStart, vStart, yuvAlign, previousYuvAlign;
    private int screenHeight, screenWidth;

    private BufferedImage img;
    
    private FrLcd lcd;

    private Timer refreshTimer;
    private final JTextField yAddressField, uAddressField, vAddressField, widthField, heightField, yuvAlignField;

    public ScreenEmulatorFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, FrLcd lcd, int yStart, int uStart, int vStart, int screenWidth, int screenHeight) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.yStart = yStart;
        this.uStart = uStart;
        this.vStart = vStart;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.yuvAlign = screenWidth;
        this.lcd = lcd;

        JPanel selectionPanel = new JPanel();
        selectionPanel.add(new JLabel("Y = 0x"));
        yAddressField = new JTextField(Format.asHex(yStart, 8), 8);
        selectionPanel.add(yAddressField,BorderLayout.WEST);
        yAddressField.addActionListener(this);

        selectionPanel.add(new JLabel(" U = 0x"));
        uAddressField = new JTextField(Format.asHex(uStart, 8), 8);
        selectionPanel.add(uAddressField);
        uAddressField.addActionListener(this);

        selectionPanel.add(new JLabel(" V = 0x"));
        vAddressField = new JTextField(Format.asHex(vStart, 8), 8);
        selectionPanel.add(vAddressField);
        vAddressField.addActionListener(this);

        selectionPanel.add(new JLabel(" Width = "));
        widthField = new JTextField(String.format("%04d",screenWidth), 4);
        selectionPanel.add(widthField);
        widthField.addActionListener(this);

        selectionPanel.add(new JLabel(" Height = "));
        heightField = new JTextField(String.format("%04d", screenHeight), 4);
        selectionPanel.add(heightField);
        heightField.addActionListener(this);

        selectionPanel.add(new JLabel(" Align = "));
        yuvAlignField = new JTextField(String.format("%04d", yuvAlign), 4);
        selectionPanel.add(yuvAlignField);
        yuvAlignField.addActionListener(this);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(selectionPanel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(new ScreenEmulatorComponent(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        
        getContentPane().add(contentPanel);

        setPreferredSize(new Dimension(screenWidth, 600));

        // Start update timer
        refreshTimer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        refreshTimer.start();
    }

    public void dispose() {
        refreshTimer.stop();
        refreshTimer = null;
        super.dispose();
    }

    public void actionPerformed(ActionEvent e) {
        int width,align;
        
        this.yStart = Format.parseIntHexField(yAddressField);
        this.uStart = Format.parseIntHexField(uAddressField);
        this.vStart = Format.parseIntHexField(vAddressField);
        try {
            width = Format.parseUnsignedField(widthField);
            align = Format.parseUnsignedField(yuvAlignField);
            this.screenHeight = Format.parseUnsignedField(heightField);
        } catch(ParsingException excp) {
            throw new NumberFormatException("Wrong number format");
        }
        // width and alignment must be always even
        if ((width&0x1)!=0) {
            widthField.setBackground(Color.RED);
            throw new NumberFormatException("Image width must be even");
        } else {
            this.screenWidth = width;
        }
        if ((align&0x1)!=0) {
            yuvAlignField.setBackground(Color.RED);
            throw new NumberFormatException("Image alignment must be even");
        } else {
            this.yuvAlign = align;
        }
    }

    private class ScreenEmulatorComponent extends JComponent {

        private ScreenEmulatorComponent() {
            super();
            setPreferredSize(new Dimension(screenWidth, screenHeight));
        }

        // This method is called whenever the contents needs to be painted
        public void paintComponent(Graphics graphics) {
            Graphics2D g2d = (Graphics2D) graphics;

            if (img!=null) {
                if (img.getWidth()!=screenWidth || img.getHeight()!=screenHeight || yuvAlign != previousYuvAlign) {
                    img.flush();
                    img=null;
                    previousYuvAlign = yuvAlign;
                }
            }
            if (img==null)
                img = lcd.getImage(screenWidth, screenHeight);
            if (img!=null) {
                // Get size of JScrollPane
                int w = getParent().getWidth();
                int h = getParent().getHeight();
    
                // Create the resizing transform upon first call or resize
                if (resizeTransform == null || previousW != w || previousH != h) {
                    resizeTransform = new AffineTransform();
                    double scaleX = Math.max(0.5, (2 * w / screenWidth) / 2.0);
                    double scaleY = Math.max(0.5, (2 * h / screenHeight) / 2.0);
                    double scale = Math.min(scaleX, scaleY);
                    resizeTransform.scale(scale, scale);
                    previousW = w;
                    previousH = h;
                }
    
                lcd.updateImage(img, yStart, uStart, vStart, yuvAlign);
    
                g2d.drawImage(img, resizeTransform, null);
            }
        }
    }
}
