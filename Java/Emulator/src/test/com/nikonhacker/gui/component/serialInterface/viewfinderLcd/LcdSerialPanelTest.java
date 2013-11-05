package com.nikonhacker.gui.component.serialInterface.viewfinderLcd;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.ParsingException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LcdSerialPanelTest {
    public static void main(String[] args) throws IOException {

        File path = new File("Java\\Emulator\\src\\main\\com\\nikonhacker\\gui\\images\\viewfinder_lcd");

        // load source images
        BufferedImage image = ImageIO.read(new File(path, "off.png"));

        // create the new image, canvas size is the max. of both image sizes
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // paint both images, preserving the alpha channels
        Graphics g = result.getGraphics();
        g.drawImage(image, 0, 0, null);

        String input = "0x00 0x00 0x06 0x3D 0x0B 0xDF 0xD0 0x02 0x7A 0xFF 0x07 0xD3 0xD3 0xD4 0x00";
        String[] values = input.trim().split("[\\s,]+");
        int byteNumber = 0;
        for (String value : values) {
            if (byteNumber > 0 && byteNumber < 14) { // Ignore byte 0 and 14
                try {
                    int bValue = Format.parseUnsigned(value);
                    for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
                        if (Format.isBitSet(bValue, bitNumber)) {
                            File overlayFile = new File(path, byteNumber + "_" + bitNumber + ".png");
                            if (!overlayFile.exists()) {
                                System.err.println("Missing file: " + overlayFile.getName());
                            }
                            else {
                                BufferedImage overlay = ImageIO.read(overlayFile);
                                if (overlay.getWidth() != w || overlay.getHeight() != h) {
                                    System.err.println("Wrong file size for " + overlayFile.getName() + ": " + overlay.getWidth() + "x" + overlay.getHeight() + " instead of " + w + "x" + h);
                                }
                                else {
                                    g.drawImage(overlay, 0, 0, null);
                                }
                            }
                        }
                    }
                } catch (ParsingException e) {
                    System.err.println("Cannot parse value: " + value);
                }
            }
            byteNumber += 1;
        }

        // Save as new image
        ImageIO.write(result, "PNG", new File(path, "result1.png"));
    }
}
