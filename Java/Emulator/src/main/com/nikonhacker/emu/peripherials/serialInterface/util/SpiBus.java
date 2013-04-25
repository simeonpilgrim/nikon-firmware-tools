package com.nikonhacker.emu.peripherials.serialInterface.util;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SpiSlaveDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a special version of SerialBus to simulate a SPI bus with one master and several slaves
 * The change is that it uses a special master partner that checks that exactly one slave device is selected and only
 * sends bytes to that device
 */
public class SpiBus extends SerialBus {
    public SpiBus(String teeName, SerialDevice masterDevice) {
        super(teeName, masterDevice);
    }

    protected InternalMasterPartner getInternalMasterPartner() {
        return new InternalMasterSpiSlavePartner();
    }

    /**
     * This is a special version of InternalMasterSpiPartner
     * The change is it checks that exactly one slave device is selected
     */
    private class InternalMasterSpiSlavePartner extends InternalMasterPartner {
        @Override
        public void write(Integer value) {
            // List selected devices
            List<InternalSlavePartner> partnersOfSelectedSlaves = new ArrayList<>();
            for (InternalSlavePartner internalSlavePartner : internalSlavePartners) {
                if (((SpiSlaveDevice) internalSlavePartner.getConnectedSerialDevice()).isSelected()) {
                    partnersOfSelectedSlaves.add(internalSlavePartner);
                }
            }
            // Write if exactly one, otherwise warn
            switch (partnersOfSelectedSlaves.size()) {
                case 0:
                    throw new RuntimeException("Tring to write serial value 0x" + Format.asHex(value, 2) + " while no slave device is selected !");
                case 1:
                    partnersOfSelectedSlaves.get(0).reverseWrite(value);
                    break;
                default:
                    String msg="";
                    for (InternalSlavePartner partnerOfSelectedSlaves : partnersOfSelectedSlaves) {
                        msg += partnerOfSelectedSlaves.getConnectedSerialDevice() + " ";
                    }
                    throw new RuntimeException("Tring to write serial value 0x" + Format.asHex(value, 2) + " while more than slave device is selected (" + msg.trim() + ")!");
            }
        }
    }
}
