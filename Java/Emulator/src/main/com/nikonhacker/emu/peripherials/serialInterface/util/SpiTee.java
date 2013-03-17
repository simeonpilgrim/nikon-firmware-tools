package com.nikonhacker.emu.peripherials.serialInterface.util;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SpiSlaveDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a special version of SerialTee
 * The change is that it uses a special A partner that checks that exactly one "B" device is selected
 */
public class SpiTee extends SerialTee {
    public SpiTee(String teeName, SerialDevice aDevice) {
        super(teeName, aDevice);
    }

    protected InternalAPartner getInternalAPartner() {
        return new InternalASpiSlavePartner();
    }

    /**
     * This is a special version of InternalASpiPartner
     * The change it checks that exactly one "B" device is selected
     */
    private class InternalASpiSlavePartner extends InternalAPartner {
        @Override
        public void write(Integer value) {
            // List selected devices
            List<InternalBPartner> partnersOfSelectedBDevices = new ArrayList<>();
            for (InternalBPartner internalBPartner : internalBPartners) {
                if (((SpiSlaveDevice) internalBPartner.getConnectedSerialDevice()).isSelected()) {
                    partnersOfSelectedBDevices.add(internalBPartner);
                }
            }
            // Write if exactly one, otherwise warn
            switch (partnersOfSelectedBDevices.size()) {
                case 0:
                    throw new RuntimeException("Tring to write serial value 0x" + Format.asHex(value, 2) + " while no connected device is selected !");
                case 1:
                    partnersOfSelectedBDevices.get(0).reverseWrite(value);
                    break;
                default:
                    String msg="";
                    for (InternalBPartner partnersOfSelectedBDevice : partnersOfSelectedBDevices) {
                        msg += partnersOfSelectedBDevice.getConnectedSerialDevice() + " ";
                    }
                    throw new RuntimeException("Tring to write serial value 0x" + Format.asHex(value, 2) + " while more than connected device is selected (" + msg.trim() + ")!");
            }
        }
    }
}
