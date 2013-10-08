package com.nikonhacker.emu.peripherials.frontPanel.tx;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.frontPanel.FrontPanel;

/**
 * This is a the model of a D5100 camera front panel.
 * This has nothing to do with the UI
 */
public class D5100FrontPanel extends FrontPanel {

    public D5100FrontPanel(Prefs prefs) {
        super(prefs);
        addCameraButton(KEY_PLUS_MINUS, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_TIMER, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_UP, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_POWER, new String[][]{{null, "on"}, {"highlight_off", "highlight_on"}}, false, new int[][]{{0, 1}});
        addCameraButton(KEY_FLASH, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_DOWN, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_LEFT, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_REC, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_DELETE, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_INFO, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_LIVEVIEW, new String[][]{{null, "on"}, {"highlight_off", "highlight_on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_PLAY, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_MENU, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_ZOOM_OUT, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_ZOOM_IN, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_OK, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_I, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_RIGHT, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_AEL_AFL, new String[][]{{null, "on"}, {"highlight", "on"}}, true, new int[][]{{1, 0}});

        addCameraButton(KEY_SHUTTER, new String[][]{{null, "half", "on"}, {"highlight", "highlight_half", "on"}}, false, new int[][]{{0, 1, 1}, {0, 0, 1}});

        addCameraButton(KEY_DIAL, new String[][]{{null, null}, {"highlight", "highlight"}}, true, new int[][]{{1, 0}});
        addCameraButton(KEY_MODEDIAL, new String[][]{
                {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"},
                {"0_highlight", "1_highlight", "2_highlight", "3_highlight", "4_highlight", "5_highlight", "6_highlight", "7_highlight", "8_highlight", "9_highlight", "10_highlight", "11_highlight", "12_highlight"}},
                false, new int[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}});

        // addLed(panel, "led", bgWidth, bgHeight);

        // TODO initialize values from ui.getPrefs();
        // TODO when do we save values ?
    }

}


