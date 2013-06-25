package com.nikonhacker.emu.peripherials.io;

import com.nikonhacker.emu.peripherials.ioPort.Pin;
import com.nikonhacker.emu.peripherials.ioPort.util.ConsoleLoggingIoWire;
import com.nikonhacker.emu.peripherials.ioPort.util.Diode;

import java.util.Random;

public class PinTest {
    public static void main(String[] args) {
        int value;
        System.out.println("Creating components SOURCE and TARGET");
        TwoPinComponent sourceComponent = new TwoPinComponent("SOURCE");
        TwoPinComponent targetComponent = new TwoPinComponent("TARGET");

        System.out.println();
        System.out.println("Testing SOURCE => TARGET direction (not connected)");
        value = new Random().nextInt();
        System.out.println("    Setting register of SOURCE to " + value);
        sourceComponent.setTx(value);
        System.out.println("    Register of TARGET is now: " + targetComponent.getRx());
        System.out.println("        => " + (value != targetComponent.getRx()?"OK":"ERROR"));


        System.out.println();
        System.out.println("Connecting SOURCE and TARGET directly.");
        Pin.interconnect(sourceComponent.getOutputPin(), targetComponent.getInputPin());

        System.out.println("Testing SOURCE => TARGET direction (with direct connection)");
        value = new Random().nextInt();
        System.out.println("    Setting register of SOURCE to " + value);
        sourceComponent.setTx(value);
        System.out.println("    Register of TARGET is now: " + targetComponent.getRx());
        System.out.println("        => " + (value == targetComponent.getRx() ? "OK" : "ERROR"));

        System.out.println("Testing TARGET => SOURCE direction (with direct connection)");
        value = new Random().nextInt();
        System.out.println("    Setting register of TARGET to " + value);
        targetComponent.setTx(value);
        System.out.println("    Register of TARGET is now: " + sourceComponent.getRx());
        System.out.println("        => " + (value != sourceComponent.getRx()?"OK":"ERROR"));


        System.out.println();
        System.out.println("Inserting SOURCE ->|- TARGET diode");
        Diode diode = new Diode("DIODE");
        diode.insertAtPin(sourceComponent.getOutputPin());

        System.out.println("Testing SOURCE => TARGET direction (with S->T diode)");
        value = new Random().nextInt();
        System.out.println("    Setting register of SOURCE to " + value);
        sourceComponent.setTx(value);
        System.out.println("    Register of TARGET is now: " + targetComponent.getRx());
        System.out.println("        => " + (value == targetComponent.getRx()?"OK":"ERROR"));

        System.out.println("Testing TARGET => SOURCE direction (with S->T diode)");
        value = new Random().nextInt();
        System.out.println("    Setting register of TARGET to " + value);
        targetComponent.setTx(value);
        System.out.println("    Register of SOURCE is now: " + sourceComponent.getRx());
        System.out.println("        => " + (value != sourceComponent.getRx()?"OK":"ERROR"));

        System.out.println("Removing diode");
        diode.remove();


        System.out.println();
        System.out.println("Inserting SOURCE <> TARGET wire");
        ConsoleLoggingIoWire wire = new ConsoleLoggingIoWire("WIRE");
        wire.insertAtPin(sourceComponent.getOutputPin());

        System.out.println("Testing SOURCE => TARGET direction (with wire)");
        value = new Random().nextInt();
        System.out.println("    Setting register of SOURCE to " + value);
        sourceComponent.setTx(value);
        System.out.println("    Register of TARGET is now: " + targetComponent.getRx());
        System.out.println("        => " + (value == targetComponent.getRx()?"OK":"ERROR"));

        System.out.println("Testing TARGET => SOURCE direction (with wire)");
        value = new Random().nextInt();
        System.out.println("    Setting register of TARGET to " + value);
        targetComponent.setTx(value);
        System.out.println("    Register of SOURCE is now: " + sourceComponent.getRx());
        System.out.println("        => " + (value != sourceComponent.getRx() ? "OK" : "ERROR"));

        System.out.println();
        System.out.println("Leaving wire in place, inserting TARGET ->|- SOURCE diode (blocking)");
        Diode reverseDiode = new Diode("REVERSE DIODE");
        reverseDiode.insertAtPin(targetComponent.getInputPin());

        System.out.println("Testing SOURCE => TARGET direction (with wire + reverse diode)");
        value = new Random().nextInt();
        System.out.println("    Setting register of SOURCE to " + value);
        sourceComponent.setTx(value);
        System.out.println("    Register of TARGET is now: " + targetComponent.getRx());
        System.out.println("        => " + (value != targetComponent.getRx()?"OK":"ERROR"));

        System.out.println("Testing TARGET => SOURCE direction (with wire + reverse diode)");
        value = new Random().nextInt();
        System.out.println("    Setting register of TARGET to " + value);
        targetComponent.setTx(value);
        System.out.println("    Register of SOURCE is now: " + sourceComponent.getRx());
        System.out.println("        => " + (value != sourceComponent.getRx()?"OK":"ERROR"));

        System.out.println("Removing reverseDiode");
        reverseDiode.remove();
        System.out.println("Removing wire");
        wire.remove();

        System.out.println("Connection is now from " + sourceComponent.getOutputPin() + " to " + sourceComponent.getOutputPin().getConnectedPin());
    }
}
