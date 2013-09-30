package com.nikonhacker.emu.peripherials.frontPanel.tx;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.frontPanel.FrontPanel;

public class D5100FrontPanel extends FrontPanel {
    public D5100FrontPanel(Prefs prefs) {
        super(prefs);
        addButton("+-", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("timer", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("up", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("power", new String[][]{{null, "on"}, {"highlight_off", "highlight_on"}}, false, false);
        addButton("flash", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("down", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("left", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("rec", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("delete", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("info", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("liveview", new String[][]{{null, "on"}, {"highlight_off", "highlight_on"}}, true, true);
        addButton("play", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("menu", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("zoomout", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("zoomin", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("ok", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("i", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("right", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);
        addButton("aelafl", new String[][]{{null, "on"}, {"highlight", "on"}}, true, true);

        addButton("shutter", new String[][]{{null, "half", "on"}, {"highlight", "highlight_half", "on"}}, false, true);

        addButton("dial", new String[][]{{null, null}, {"highlight", "highlight"}}, true, true);
        addButton("modedial", new String[][]{{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"}, {"0_highlight", "1_highlight", "2_highlight", "3_highlight", "4_highlight", "5_highlight", "6_highlight", "7_highlight", "8_highlight", "9_highlight", "10_highlight", "11_highlight", "12_highlight"}}, false, true);

        // addLed(panel, "led", bgWidth, bgHeight);

        // TODO initialize values from ui.getPrefs();
        // TODO when do we save values ?
    }

}


