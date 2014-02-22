package com.nikonhacker.gui.component.serialInterface;

import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St950x0;
import com.nikonhacker.emu.peripherials.serialInterface.lcd.LcdDriver;
import com.nikonhacker.emu.peripherials.serialInterface.fMount.FMountCircuit;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.serialInterface.eeprom.EepromSerialPanel;
import com.nikonhacker.gui.component.serialInterface.viewfinderLcd.LcdSerialPanel;
import com.nikonhacker.gui.component.serialInterface.fMount.FMountSerialPanel;

public class SerialPanelFactory {
    public static SerialDevicePanel getSerialDevicePanel(SerialDevice serialDevice, EmulatorUI ui) {
        if (serialDevice instanceof St950x0) {
            return new EepromSerialPanel((St950x0)serialDevice, ui);
        }
        else if (serialDevice instanceof LcdDriver) {
            return new LcdSerialPanel((LcdDriver)serialDevice, ui);
        }
        else if (serialDevice instanceof FMountCircuit) {
            return new FMountSerialPanel((FMountCircuit)serialDevice, ui);
        }
        else return new RxTxSerialPanel(serialDevice);
    }
}
