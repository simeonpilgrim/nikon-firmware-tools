package com.nikonhacker.gui.component.image;

import javax.swing.*;
import java.awt.*;

public class BackgroundImagePanel extends JPanel {
    Image backgroundImage = null;

    public BackgroundImagePanel(Image backgroundImage) {
        super();
        setImage(backgroundImage);
    }

    public BackgroundImagePanel(LayoutManager manager, Image backgroundImage) {
        super(manager);
        setImage(backgroundImage);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //int imwidth = backgroundImage.getWidth(null);
        //int imheight = backgroundImage.getHeight(null);
        g.drawImage(backgroundImage, 1, 1, null);
    }

    private void setImage(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(this.backgroundImage, 0);
        try {
            mt.waitForAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
