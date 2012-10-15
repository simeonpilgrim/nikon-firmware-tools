package com.nikonhacker.disassembly;

public class WriteListenerRegister32 extends Register32{
    private WriteListener writeListener;

    public WriteListenerRegister32(WriteListener writeListener) {
        this.writeListener = writeListener;
    }

    public void setValue(int value) {
        writeListener.onWrite(value);
        super.setValue(value);
    }


    public interface WriteListener {
        public void onWrite(int newValue);
    }

}
