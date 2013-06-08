package com.nikonhacker.emu.memory.listener;

public interface MemoryActivityListener {

    /**
     * Method used to determine if this activity listener must be warned of change to a given address
     *
     * @param address the address to test
     * @return true if the address belongs to an area to watch
     */
    boolean matches(int address);

    /**
     * Method used to declare if this activity listener is a logger or can also modify data in onLoadXX methods
     * This is to make sure that logging happens AFTER modifiers have processed data, so that the logs reflects the
     * values that the code actually receives.
     *
     * @return true if this Listener is only a logger
     */
    boolean isReadOnly();


    /**
     * Method to be called each time a byte of instruction is read from the monitored area.
     * It gives the opportunity to return a value different than the one stored at that address if it was plain
     * memory. It is the responsibility of the programmer that only one listener attached to an address returns a
     * non-null value
     *
     * @param pageData the page that address belongs to
     * @param address the address from which data is read
     * @param value the value stored at this place if it was a standard memory address
     * @return the value to return instead of the backing memory. If null, the original value can be returned
     */
    Byte onLoadData8(byte[] pageData, int address, byte value);

    /**
     * Method to be called each time a halfword of instruction is read from the monitored area
     * It gives the opportunity to return a value different than the one stored at that address if it was plain
     * memory. It is the responsibility of the programmer that only one listener attached to an address returns a
     * non-null value
     *
     * @param pageData the page that address belongs to
     * @param address the address from which data is read
     * @param value the value stored at this place if it was a standard memory address
     * @return the value to return instead of the backing memory. If null, the original value can be returned
     */
    Integer onLoadData16(byte[] pageData, int address, int value);

    /**
     * Method to be called each time a word of instruction is read from the monitored area
     * It gives the opportunity to return a value different than the one stored at that address if it was plain
     * memory. It is the responsibility of the programmer that only one listener attached to an address returns a
     * non-null value
     *
     * @param pageData the page that address belongs to
     * @param address the address from which data is read
     * @param value the value stored at this place if it was a standard memory address
     * @return the value to return instead of the backing memory. If null, the original value can be returned
     */
    Integer onLoadData32(byte[] pageData, int address, int value);


    /**
     * Method to be called each time a byte of instruction is read from the monitored area
     *
     * @param pageData the page that address belongs to
     * @param address the address from which instruction is read
     * @param value the value stored at this place if it was a standard memory address
     */
    void onLoadInstruction8(byte[] pageData, int address, byte value);

    /**
     * Method to be called each time a halfword of instruction is read from the monitored area
     *
     * @param pageData the page that address belongs to
     * @param address the address from which instruction is read
     * @param value the value stored at this place if it was a standard memory address
     */
    void onLoadInstruction16(byte[] pageData, int address, int value);

    /**
     * Method to be called each time a word of instruction is read from the monitored area
     *
     * @param pageData the page that address belongs to
     * @param address the address from which instruction is read
     * @param value the value stored at this place if it was a standard memory address
     */
    void onLoadInstruction32(byte[] pageData, int address, int value);


    /**
     * Method to be called each time a byte is written to the monitored area
     *
     * @param pageData the page that address belongs to
     * @param address the address to which data is written
     * @param value the value being stored at that address
     */
    void onStore8(byte[] pageData, int address, byte value);

    /**
     * Method to be called each time a half-word is written to the monitored area
     *
     * @param pageData the page that address belongs to
     * @param address the address to which data is written
     * @param value the value being stored at that address
     */
    void onStore16(byte[] pageData, int address, int value);

    /**
     * Method to be called each time a word is written to the monitored area
     *
     * @param pageData the page that address belongs to
     * @param address the address to which data is written
     * @param value the value being stored at that address
     */
    void onStore32(byte[] pageData, int address, int value);

}
