package com.nikonhacker.emu.peripherials.serialInterface;

/**
 * This class connects 2 serial ports together, using 2 SerialConnectors
 * RX of the first goes to the TX of the second
 * RX of the second goes to the RX of the first
 */
public class BidirectionalSerialConnection {
    private SerialInterface serialInterface1;
    private SerialInterface serialInterface2;

    private SerialConnector one2two;
    private SerialConnector two2one;

    public BidirectionalSerialConnection(SerialInterface serialInterface1, SerialInterface serialInterface2) {
        this.serialInterface1 = serialInterface1;
        this.serialInterface2 = serialInterface2;
    }

    public void connect() {
        one2two = new SerialConnector(serialInterface1, serialInterface2);
        two2one = new SerialConnector(serialInterface2, serialInterface1);
        serialInterface1.addSerialValueReadyListener(one2two);
        serialInterface2.addSerialValueReadyListener(two2one);
    }

    public void disconnect() {
        serialInterface1.removeSerialValueReadyListener(one2two);
        serialInterface2.removeSerialValueReadyListener(two2one);
    }

}
