package com.nikonhacker.gui;

import com.nikonhacker.emu.EmulationException;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.TrackingMemoryActivityListener;

import javax.swing.*;
import java.io.IOException;

public class MemoryMapComponentTest {
    public static void main(String[] args) throws EmulationException, IOException {
        new MemoryMapComponentTest();
    }

    private MemoryMapComponentTest() throws IOException, EmulationException {
        DebuggableMemory memory = new DebuggableMemory(false);
        TrackingMemoryActivityListener activityListener = new TrackingMemoryActivityListener(memory.getNumPages(), memory.getPageSize());

        // Create a frame
        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Emulator memory viewer");

        MemoryMapComponent memoryMapComponent = new MemoryMapComponent(activityListener, MemoryMapComponent.PAGE_MODE, null);
        frame.getContentPane().add(memoryMapComponent);
        frame.pack();                          // Layout components
        frame.setLocationRelativeTo(null);     // Center window.

        frame.setVisible(true);
        
        for(;;) {
            for(int i = 0; i < activityListener.getPageActivityMap().length; i++) {
                activityListener.getPageActivityMap()[i] = (int) (Math.random() * Integer.MAX_VALUE);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
