package com.nikonhacker.disassembly;

public class WriteListenerRegister32 extends Register32{
    private WriteListener writeListener;

    public WriteListenerRegister32(WriteListener writeListener) {
        this.writeListener = writeListener;
    }

    public void setValue(int value) {
        super.setValue(value);
        writeListener.afterWrite(value);
    }


    public interface WriteListener {
        public void afterWrite(int newValue);
    }

}
