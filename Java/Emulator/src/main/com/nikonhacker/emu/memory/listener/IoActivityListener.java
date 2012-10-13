package com.nikonhacker.emu.memory.listener;

/**
 * An IO Activity Listener is hooked to a page of memory addresses meant to contain IO registers
 */
public interface IoActivityListener {
    /**
     * Return the page to hook this listener to
     * @return
     */
    int getIoPage();


    /**
     * Method to be called each time a byte is read from the IO page
     * @param ioPage
     * @param addr
     * @param value
     * @return
     */
    Byte onIoLoad8(byte[] ioPage, int addr, byte value);

    /**
     * Method to be called each time a half word is read from the IO page
     * @param ioPage
     * @param addr
     * @param value
     * @return
     */
    Integer onIoLoad16(byte[] ioPage, int addr, int value);

    /**
     * Method to be called each time a word is read from the IO page
     * @param ioPage
     * @param addr
     * @param value
     * @return
     */
    Integer onIoLoad32(byte[] ioPage, int addr, int value);


    /**
     * Method to be called each time a byte is written to the IO page
     * @param ioPage
     * @param addr
     * @param value
     * @return
     */
    void onIoStore8(byte[] ioPage, int addr, byte value);

    /**
     * Method to be called each time a half word is written to the IO page
     * @param ioPage
     * @param addr
     * @param value
     * @return
     */
    void onIoStore16(byte[] ioPage, int addr, int value);

    /**
     * Method to be called each time a word is written to the IO page
     * @param ioPage
     * @param addr
     * @param value
     * @return
     */
    void onIoStore32(byte[] ioPage, int addr, int value);
}
