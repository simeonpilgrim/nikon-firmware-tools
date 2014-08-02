package com.nikonhacker.gui.component.serialInterface;

import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St950x0;
import com.nikonhacker.emu.peripherials.serialInterface.fMount.FMountCircuit;
import com.nikonhacker.emu.peripherials.serialInterface.imageSensor.Imx071;
import com.nikonhacker.emu.peripherials.serialInterface.lcd.LcdDriver;
import com.nikonhacker.emu.peripherials.serialInterface.sensorBridge.Ei155;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.serialInterface.eeprom.EepromSerialPanel;
import com.nikonhacker.gui.component.serialInterface.fMount.FMountSerialPanel;
import com.nikonhacker.gui.component.serialInterface.imageSensor.ImageSensorSerialPanel;
import com.nikonhacker.gui.component.serialInterface.sensorBridge.SensorBridgeSerialPanel;
import com.nikonhacker.gui.component.serialInterface.viewfinderLcd.LcdSerialPanel;

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
        else if (serialDevice instanceof Imx071) {
            return new ImageSensorSerialPanel((Imx071)serialDevice, ui);
        }
        else if (serialDevice instanceof Ei155) {
            return new SensorBridgeSerialPanel((Ei155)serialDevice, ui);
        }
        else return new RxTxSerialPanel(serialDevice);
    }
}
