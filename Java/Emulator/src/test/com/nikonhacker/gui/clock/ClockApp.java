package com.nikonhacker.gui.clock;

import javax.swing.*;
import java.awt.*;

public class ClockApp {
    private Clock _clock;                        // Our clock component.

    //================================================================= main
    public static void main(String[] args) {
        new ClockApp().run();
    }

    private void run() {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setTitle("Analog Clock");
        ClockAnalogBuf contentPane = new ClockAnalogBuf();
        window.setContentPane(contentPane);
        window.pack();                          // Layout components
        window.setLocationRelativeTo(null);     // Center window.
        window.setVisible(true);
    }

    //========================================================== constructor
    class ClockAnalogBuf extends JComponent {
        public ClockAnalogBuf() {
            //... Create an instance of our new clock component.
            _clock = new Clock();

            //... Set the applet's layout and add the clock to it.
            setLayout(new BorderLayout());
            add(_clock, BorderLayout.CENTER);

            //... Start the clock running.
            start();
        }

        //=============================================================== start
        public void start() {
            _clock.start();
        }

        //================================================================ stop
        public void stop() {
            _clock.stop();
        }
    }
}
