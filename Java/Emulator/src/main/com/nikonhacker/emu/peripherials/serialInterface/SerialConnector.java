package com.nikonhacker.emu.peripherials.serialInterface;

/**
 * This represents a connector from a source serial's Tx to a target serial's Rx
 */
public class SerialConnector implements SerialInterfaceListener {
    private final SerialInterface source;
    private final SerialInterface target;

    SerialConnector(SerialInterface source, SerialInterface target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public void onValueReady(SerialInterface serialInterface) {
        Integer value = source.read();
        while (value != null) {
            target.write(value);
            value = source.read();
        }
    }

    @Override
    public void onBitNumberChange(SerialInterface serialInterface, int nbBits) {
        // ignore
    }

}
