package com.nikonhacker.gui.component.serialInterface;

import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St950x0;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.serialInterface.eeprom.EepromSerialPanel;

public class SerialPanelFactory {
    public static SerialDevicePanel getSerialDevicePanel(SerialDevice serialDevice, EmulatorUI ui) {
        if (serialDevice instanceof St950x0) {
            return new EepromSerialPanel((St950x0)serialDevice, ui);
        }
        return new RxTxSerialPanel(serialDevice);
    }
}
