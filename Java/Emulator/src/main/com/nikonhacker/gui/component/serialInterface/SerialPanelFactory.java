package com.nikonhacker.gui.component.serialInterface;

import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St950x0;
import com.nikonhacker.gui.component.serialInterface.eeprom.EepromSerialPanel;

public class SerialPanelFactory {
    public static SerialDevicePanel getSerialDevicePanel(SerialDevice serialDevice) {
        if (serialDevice instanceof St950x0) {
            return new EepromSerialPanel((St950x0)serialDevice);
        }
        return new RxTxSerialPanel(serialDevice);
    }
}
