// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Main.java

package com.nikonhacker.gui;

import eu.hansolo.steelseries.extras.AirCompass;
import eu.hansolo.steelseries.extras.Altimeter;
import eu.hansolo.steelseries.extras.Battery;
import eu.hansolo.steelseries.extras.Clock;
import eu.hansolo.steelseries.extras.Compass;
import eu.hansolo.steelseries.extras.Horizon;
import eu.hansolo.steelseries.extras.Indicator;
import eu.hansolo.steelseries.extras.Led;
import eu.hansolo.steelseries.extras.Level;
import eu.hansolo.steelseries.extras.Radar;
import eu.hansolo.steelseries.extras.StopWatch;
import eu.hansolo.steelseries.extras.WindDirection;
import eu.hansolo.steelseries.gauges.DigitalRadial;
import eu.hansolo.steelseries.gauges.DisplayCircular;
import eu.hansolo.steelseries.gauges.RadialCounter;
import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.ForegroundType;
import eu.hansolo.steelseries.tools.FrameDesign;
import eu.hansolo.steelseries.tools.KnobStyle;
import eu.hansolo.steelseries.tools.KnobType;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.LedColor;
import eu.hansolo.steelseries.tools.LedType;
import eu.hansolo.steelseries.tools.PointerType;
import eu.hansolo.steelseries.tools.SymbolType;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class TestLed extends JFrame
        implements ActionListener
{

    public TestLed()
    {
        initComponents();
        SwingUtilities.invokeLater(new Runnable() {

            public void run()
            {
                clock1.setAutomatic(true);
                radar1.animate(true);
                stopWatch1.start();
                windDirection1.setValueCoupled(true);
                led1.setLedBlinking(true);
                led2.setLedBlinking(true);
                led3.setLedBlinking(true);
            }

            final TestLed this$0;

            {
                this$0 = TestLed.this;
                super();
            }
        }
        );
        TIMER.start();
    }

    private void initComponents()
    {
        jPanel2 = new JPanel();
        compass1 = new Compass();
        radar1 = new Radar();
        windDirection1 = new WindDirection();
        clock1 = new Clock();
        level1 = new Level();
        horizon1 = new Horizon();
        radialCounter1 = new RadialCounter();
        displayCircular1 = new DisplayCircular();
        altimeter1 = new Altimeter();
        airCompass1 = new AirCompass();
        digitalRadial1 = new DigitalRadial();
        stopWatch1 = new StopWatch();
        jPanel3 = new JPanel();
        battery1 = new Battery();
        led1 = new Led();
        led2 = new Led();
        led3 = new Led();
        indicator1 = new Indicator();
        indicator2 = new Indicator();
        indicator3 = new Indicator();
        indicator4 = new Indicator();
        buttonExit = new JButton();
        setDefaultCloseOperation(3);
        setTitle("SteelSeries Extras");
        setBackground(new Color(0, 0, 0));
        setResizable(false);
        compass1.setBackgroundColor(BackgroundColor.BROWN);
        compass1.setFrameDesign(FrameDesign.BRASS);
        compass1.setKnobStyle(KnobStyle.BRASS);
        compass1.setKnobType(KnobType.METAL_KNOB);
        compass1.setPointerType(PointerType.TYPE2);
        GroupLayout compass1Layout = new GroupLayout(compass1);
        compass1.setLayout(compass1Layout);
        compass1Layout.setHorizontalGroup(compass1Layout.createParallelGroup(1).add(0, 150, 32767));
        compass1Layout.setVerticalGroup(compass1Layout.createParallelGroup(1).add(0, 150, 32767));
        radar1.setFrameDesign(FrameDesign.TILTED_GRAY);
        GroupLayout radar1Layout = new GroupLayout(radar1);
        radar1.setLayout(radar1Layout);
        radar1Layout.setHorizontalGroup(radar1Layout.createParallelGroup(1).add(0, 150, 32767));
        radar1Layout.setVerticalGroup(radar1Layout.createParallelGroup(1).add(0, 150, 32767));
        windDirection1.setBackgroundColor(BackgroundColor.WHITE);
        windDirection1.setDigitalFont(true);
        windDirection1.setFrameDesign(FrameDesign.BRASS);
        windDirection1.setKnobStyle(KnobStyle.BRASS);
        windDirection1.setLcdColor(LcdColor.BLUE2_LCD);
        GroupLayout windDirection1Layout = new GroupLayout(windDirection1);
        windDirection1.setLayout(windDirection1Layout);
        windDirection1Layout.setHorizontalGroup(windDirection1Layout.createParallelGroup(1).add(0, 150, 32767));
        windDirection1Layout.setVerticalGroup(windDirection1Layout.createParallelGroup(1).add(0, 150, 32767));
        clock1.setAutomatic(true);
        clock1.setBackgroundColor(BackgroundColor.CARBON);
        clock1.setFrameDesign(FrameDesign.SHINY_METAL);
        clock1.setPointerColor(ColorDef.WHITE);
        clock1.setPointerType(PointerType.TYPE2);
        clock1.setSecondMovesContinuous(true);
        GroupLayout clock1Layout = new GroupLayout(clock1);
        clock1.setLayout(clock1Layout);
        clock1Layout.setHorizontalGroup(clock1Layout.createParallelGroup(1).add(0, 150, 32767));
        clock1Layout.setVerticalGroup(clock1Layout.createParallelGroup(1).add(0, 150, 32767));
        level1.setBackgroundColor(BackgroundColor.PUNCHED_SHEET);
        level1.setFrameDesign(FrameDesign.TILTED_BLACK);
        GroupLayout level1Layout = new GroupLayout(level1);
        level1.setLayout(level1Layout);
        level1Layout.setHorizontalGroup(level1Layout.createParallelGroup(1).add(0, 150, 32767));
        level1Layout.setVerticalGroup(level1Layout.createParallelGroup(1).add(0, 150, 32767));
        horizon1.setFrameDesign(FrameDesign.ANTHRACITE);
        GroupLayout horizon1Layout = new GroupLayout(horizon1);
        horizon1.setLayout(horizon1Layout);
        horizon1Layout.setHorizontalGroup(horizon1Layout.createParallelGroup(1).add(0, 150, 32767));
        horizon1Layout.setVerticalGroup(horizon1Layout.createParallelGroup(1).add(0, 150, 32767));
        radialCounter1.setBackgroundColor(BackgroundColor.MUD);
        radialCounter1.setForegroundType(ForegroundType.FG_TYPE2);
        radialCounter1.setFrameDesign(FrameDesign.SHINY_METAL);
        GroupLayout radialCounter1Layout = new GroupLayout(radialCounter1);
        radialCounter1.setLayout(radialCounter1Layout);
        radialCounter1Layout.setHorizontalGroup(radialCounter1Layout.createParallelGroup(1).add(0, 150, 32767));
        radialCounter1Layout.setVerticalGroup(radialCounter1Layout.createParallelGroup(1).add(0, 150, 32767));
        displayCircular1.setBackgroundColor(BackgroundColor.BEIGE);
        displayCircular1.setFrameDesign(FrameDesign.CHROME);
        displayCircular1.setLcdColor(LcdColor.STANDARD_LCD);
        GroupLayout displayCircular1Layout = new GroupLayout(displayCircular1);
        displayCircular1.setLayout(displayCircular1Layout);
        displayCircular1Layout.setHorizontalGroup(displayCircular1Layout.createParallelGroup(1).add(0, 150, 32767));
        displayCircular1Layout.setVerticalGroup(displayCircular1Layout.createParallelGroup(1).add(0, 150, 32767));
        altimeter1.setForegroundType(ForegroundType.FG_TYPE3);
        altimeter1.setFrameDesign(FrameDesign.BLACK_METAL);
        GroupLayout altimeter1Layout = new GroupLayout(altimeter1);
        altimeter1.setLayout(altimeter1Layout);
        altimeter1Layout.setHorizontalGroup(altimeter1Layout.createParallelGroup(1).add(0, 150, 32767));
        altimeter1Layout.setVerticalGroup(altimeter1Layout.createParallelGroup(1).add(0, 150, 32767));
        airCompass1.setFrameDesign(FrameDesign.BLACK_METAL);
        GroupLayout airCompass1Layout = new GroupLayout(airCompass1);
        airCompass1.setLayout(airCompass1Layout);
        airCompass1Layout.setHorizontalGroup(airCompass1Layout.createParallelGroup(1).add(0, 150, 32767));
        airCompass1Layout.setVerticalGroup(airCompass1Layout.createParallelGroup(1).add(0, 150, 32767));
        digitalRadial1.setBackgroundColor(BackgroundColor.ANTHRACITE);
        digitalRadial1.setForegroundType(ForegroundType.FG_TYPE4);
        digitalRadial1.setFrameDesign(FrameDesign.STEEL);
        GroupLayout digitalRadial1Layout = new GroupLayout(digitalRadial1);
        digitalRadial1.setLayout(digitalRadial1Layout);
        digitalRadial1Layout.setHorizontalGroup(digitalRadial1Layout.createParallelGroup(1).add(0, 150, 32767));
        digitalRadial1Layout.setVerticalGroup(digitalRadial1Layout.createParallelGroup(1).add(0, 150, 32767));
        stopWatch1.setFrameDesign(FrameDesign.STEEL);
        GroupLayout stopWatch1Layout = new GroupLayout(stopWatch1);
        stopWatch1.setLayout(stopWatch1Layout);
        stopWatch1Layout.setHorizontalGroup(stopWatch1Layout.createParallelGroup(1).add(0, 150, 32767));
        stopWatch1Layout.setVerticalGroup(stopWatch1Layout.createParallelGroup(1).add(0, 150, 32767));
        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(1).add(2, jPanel2Layout.createSequentialGroup().add(42, 42, 42).add(jPanel2Layout.createParallelGroup(2).add(digitalRadial1, -2, 150, -2).add(airCompass1, -2, 150, -2).add(stopWatch1, -2, 150, -2)).addPreferredGap(1).add(jPanel2Layout.createParallelGroup(1).add(displayCircular1, -2, 150, -2).add(radialCounter1, -2, 150, -2).add(altimeter1, -2, 150, -2)).addPreferredGap(1).add(jPanel2Layout.createParallelGroup(1).add(clock1, -2, 150, -2).add(level1, -2, 150, -2).add(horizon1, -2, 150, -2)).addPreferredGap(1).add(jPanel2Layout.createParallelGroup(1).add(jPanel2Layout.createParallelGroup(2).add(compass1, -2, 150, -2).add(radar1, -2, 150, -2)).add(windDirection1, -2, 150, -2)).addContainerGap()));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(1).add(jPanel2Layout.createSequentialGroup().addContainerGap().add(jPanel2Layout.createParallelGroup(1).add(jPanel2Layout.createParallelGroup(1, false).add(2, jPanel2Layout.createSequentialGroup().add(compass1, -2, 150, -2).addPreferredGap(0, -1, 32767).add(radar1, -2, 150, -2)).add(2, jPanel2Layout.createSequentialGroup().add(clock1, -2, 150, -2).addPreferredGap(0, -1, 32767).add(level1, -2, 150, -2)).add(jPanel2Layout.createSequentialGroup().add(altimeter1, -2, 150, -2).add(18, 18, 18).add(jPanel2Layout.createParallelGroup(1).add(digitalRadial1, -2, 150, -2).add(displayCircular1, -2, 150, -2)))).add(airCompass1, -2, 150, -2)).add(18, 18, 18).add(jPanel2Layout.createParallelGroup(1).add(horizon1, -2, 150, -2).add(radialCounter1, -2, 150, 32767).add(windDirection1, -2, 150, -2).add(stopWatch1, -2, 150, -2)).addContainerGap()));
        GroupLayout battery1Layout = new GroupLayout(battery1);
        battery1.setLayout(battery1Layout);
        battery1Layout.setHorizontalGroup(battery1Layout.createParallelGroup(1).add(0, 50, 32767));
        battery1Layout.setVerticalGroup(battery1Layout.createParallelGroup(1).add(0, 22, 32767));
        GroupLayout led1Layout = new GroupLayout(led1);
        led1.setLayout(led1Layout);
        led1Layout.setHorizontalGroup(led1Layout.createParallelGroup(1).add(0, 24, 32767));
        led1Layout.setVerticalGroup(led1Layout.createParallelGroup(1).add(0, 24, 32767));
        led2.setLedColor(LedColor.ORANGE_LED);
        led2.setLedType(LedType.RECT_VERTICAL);
        GroupLayout led2Layout = new GroupLayout(led2);
        led2.setLayout(led2Layout);
        led2Layout.setHorizontalGroup(led2Layout.createParallelGroup(1).add(0, 24, 32767));
        led2Layout.setVerticalGroup(led2Layout.createParallelGroup(1).add(0, 24, 32767));
        led3.setLedColor(LedColor.GREEN_LED);
        led3.setLedType(LedType.RECT_HORIZONTAL);
        GroupLayout led3Layout = new GroupLayout(led3);
        led3.setLayout(led3Layout);
        led3Layout.setHorizontalGroup(led3Layout.createParallelGroup(1).add(0, 24, 32767));
        led3Layout.setVerticalGroup(led3Layout.createParallelGroup(1).add(0, 24, 32767));
        indicator1.setBackgroundColor(BackgroundColor.ANTHRACITE);
        indicator1.setForegroundType(ForegroundType.FG_TYPE4);
        indicator1.setForegroundVisible(false);
        indicator1.setFrameDesign(FrameDesign.BLACK_METAL);
        indicator1.setOn(true);
        indicator1.setSymbolType(SymbolType.OIL);
        GroupLayout indicator1Layout = new GroupLayout(indicator1);
        indicator1.setLayout(indicator1Layout);
        indicator1Layout.setHorizontalGroup(indicator1Layout.createParallelGroup(1).add(0, 50, 32767));
        indicator1Layout.setVerticalGroup(indicator1Layout.createParallelGroup(1).add(0, 50, 32767));
        indicator2.setBackgroundColor(BackgroundColor.ANTHRACITE);
        indicator2.setForegroundVisible(false);
        indicator2.setFrameDesign(FrameDesign.BLACK_METAL);
        indicator2.setOn(true);
        indicator2.setOnColor(ColorDef.BLUE);
        indicator2.setSymbolType(SymbolType.FULL_BEAM);
        GroupLayout indicator2Layout = new GroupLayout(indicator2);
        indicator2.setLayout(indicator2Layout);
        indicator2Layout.setHorizontalGroup(indicator2Layout.createParallelGroup(1).add(0, 50, 32767));
        indicator2Layout.setVerticalGroup(indicator2Layout.createParallelGroup(1).add(0, 50, 32767));
        indicator3.setBackgroundColor(BackgroundColor.ANTHRACITE);
        indicator3.setForegroundVisible(false);
        indicator3.setFrameDesign(FrameDesign.BLACK_METAL);
        indicator3.setOnColor(ColorDef.YELLOW);
        indicator3.setSymbolType(SymbolType.FOG_LIGHT);
        GroupLayout indicator3Layout = new GroupLayout(indicator3);
        indicator3.setLayout(indicator3Layout);
        indicator3Layout.setHorizontalGroup(indicator3Layout.createParallelGroup(1).add(0, 50, 32767));
        indicator3Layout.setVerticalGroup(indicator3Layout.createParallelGroup(1).add(0, 50, 32767));
        indicator4.setBackgroundColor(BackgroundColor.ANTHRACITE);
        indicator4.setForegroundVisible(false);
        indicator4.setFrameDesign(FrameDesign.BLACK_METAL);
        indicator4.setSymbolType(SymbolType.SLICKNESS);
        GroupLayout indicator4Layout = new GroupLayout(indicator4);
        indicator4.setLayout(indicator4Layout);
        indicator4Layout.setHorizontalGroup(indicator4Layout.createParallelGroup(1).add(0, 50, 32767));
        indicator4Layout.setVerticalGroup(indicator4Layout.createParallelGroup(1).add(0, 50, 32767));
        buttonExit.setText("Exit");
        buttonExit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt)
            {
                actionHandler(evt);
            }

            final TestLed this$0;


            {
                this$0 = TestLed.this;
                super();
            }
        }
        );
        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(1).add(jPanel3Layout.createSequentialGroup().addContainerGap().add(jPanel3Layout.createParallelGroup(2, false).add(1, battery1, -1, -1, 32767).add(1, led1, -2, -1, -2).add(1, led2, -2, -1, -2).add(1, led3, -2, -1, -2).add(1, indicator3, -1, -1, 32767).add(1, indicator1, -1, -1, 32767).add(1, indicator4, -1, -1, 32767).add(1, indicator2, -1, -1, 32767)).addContainerGap(22, 32767)).add(buttonExit, -1, 92, 32767));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(1).add(jPanel3Layout.createSequentialGroup().addContainerGap().add(battery1, -2, -1, -2).add(31, 31, 31).add(led1, -2, -1, -2).addPreferredGap(1).add(led2, -2, -1, -2).add(18, 18, 18).add(led3, -2, -1, -2).add(18, 18, 18).add(indicator1, -2, -1, -2).addPreferredGap(1).add(indicator4, -2, -1, -2).add(16, 16, 16).add(indicator2, -2, -1, -2).addPreferredGap(1).add(indicator3, -2, -1, -2).add(30, 30, 30).add(buttonExit).addContainerGap(28, 32767)));
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(1).add(layout.createSequentialGroup().add(jPanel3, -2, -1, -2).add(0, 0, 0).add(jPanel2, -1, 704, 32767)));
        layout.setVerticalGroup(layout.createParallelGroup(1).add(jPanel2, -1, -1, 32767).add(jPanel3, -1, -1, 32767));
        pack();
    }

    private void actionHandler(ActionEvent evt)
    {
        System.exit(0);
    }

    public void actionPerformed(ActionEvent event)
    {
        if(event.getSource().equals(TIMER))
        {
            airCompass1.setValueAnimated(Math.random() * 360D);
            compass1.setValueAnimated(Math.random() * 360D);
            altimeter1.setValueAnimated(Math.random() * 10000D);
            digitalRadial1.setValueAnimated(Math.random() * 100D);
            displayCircular1.setLcdValue(Math.random() * 100D);
            horizon1.setPitchAnimated(Math.random() * 90D);
            horizon1.setRollAnimated(Math.random() * 45D);
            level1.setValueAnimated(Math.random() * 360D);
            radialCounter1.setValueAnimated(Math.random() * 100D);
            windDirection1.setValueAnimated(Math.random() * 360D);
            windDirection1.setValue2(Math.random() * 360D);
            int value = battery1.getValue();
            if((value += 5) > 100)
                value = 0;
            battery1.setValue(value);
            indicator1.setOn(!indicator1.isOn());
            indicator2.setOn(!indicator2.isOn());
            indicator3.setOn(!indicator3.isOn());
            indicator4.setOn(!indicator4.isOn());
        }
    }

    public static void main(String args[])
    {
        try
        {
            javax.swing.UIManager.LookAndFeelInfo arr$[] = UIManager.getInstalledLookAndFeels();
            int len$ = arr$.length;
            int i$ = 0;
            do
            {
                if(i$ >= len$)
                    break;
                javax.swing.UIManager.LookAndFeelInfo info = arr$[i$];
                if("Nimbus".equals(info.getName()))
                {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
                i$++;
            } while(true);
        }
        catch(ClassNotFoundException ex)
        {
            Logger.getLogger(test2/ TestLed.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch(InstantiationException ex)
        {
            Logger.getLogger(test2/ TestLed.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch(IllegalAccessException ex)
        {
            Logger.getLogger(test2/ TestLed.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch(UnsupportedLookAndFeelException ex)
        {
            Logger.getLogger(test2/ TestLed.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        EventQueue.invokeLater(new Runnable() {

            public void run()
            {
                TestLed APP = new TestLed();
                APP.setLocationRelativeTo(null);
                APP.setVisible(true);
            }

        }
        );
    }

    private final Timer TIMER = new Timer(3000, this);
    private AirCompass airCompass1;
    private Altimeter altimeter1;
    private Battery battery1;
    private JButton buttonExit;
    private Clock clock1;
    private Compass compass1;
    private DigitalRadial digitalRadial1;
    private DisplayCircular displayCircular1;
    private Horizon horizon1;
    private Indicator indicator1;
    private Indicator indicator2;
    private Indicator indicator3;
    private Indicator indicator4;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private Led led1;
    private Led led2;
    private Led led3;
    private Level level1;
    private Radar radar1;
    private RadialCounter radialCounter1;
    private StopWatch stopWatch1;
    private WindDirection windDirection1;








}
