package com.nikonhacker.gui;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.dfr.Format;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @deprecated
 * @see CPUStateEditorFrame
 */
public class CPUStateFrame extends DocumentFrame {

    private static final int UPDATE_INTERVAL_MS = 100; // 10fps
    private static final int WINDOW_WIDTH = 280;
    private static final int WINDOW_HEIGHT = 320;
    private static final int LABEL0_X = 5;
    private static final int VALUE0_X = 45;
    private static final int LABEL1_X = 160;
    private static final int VALUE1_X = 200;

    private Timer _timer;

    private CPUState cpuState;

    public CPUStateFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, CPUState cpuState, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.cpuState = cpuState;

        getContentPane().add(new CPUStateComponent());
        
        // Start update timer
        _timer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        _timer.start();
    }

    public void dispose() {
        _timer.stop();
        _timer = null;
        super.dispose();
    }

    private class CPUStateComponent extends JComponent {
        private CPUStateComponent() {
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        }

        // This method is called whenever the contents needs to be painted
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

            g2d.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

            int lineHeight = g.getFontMetrics().getHeight();
            int h;


            // PC
            h = lineHeight;
            g2d.drawString("PC", LABEL0_X, h);
            g2d.drawString("0x" + Format.asHex(cpuState.pc, 8), VALUE0_X, h);

            // ILM
            h += lineHeight * 2;
            g2d.drawString("     43210", VALUE0_X, h);
            h += lineHeight;
            g2d.drawString("ILM", LABEL0_X, h);
            g2d.drawString("   0b" + Format.asBinary(cpuState.getILM(), 5), VALUE0_X, h);

            // SCR
            h += lineHeight * 2;
            g2d.drawString("      D10T", VALUE0_X, h);
            h += lineHeight;
            g2d.drawString("SCR", LABEL0_X, h);
            g2d.drawString("     0b" + Format.asBinary(cpuState.getSCR(), 3), VALUE0_X, h);

            // CCR
            h += lineHeight * 2;
            g2d.drawString("    SINZVC", VALUE0_X, h);
            h += lineHeight;
            g2d.drawString("CCR", LABEL0_X, h);
            g2d.drawString("  0b" + Format.asBinary(cpuState.getCCR(), 6), VALUE0_X, h);

            // TBR
            h += lineHeight * 2;
            g2d.drawString(CPUState.REG_LABEL[CPUState.TBR], LABEL0_X, h);
            g2d.drawString("0x" + Format.asHex(cpuState.getReg(CPUState.TBR), 8), VALUE0_X, h);

            // RP
            h += lineHeight * 2;
            g2d.drawString(CPUState.REG_LABEL[CPUState.RP], LABEL0_X, h);
            g2d.drawString("0x" + Format.asHex(cpuState.getReg(CPUState.RP), 8), VALUE0_X, h);

            // SSP/USP
            h += lineHeight * 2;
            g2d.drawString(CPUState.REG_LABEL[CPUState.SSP], LABEL0_X, h);
            g2d.drawString("0x" + Format.asHex(cpuState.getReg(CPUState.SSP), 8), VALUE0_X, h);
            h += lineHeight ;
            g2d.drawString(CPUState.REG_LABEL[CPUState.USP], LABEL0_X, h);
            g2d.drawString("0x" + Format.asHex(cpuState.getReg(CPUState.USP), 8), VALUE0_X, h);

            // MDH/MDL
            h += lineHeight * 2;
            g2d.drawString("MDH/L", LABEL0_X, h);
            g2d.drawString("0x" + Format.asHex(cpuState.getReg(CPUState.MDH), 8) + " " + Format.asHex(cpuState.getReg(CPUState.MDL), 8), VALUE0_X, h);

            // General purpose registers
            for (int i = 0; i < CPUState.DEDICATED_REG_OFFSET - 1; i++) {
                h = lineHeight * (i + 1);
                g2d.drawString(CPUState.REG_LABEL[i], LABEL1_X, h);
                g2d.drawString("0x" + Format.asHex(cpuState.getReg(i), 8), VALUE1_X, h);
            }

            h += lineHeight;
            if (cpuState.getS() == 1) {
                h += lineHeight;
            }
            g2d.drawString("= " + CPUState.REG_LABEL[15], LABEL1_X - 14, h);
        }

    }
}
