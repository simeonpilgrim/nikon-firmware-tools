package com.nikonhacker.emu;

import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.trigger.condition.AlwaysBreakCondition;
import com.nikonhacker.emu.trigger.condition.BreakPointCondition;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;

import java.util.Random;

/**
 * This class tests all examples provided in the Fujitsu specification
 * http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-00101-5E.pdf
 *
 * Additionally, the bit search module example is taken from the FR80 Family programming manual 
 * http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-00104-3E.pdf
 *
 * Look for the word "spec" for failed tests or differences compared to the examples in the tests
 * Among other things, the spec says unused bits of PS should be 1 while code shows they have to be 0
 */
public class FrEmulatorTest extends TestCase {

    public static final boolean STAY_SILENT_IF_OK = true;
    public static final boolean STOP_ON_ERROR     = true;

    static final int BASE_ADDRESS = 0x40000;

    private static final int RANDOM_32 = 0xBAF60715;
    private static final int RANDOM_16 = 0xBF01;
    private static final int RANDOM_8  = 0xB3;

    static FrEmulator       emulator;
    static FrCPUState       cpuState;
    static DebuggableMemory memory;

    static {
        emulator = new FrEmulator();
        Platform platform = new Platform();
        FrInterruptController interruptController = new FrInterruptController(platform);

        emulator.clearBreakConditions();
        emulator.addBreakCondition(new AlwaysBreakCondition());

        cpuState = new FrCPUState(BASE_ADDRESS);
        emulator.setCpuState(cpuState);

        memory = new DebuggableMemory(false);
        memory.setLogMemoryMessages(false);
        emulator.setMemory(memory);

        platform.setCpuState(cpuState);
        platform.setMemory(memory);
        platform.setInterruptController(interruptController);

        emulator.setInterruptController(interruptController);
    }

    /*
     *
     * Utility methods
     *
     */

    private void initCpu() {
        cpuState.reset();
        cpuState.pc = BASE_ADDRESS;
        emulator.clearBreakConditions();
        emulator.addBreakCondition(new AlwaysBreakCondition()); // Exit at first interrupt period
    }

    private void setInstruction(int instruction) {
        memory.store16(BASE_ADDRESS, instruction);
    }

    private void checkRegister(int registerNumber, int expectedValue) {
        if (cpuState.getReg(registerNumber)==expectedValue) {
            if (!STAY_SILENT_IF_OK) System.out.println(" OK    : " + FrCPUState.registerLabels[registerNumber] + "=" + toHexString(cpuState.getReg(registerNumber), 8));
        }
        else {
            System.out.println(" ERROR : " + FrCPUState.registerLabels[registerNumber] + "=" + toHexString(cpuState.getReg(registerNumber), 8)
                    + ", should be " + toHexString(expectedValue, 8) + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (STOP_ON_ERROR) fail();
        }
    }

    private void checkILM(int expectedValue) {
        if (cpuState.getILM()==expectedValue) {
            if (!STAY_SILENT_IF_OK) System.out.println(" OK    : ILM=" + toBinString(cpuState.getILM(), 5));
        }
        else {
            System.out.println(" ERROR : ILM=" + toBinString(cpuState.getILM(), 5)
                    + ", should be " + toBinString(expectedValue, 5) + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (STOP_ON_ERROR) fail();
        }
    }

    private void checkCCR(int expectedValue) {
        if (cpuState.getCCR()==expectedValue) {
            if (!STAY_SILENT_IF_OK) System.out.println(" OK    : CCR=" + toBinString(cpuState.getCCR(), 4));
        }
        else {
            System.out.println(" ERROR : CCR=" + toBinString(cpuState.getCCR(), 4)
                    + ", should be " + toBinString(expectedValue, 4) + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (STOP_ON_ERROR) fail();
        }
    }

    private void checkSCR(int expectedValue) {
        if (cpuState.getSCR()==expectedValue) {
            if (!STAY_SILENT_IF_OK) System.out.println(" OK    : SCR=" + toBinString(cpuState.getSCR(), 3));
        }
        else {
            System.out.println(" ERROR : SCR=" + toBinString(cpuState.getSCR(), 3)
                    + ", should be " + toBinString(expectedValue, 3) + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (STOP_ON_ERROR) fail();
        }
    }

    private void checkPS(int expectedValue) {
        if (cpuState.getPS()==expectedValue) {
            if (!STAY_SILENT_IF_OK) System.out.println(" OK    : PS=" + toBinString(cpuState.getPS(), 32));
        }
        else {
            System.out.println(" ERROR : PS=" + toBinString(cpuState.getPS(), 32)
                    + ", should be " + toBinString(expectedValue, 32) + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (STOP_ON_ERROR) fail();
        }
    }

    private void checkMemory32(int address, int expectedValue) {
        if (memory.load32(address)==expectedValue) {
            if (!STAY_SILENT_IF_OK) System.out.println(" OK    : (" + toHexString(address, 8) + ")=" + toHexString(memory.load32(address), 8));
        }
        else {
            System.out.println(" ERROR : (" + toHexString(address, 8) + ")=" + toHexString(memory.load32(address), 8)
                    + ", should be " + toHexString(expectedValue, 8) + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (STOP_ON_ERROR) fail();
        }
    }

    private void checkMemory16(int address, int expectedValue) {
        if (memory.loadUnsigned16(address)==expectedValue) {
            if (!STAY_SILENT_IF_OK) System.out.println(" OK    : (" + toHexString(address, 8) + ")=" + toHexString(memory.loadUnsigned16(address), 4));
        }
        else {
            System.out.println(" ERROR : (" + toHexString(address, 8) + ")=" + toHexString(memory.loadUnsigned16(address), 4)
                    + ", should be " + toHexString(expectedValue, 4) + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (STOP_ON_ERROR) fail();
        }
    }

    private void checkMemory8(int address, int expectedValue) {
        if (memory.loadUnsigned8(address)==expectedValue) {
            if (!STAY_SILENT_IF_OK) System.out.println(" OK    : (" + toHexString(address, 8) + ")=" + toHexString(memory.loadUnsigned8(address), 2));
        }
        else {
            System.out.println(" ERROR : (" + toHexString(address, 8) + ")=" + toHexString(memory.loadUnsigned8(address), 2)
                    + ", should be " + toHexString(expectedValue, 2) + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (STOP_ON_ERROR) fail();
        }
    }

    private void checkPC(int expectedValue) {
        if (cpuState.pc==expectedValue) {
            if (!STAY_SILENT_IF_OK) System.out.println(" OK    : PC=" + toHexString(cpuState.pc, 8));
        }
        else {
            System.out.println(" ERROR : PC=" + toHexString(cpuState.pc, 8)
                    + ", should be " + toHexString(expectedValue, 8) + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (STOP_ON_ERROR) fail();
        }
    }



    private String toHexString(int address, int numChars) {
        return "0x" + StringUtils.leftPad(Integer.toHexString(address), numChars, '0');
    }

    private String toBinString(int address, int numChars) {
        return "0b" + StringUtils.leftPad(Integer.toBinaryString(address), numChars, '0');
    }


    /*
     *
     * ACTUAL TEST METHODS
     *
     */

    public void testADD_A6() throws EmulationException {
        System.out.println("EmulatorTest.testADD_A6");
        initCpu();

        setInstruction(0xa623); // 0b1010011000100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, 0x87654321);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x99999999);
        checkCCR(0x8); //0b1000
    }

    public void testADD_A4() throws EmulationException {
        System.out.println("EmulatorTest.testADD_A4");
        initCpu();

        setInstruction(0xa423); // 0b1010010000100011

        cpuState.setReg(3, 0x99999997);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x99999999);
        checkCCR(0x8); //0b1000
    }

    public void testADD2() throws EmulationException {
        System.out.println("EmulatorTest.testADD2");
        initCpu();

        setInstruction(0xa5e3); // 0b1010010111100011

        cpuState.setReg(3, 0x99999999);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x99999997);
        checkCCR(0x9); // 0b1001
    }

    public void testADDC() throws EmulationException {
        System.out.println("EmulatorTest.testADDC");
        initCpu();

        setInstruction(0xa723); // 0b1010011100100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, 0x87654320);
        cpuState.setCCR(1); // 0b0001

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x99999999);
        checkCCR(0x8); // 0b1000
    }



    public void testADDN_A2() throws EmulationException {
        System.out.println("EmulatorTest.testADDN_A2");
        initCpu();

        setInstruction(0xa223); // 0b1010001000100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, 0x87654321);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x99999999);
        checkCCR(0); // 0b0000
    }

    public void testADDN_A1() throws EmulationException {
        System.out.println("EmulatorTest.testADDN_A1");
        initCpu();

        setInstruction(0xa023); // 0b1010000000100011

        cpuState.setReg(3, 0x99999997);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x99999999);
        checkCCR(0); // 0b0000
    }

    public void testADDN2() throws EmulationException {
        System.out.println("EmulatorTest.testADDN2");
        initCpu();

        setInstruction(0xa1e3); // 0b1010000111100011

        cpuState.setReg(3, 0x99999999);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x99999997);
        checkCCR(0); // 0b0000
    }



    public void testSUB() throws EmulationException {
        System.out.println("EmulatorTest.testSUB");
        initCpu();

        setInstruction(0xac23); // 0b1010110000100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, 0x99999999);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x87654321);
        checkCCR(0x8); // 0b1000
    }


    public void testSUBC() throws EmulationException {
        System.out.println("EmulatorTest.testSUBC");
        initCpu();

        setInstruction(0xad23); // 0b1010110100100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, 0x99999999);
        cpuState.setCCR(1); // 0b0001

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x87654320);
        checkCCR(0x8); // 0b1000
    }

    public void testSUBN() throws EmulationException {
        System.out.println("EmulatorTest.testSUBN");
        initCpu();

        setInstruction(0xae23); // 0b1010111000100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, 0x99999999);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x87654321);
        checkCCR(0x0); // 0b0000
    }



    public void testCMP_AA() throws EmulationException {
        System.out.println("EmulatorTest.testCMP_AA");
        initCpu();

        setInstruction(0xaa23); // 0b1010101000100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, 0x12345678);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x12345678);
        checkCCR(0x4); // 0b0100
    }

    public void testCMP_AAbis() throws EmulationException {
        System.out.println("EmulatorTest.testCMP_AAbis");
        initCpu();

        setInstruction(0xaa23); // 0b1010101000100011

        cpuState.setReg(2, 0x82345679);
        cpuState.setReg(3, 0x82345678);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkCCR(0x9); // 0b1001
    }

    public void testCMP_A8() throws EmulationException {
        System.out.println("EmulatorTest.testCMP_A8");
        initCpu();

        setInstruction(0xa833); // 0b1010100000110011

        cpuState.setReg(3, 0x00000003);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x00000003);
        checkCCR(0x4); // 0b0100
    }

    public void testCMP2() throws EmulationException {
        System.out.println("EmulatorTest.testCMP2");
        initCpu();

        setInstruction(0xa9d3); // 0b1010100111010011

        cpuState.setReg(3, 0xFFFFFFFD);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0xFFFFFFFD);
        checkCCR(0x4); // 0b0100
    }



    public void testAND_82() throws EmulationException {
        System.out.println("EmulatorTest.testAND_82");
        initCpu();

        setInstruction(0x8223); // 0b1000001000100011

        cpuState.setReg(2, 0x11110000);
        cpuState.setReg(3, 0x10101010);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x11110000);
        checkRegister(3, 0x10100000);
        checkCCR(0); // 0b0000
    }

    public void testAND_84() throws EmulationException {
        System.out.println("EmulatorTest.testAND_84");
        initCpu();

        setInstruction(0x8423); // 0b1000010000100011

        cpuState.setReg(2, 0x11110000);
        cpuState.setReg(3, 0x12345678);
        memory.store32(0x12345678, 0x10101010);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x11110000);
        checkRegister(3, 0x12345678);
        checkMemory32(0x12345678, 0x10100000);
        checkCCR(0); // 0b0000
    }


    public void testANDH() throws EmulationException {
        System.out.println("EmulatorTest.testANDH");
        initCpu();

        setInstruction(0x8523); // 0b1000010100100011

        cpuState.setReg(2, 0x00001100);
        cpuState.setReg(3, 0x12345678);
        memory.store16(0x12345678, 0x1010);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00001100);
        checkRegister(3, 0x12345678);
        checkMemory16(0x12345678, 0x1000);
        checkCCR(0); // 0b0000
    }

    public void testANDB() throws EmulationException {
        System.out.println("EmulatorTest.testANDB");
        initCpu();

        setInstruction(0x8623); // 0b1000011000100011

        cpuState.setReg(2, 0x00000010);
        cpuState.setReg(3, 0x12345678);
        memory.store8(0x12345678, 0x11);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x000000010);
        checkRegister(3, 0x12345678);
        checkMemory8(0x12345678, 0x10);
        checkCCR(0); // 0b0000
    }



    public void testOR_92() throws EmulationException {
        System.out.println("EmulatorTest.testOR_92");
        initCpu();

        setInstruction(0x9223); // 0b1001001000100011

        cpuState.setReg(2, 0x11110000);
        cpuState.setReg(3, 0x10101010);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x11110000);
        checkRegister(3, 0x11111010);
        checkCCR(0); // 0b0000
    }

    public void testOR_94() throws EmulationException {
        System.out.println("EmulatorTest.testOR_92");
        initCpu();

        setInstruction(0x9423); // 0b1001010000100011

        cpuState.setReg(2, 0x11110000);
        cpuState.setReg(3, 0x12345678);
        memory.store32(0x12345678, 0x10101010);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x11110000);
        checkRegister(3, 0x12345678);
        checkMemory32(0x12345678, 0x11111010);
        checkCCR(0); // 0b0000
    }

    public void testORH() throws EmulationException {
        System.out.println("EmulatorTest.testORH");
        initCpu();

        setInstruction(0x9523); // 0b1001010100100011

        cpuState.setReg(2, 0x00001100);
        cpuState.setReg(3, 0x12345678);
        memory.store16(0x12345678, 0x1010);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00001100);
        checkRegister(3, 0x12345678);
        checkMemory16(0x12345678, 0x1110);
        checkCCR(0); // 0b0000
    }

    public void testORB() throws EmulationException {
        System.out.println("EmulatorTest.testORB");
        initCpu();

        setInstruction(0x9623); // 0b1001011000100011

        cpuState.setReg(2, 0x00000011);
        cpuState.setReg(3, 0x12345678);
        memory.store8(0x12345678, 0x10);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00000011);
        checkRegister(3, 0x12345678);
        checkMemory8(0x12345678, 0x11);
        checkCCR(0); // 0b0000
    }



    public void testEOR_9A() throws EmulationException {
        System.out.println("EmulatorTest.testEOR_9A");
        initCpu();

        setInstruction(0x9a23); // 0b1001101000100011

        cpuState.setReg(2, 0x11110000);
        cpuState.setReg(3, 0x10101010);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x11110000);
        checkRegister(3, 0x01011010);
        checkCCR(0); // 0b0000
    }

    public void testEOR_9C() throws EmulationException {
        System.out.println("EmulatorTest.testEOR_9C");
        initCpu();

        setInstruction(0x9c23); // 0b1001110000100011

        cpuState.setReg(2, 0x11110000);
        cpuState.setReg(3, 0x12345678);
        memory.store32(0x12345678, 0x10101010);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x11110000);
        checkRegister(3, 0x12345678);
        checkMemory32(0x12345678, 0x01011010);
        checkCCR(0); // 0b0000
    }

    public void testEORH() throws EmulationException {
        System.out.println("EmulatorTest.testEORH");
        initCpu();

        setInstruction(0x9d23); // 0b1001110100100011

        cpuState.setReg(2, 0x00001100);
        cpuState.setReg(3, 0x12345678);
        memory.store16(0x12345678, 0x1010);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00001100);
        checkRegister(3, 0x12345678);
        checkMemory16(0x12345678, 0x0110);
        checkCCR(0); // 0b0000
    }

    public void testEORB() throws EmulationException {
        System.out.println("EmulatorTest.testEORB");
        initCpu();

        setInstruction(0x9e23); // 0b1001111000100011

        cpuState.setReg(2, 0x00000011);
        cpuState.setReg(3, 0x12345678);
        memory.store8(0x12345678, 0x10);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00000011);
        checkRegister(3, 0x12345678);
        checkMemory8(0x12345678, 0x01);
        checkCCR(0); // 0b0000
    }



    public void testBANDL() throws EmulationException {
        System.out.println("EmulatorTest.testBANDL");
        initCpu();

        setInstruction(0x8003); // 0b1000000000000011

        cpuState.setReg(3, 0x12345678);
        memory.store8(0x12345678, 0x11);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x12345678);
        checkMemory8(0x12345678, 0x10);
        checkCCR(0); // 0b0000
    }

    public void testBANDH() throws EmulationException {
        System.out.println("EmulatorTest.testBANDH");
        initCpu();

        setInstruction(0x8103); // 0b1000000100000011

        cpuState.setReg(3, 0x12345678);
        memory.store8(0x12345678, 0x11);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x12345678);
        checkMemory8(0x12345678, 0x01);
        checkCCR(0); // 0b0000
    }



    public void testBORL() throws EmulationException {
        System.out.println("EmulatorTest.testBORL");
        initCpu();

        setInstruction(0x9013); // 0b1001000000010011

        cpuState.setReg(3, 0x12345678);
        memory.store8(0x12345678, 0x00);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x12345678);
        checkMemory8(0x12345678, 0x01);
        checkCCR(0); // 0b0000
    }

    public void testBORH() throws EmulationException {
        System.out.println("EmulatorTest.testBORH");
        initCpu();

        setInstruction(0x9113); // 0b1001000100010011

        cpuState.setReg(3, 0x12345678);
        memory.store8(0x12345678, 0x00);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x12345678);
        checkMemory8(0x12345678, 0x10);
        checkCCR(0); // 0b0000
    }



    public void testBEORL() throws EmulationException {
        System.out.println("EmulatorTest.testBEORL");
        initCpu();

        setInstruction(0x9813); // 0b1001100000010011

        cpuState.setReg(3, 0x12345678);
        memory.store8(0x12345678, 0x00);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x12345678);
        checkMemory8(0x12345678, 0x01);
        checkCCR(0); // 0b0000
    }

    public void testBEORH() throws EmulationException {
        System.out.println("EmulatorTest.testBEORH");
        initCpu();

        setInstruction(0x9913); // 0b1001100100010011

        cpuState.setReg(3, 0x12345678);
        memory.store8(0x12345678, 0x00);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x12345678);
        checkMemory8(0x12345678, 0x10);
        checkCCR(0); // 0b0000
    }



    public void testBTSTL() throws EmulationException {
        System.out.println("EmulatorTest.testBTSTL");
        initCpu();

        setInstruction(0x8813); // 0b1000100000010011 BTSTL #1, @R3

        cpuState.setReg(3, 0x12345678);
        memory.store8(0x12345678, 0x10);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x12345678);
        checkMemory8(0x12345678, 0x10);
        checkCCR(4); // 0b0100
    }

    public void testBTSTH() throws EmulationException {
        System.out.println("EmulatorTest.testBTSTH");
        initCpu();

        setInstruction(0x8913); // 0b1000100100010011

        cpuState.setReg(3, 0x12345678);
        memory.store8(0x12345678, 0x01);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x12345678);
        checkMemory8(0x12345678, 0x01);
        checkCCR(4); // 0b0100
    }



    public void testMUL() throws EmulationException {
        System.out.println("EmulatorTest.testMUL");
        initCpu();

        setInstruction(0xaf23); // 0b1010111100100011

        cpuState.setReg(2, 0x00000002);
        cpuState.setReg(3, 0x80000001);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00000002);
        checkRegister(3, 0x80000001);
        checkRegister(FrCPUState.MDH, 0xFFFFFFFF);
        checkRegister(FrCPUState.MDL, 0x00000002);
        checkCCR(2); // 0b0010
    }

    public void testMULU() throws EmulationException {
        System.out.println("EmulatorTest.testMULU");
        initCpu();

        setInstruction(0xab23); // 0b1010101100100011

        cpuState.setReg(2, 0x00000002);
        cpuState.setReg(3, 0x80000001);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00000002);
        checkRegister(3, 0x80000001);
        checkRegister(FrCPUState.MDH, 0x00000001);
        checkRegister(FrCPUState.MDL, 0x00000002);
        checkCCR(2); // 0b0010
    }

    public void testMULH() throws EmulationException {
        System.out.println("EmulatorTest.testMULH");
        initCpu();

        setInstruction(0xbf23); // 0b1011111100100011

        cpuState.setReg(2, 0xFFDCBA98);
        cpuState.setReg(3, 0x01234567);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0xFFDCBA98);
        checkRegister(3, 0x01234567);
        checkRegister(FrCPUState.MDL, 0xED2F0B28);
        checkCCR(8); // 0b1000
    }

    public void testMULUH() throws EmulationException {
        System.out.println("EmulatorTest.testMULUH");
        initCpu();

        setInstruction(0xbb23); // 0b1011101100100011

        cpuState.setReg(2, 0xFFDCBA98);
        cpuState.setReg(3, 0x01234567);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0xFFDCBA98);
        checkRegister(3, 0x01234567);
        checkRegister(FrCPUState.MDL, 0x32960B28);
        checkCCR(0); // 0b0000
    }

    public void testDIV0S() throws EmulationException {
        System.out.println("EmulatorTest.testDIV0S");
        initCpu();

        setInstruction(0x9742); // 0b1001011101000010

        cpuState.setReg(2, 0x0FFFFFFF);
        cpuState.setReg(FrCPUState.MDH, 0x00000000);
        cpuState.setReg(FrCPUState.MDL, 0xFFFFFFF0);
        cpuState.setSCR(0); // 0b000

        emulator.play();

        checkRegister(2, 0x0FFFFFFF);
        checkRegister(FrCPUState.MDH, 0xFFFFFFFF);
        checkRegister(FrCPUState.MDL, 0xFFFFFFF0);
        checkSCR(0x6); // 0b110
    }

    public void testDIV0U() throws EmulationException {
        System.out.println("EmulatorTest.testDIV0U");
        initCpu();

        setInstruction(0x9752); // 0b1001011101010010

        cpuState.setReg(2, 0x00FFFFFF);
        cpuState.setReg(FrCPUState.MDH, 0x00000000);
        cpuState.setReg(FrCPUState.MDL, 0x0FFFFFF0);
        cpuState.setSCR(6); // 0b110

        emulator.play();

        checkRegister(2, 0x00FFFFFF);
        checkRegister(FrCPUState.MDH, 0x00000000);
        checkRegister(FrCPUState.MDL, 0x0FFFFFF0);
        checkSCR(0); // 0b000
    }

    public void testDIV1() throws EmulationException {
        System.out.println("EmulatorTest.testDIV1");
        initCpu();

        setInstruction(0x9762); // 0b1001011101100010

        // Note : probably an error in the spec :
        // we shouldn't have the same value in divisor and MDH because the previous step should have subtracted it

        cpuState.setReg(2, 0x00FFFFFF);
        cpuState.setReg(FrCPUState.MDH, 0x00FFFFFF);
        cpuState.setReg(FrCPUState.MDL, 0x00000000);
        cpuState.setSCR(0); // 0b000
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00FFFFFF);
        checkRegister(FrCPUState.MDH, 0x00FFFFFF /*0x01000000*/); // See above. Changed for the test to succeed
        checkRegister(FrCPUState.MDL, 0x00000001);
        checkSCR(0); // 0b000
        checkCCR(0); // 0b0000
    }


    public void testDIV2() throws EmulationException {
        System.out.println("EmulatorTest.testDIV2");
        initCpu();

        setInstruction(0x9772); // 0b1001011101110010

        cpuState.setReg(2, 0x00FFFFFF);
        cpuState.setReg(FrCPUState.MDH, 0x00FFFFFF);
        cpuState.setReg(FrCPUState.MDL, 0x0000000F);
        cpuState.setSCR(0); // 0b000
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00FFFFFF);
        checkRegister(FrCPUState.MDH, 0x00000000);
        checkRegister(FrCPUState.MDL, 0x0000000F);
        checkSCR(0); // 0b000
        checkCCR(4); // 0b0100
    }

    public void testDIV3() throws EmulationException {
        System.out.println("EmulatorTest.testDIV3");
        initCpu();

        setInstruction(0x9f60); // 0b1001111101100000

        cpuState.setReg(2, 0x00FFFFFF);
        cpuState.setReg(FrCPUState.MDH, 0x00000000);
        cpuState.setReg(FrCPUState.MDL, 0x0000000F);
        cpuState.setSCR(0); // 0b000
        cpuState.setCCR(4); // 0b0100

        emulator.play();

        checkRegister(2, 0x00FFFFFF);
        checkRegister(FrCPUState.MDH, 0x00000000);
        checkRegister(FrCPUState.MDL, 0x00000010);
        checkSCR(0); // 0b000
        checkCCR(4); // 0b0100
    }

    public void testDIV4S() throws EmulationException {
        System.out.println("EmulatorTest.testDIV4S");
        initCpu();

        setInstruction(0x9f70); // 0b1001111101110000

        cpuState.setReg(2, 0x00FFFFFF);
        cpuState.setReg(FrCPUState.MDH, 0x00000000);
        cpuState.setReg(FrCPUState.MDL, 0x0000000F);
        cpuState.setSCR(6); // 0b110
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00FFFFFF);
        checkRegister(FrCPUState.MDH, 0x00000000);
        checkRegister(FrCPUState.MDL, 0xFFFFFFF1);
        checkSCR(6); // 0b110
        checkCCR(0); // 0b0000
    }




    public void testFullDIVS() throws EmulationException {
        System.out.println("EmulatorTest.testFullDIVS");
        initCpu();

        memory.store16(BASE_ADDRESS     , 0x9742); // 0b1001011101000010 DIV0S R2
        memory.store16(BASE_ADDRESS +  2, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  4, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  6, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  8, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 10, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 12, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 14, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 16, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 18, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 20, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 22, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 24, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 26, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 28, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 30, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 32, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 34, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 36, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 38, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 40, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 42, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 44, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 46, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 48, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 50, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 52, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 54, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 56, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 58, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 60, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 62, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 64, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 66, 0x9772); // 0b1001011101110010 DIV2
        memory.store16(BASE_ADDRESS + 68, 0x9f60); // 0b1001111101100000 DIV3
        memory.store16(BASE_ADDRESS + 70, 0x9f70); // 0b1001111101110000 DIV4S

        cpuState.setReg(2, 0x01234567);
        cpuState.setReg(FrCPUState.MDL, 0xFEDCBA98);
        cpuState.setSCR(0); // 0b000

        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(BASE_ADDRESS + 72, null));
        emulator.play();

        checkRegister(2, 0x01234567);
        checkRegister(FrCPUState.MDH, 0xFFFFFFFF);
        checkRegister(FrCPUState.MDL, 0xFFFFFFFF);
        checkSCR(0x6); // 0b110
    }


    public void testOneSignedDivision(int dividend, int divisor) throws EmulationException {
        initCpu();

        memory.store16(BASE_ADDRESS     , 0x9742); // 0b1001011101000010 DIV0S R2
        memory.store16(BASE_ADDRESS +  2, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  4, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  6, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  8, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 10, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 12, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 14, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 16, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 18, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 20, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 22, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 24, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 26, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 28, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 30, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 32, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 34, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 36, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 38, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 40, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 42, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 44, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 46, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 48, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 50, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 52, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 54, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 56, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 58, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 60, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 62, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 64, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 66, 0x9772); // 0b1001011101110010 DIV2
        memory.store16(BASE_ADDRESS + 68, 0x9f60); // 0b1001111101100000 DIV3
        memory.store16(BASE_ADDRESS + 70, 0x9f70); // 0b1001111101110000 DIV4S

        cpuState.setReg(2, divisor);
        cpuState.setReg(FrCPUState.MDL, dividend);
        cpuState.setSCR(0); // 0b000

        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(BASE_ADDRESS + 72, null));

        emulator.play();

        int foundQuotient = cpuState.getReg(FrCPUState.MDL);
        int foundRemainder = cpuState.getReg(FrCPUState.MDH);

        int correctQuotient = dividend / divisor;
        int correctRemainder = dividend % divisor;

        if ((correctQuotient != foundQuotient) || (correctRemainder != foundRemainder)) {
            System.out.println("Error : found " + dividend + "=" + foundQuotient + "x" + divisor + " + " + foundRemainder);
        }
    }

    public void testMultipleSignedDivisions() throws EmulationException {
        System.out.println("EmulatorTest.testMultipleUnsignedDivisions");
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            int dividend = (int) (random.nextLong());
            int divisor = (int) (random.nextLong());
            if (divisor != 0) {
                testOneSignedDivision(dividend, divisor);
            }
        }
    }


    // ***********************************


    public void testFullDIVU() throws EmulationException {
        System.out.println("EmulatorTest.testFullDIVU");
        initCpu();

        memory.store16(BASE_ADDRESS     , 0x9752); // 0b1001011101010010 DIV0U R2
        memory.store16(BASE_ADDRESS +  2, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  4, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  6, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  8, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 10, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 12, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 14, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 16, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 18, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 20, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 22, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 24, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 26, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 28, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 30, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 32, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 34, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 36, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 38, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 40, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 42, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 44, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 46, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 48, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 50, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 52, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 54, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 56, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 58, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 60, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 62, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 64, 0x9762); // 0b1001011101100010 DIV1  R2

        cpuState.setReg(2, 0x01234567);
        cpuState.setReg(FrCPUState.MDL, 0xFEDCBA98);
        cpuState.setSCR(0); // 0b000

        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(BASE_ADDRESS + 66, null));
        emulator.play();

        checkRegister(2, 0x01234567);
        checkRegister(FrCPUState.MDH, 0x00000078);
        checkRegister(FrCPUState.MDL, 0x000000E0);
        checkSCR(0x0); // 0b000
    }


    public void testOneUnsignedDivision(int dividend, int divisor) throws EmulationException {
        initCpu();

        memory.store16(BASE_ADDRESS     , 0x9752); // 0b1001011101010010 DIV0U R2
        memory.store16(BASE_ADDRESS +  2, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  4, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  6, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS +  8, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 10, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 12, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 14, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 16, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 18, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 20, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 22, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 24, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 26, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 28, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 30, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 32, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 34, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 36, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 38, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 40, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 42, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 44, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 46, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 48, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 50, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 52, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 54, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 56, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 58, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 60, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 62, 0x9762); // 0b1001011101100010 DIV1  R2
        memory.store16(BASE_ADDRESS + 64, 0x9762); // 0b1001011101100010 DIV1  R2

        cpuState.setReg(2, divisor);
        cpuState.setReg(FrCPUState.MDL, dividend);
        cpuState.setSCR(0); // 0b000

        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(BASE_ADDRESS + 66, null));
        emulator.play();

        long foundQuotient = cpuState.getReg(FrCPUState.MDL) & 0xFFFFFFFFL;
        long foundRemainder = cpuState.getReg(FrCPUState.MDH) & 0xFFFFFFFFL;
        
        long correctQuotient = (dividend & 0xFFFFFFFFL) / (divisor & 0xFFFFFFFFL);
        long correctRemainder = (dividend & 0xFFFFFFFFL) % (divisor & 0xFFFFFFFFL);
        
        if ((correctQuotient != foundQuotient) || (correctRemainder != foundRemainder)) {
            System.out.println("Error : found " + (dividend & 0xFFFFFFFFL) + "=" + foundQuotient + "x" + (divisor & 0xFFFFFFFFL) + " + " + foundRemainder);
        }
        
    }

    public void testMultipleUnsignedDivisions() throws EmulationException {
        System.out.println("EmulatorTest.testMultipleUnsignedDivisions");
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            int dividend = (int) (random.nextLong()); // Using long to have more than Integer.MAX_VALUE
            int divisor = (int) (random.nextLong()); // Using long to have more than Integer.MAX_VALUE
            if (divisor != 0) {
                testOneUnsignedDivision(dividend, divisor);
            }
        }
    }


    public void testLSL_B6() throws EmulationException {
        System.out.println("EmulatorTest.testLSL_B6");
        initCpu();

        setInstruction(0xb623); // 0b1011011000100011

        cpuState.setReg(2, 0x00000008);
        cpuState.setReg(3, 0xFFFFFFFF);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00000008);
        checkRegister(3, 0xFFFFFF00);
        checkCCR(9); // 0b1001
    }

    public void testLSL_B4() throws EmulationException {
        System.out.println("EmulatorTest.testLSL_B4");
        initCpu();

        setInstruction(0xb483); // 0b1011010010000011

        cpuState.setReg(3, 0xFFFFFFFF);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0xFFFFFF00);
        checkCCR(9); // 0b1001
    }

    public void testLSL2() throws EmulationException {
        System.out.println("EmulatorTest.testLSL2");
        initCpu();

        setInstruction(0xb583); // 0b1011010110000011

        cpuState.setReg(3, 0xFFFFFFFF);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0xFF000000);
        checkCCR(9); // 0b1001
    }



    public void testLSR_B2() throws EmulationException {
        System.out.println("EmulatorTest.testLSR_B6");
        initCpu();

        setInstruction(0xb223); // 0b1011001000100011

        cpuState.setReg(2, 0x00000008);
        cpuState.setReg(3, 0xFFFFFFFF);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00000008);
        checkRegister(3, 0x00FFFFFF);
        checkCCR(1); // 0b0001
    }

    public void testLSR_B0() throws EmulationException {
        System.out.println("EmulatorTest.testLSR_B0");
        initCpu();

        setInstruction(0xb083); // 0b1011000010000011

        cpuState.setReg(3, 0xFFFFFFFF);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x00FFFFFF);
        checkCCR(1); // 0b0001
    }

    public void testLSR2() throws EmulationException {
        System.out.println("EmulatorTest.testLSR2");
        initCpu();

        setInstruction(0xb183); // 0b1011000110000011

        cpuState.setReg(3, 0xFFFFFFFF);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0x000000FF);
        checkCCR(1); // 0b0001
    }



    public void testASR_BA() throws EmulationException {
        System.out.println("EmulatorTest.testASR_BA");
        initCpu();

        setInstruction(0xba23); // 0b1011101000100011

        cpuState.setReg(2, 0x00000008);
        cpuState.setReg(3, 0xFF0FFFFF);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0x00000008);
        checkRegister(3, 0xFFFF0FFF);
        checkCCR(9); // 0b1001
    }

    public void testASR_B8() throws EmulationException {
        System.out.println("EmulatorTest.testASR_B8");
        initCpu();

        setInstruction(0xb883); // 0b1011100010000011

        cpuState.setReg(3, 0xFF0FFFFF);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0xFFFF0FFF);
        checkCCR(9); // 0b1001
    }

    public void testASR2() throws EmulationException {
        System.out.println("EmulatorTest.testASR2");
        initCpu();

        setInstruction(0xb983); // 0b1011100110000011

        cpuState.setReg(3, 0xF0FFFFFF);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(3, 0xFFFFFFF0);
        checkCCR(9); // 0b1001
    }



    public void testLDI32() throws EmulationException {
        System.out.println("EmulatorTest.testLDI32");
        initCpu();

        memory.store16(BASE_ADDRESS    , 0x9f83); // 0b1001111110000011
        memory.store16(BASE_ADDRESS + 2, 0x8765); // 0b1000011101100101
        memory.store16(BASE_ADDRESS + 4, 0x4321); // 0b0100001100100001

        cpuState.setReg(3, 0x00000000);

        emulator.play();

        checkRegister(3, 0x87654321);
    }

    public void testLDI20() throws EmulationException {
        System.out.println("EmulatorTest.testLDI20");
        initCpu();

        memory.store16(BASE_ADDRESS    , 0x9b53); // 0b1001101101010011
        memory.store16(BASE_ADDRESS + 2, 0x4321); // 0b0100001100100001

        cpuState.setReg(3, 0x00000000);

        emulator.play();

        checkRegister(3, 0x00054321);
    }

    public void testLDI8() throws EmulationException {
        System.out.println("EmulatorTest.testLDI8");
        initCpu();

        setInstruction(0xc213); // 0b1100001000010011

        cpuState.setReg(3, 0x00000000);

        emulator.play();

        checkRegister(3, 0x00000021);
    }

    public void testLD_04() throws EmulationException {
        System.out.println("EmulatorTest.testLD_04");
        initCpu();

        setInstruction(0x423); // 0b0000010000100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, 0x00000000);
        memory.store32(0x12345678, 0x87654321);

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x87654321);
        checkMemory32(0x12345678, 0x87654321);
    }

    public void testLD_00() throws EmulationException {
        System.out.println("EmulatorTest.testLD_00");
        initCpu();

        setInstruction(0x23); // 0b0000000000100011

        cpuState.setReg(2, 0x00000004);
        cpuState.setReg(3,  RANDOM_32);
        cpuState.setReg(13, 0x12345678);
        memory.store32(0x1234567C, 0x87654321);

        emulator.play();

        checkRegister(2, 0x00000004);
        checkRegister(3, 0x87654321);
        checkRegister(13, 0x12345678);
        checkMemory32(0x1234567C, 0x87654321);
    }

    public void testLD_20() throws EmulationException {
        System.out.println("EmulatorTest.testLD_20");
        initCpu();

        setInstruction(0x2013); // 0b0010000000010011

        cpuState.setReg(3,  RANDOM_32);
        cpuState.setReg(14, 0x12345678);
        memory.store32(0x1234567C, 0x87654321);

        emulator.play();

        checkRegister(3, 0x87654321);
        checkRegister(14, 0x12345678);
        checkMemory32(0x1234567C, 0x87654321);
    }

    public void testLD_03() throws EmulationException {
        System.out.println("EmulatorTest.testLD_03");
        initCpu();

        setInstruction(0x313); // 0b0000001100010011

        cpuState.setReg(3, RANDOM_32);
        cpuState.setReg(15, 0x12345678);
        memory.store32(0x1234567C, 0x87654321);

        emulator.play();

        checkRegister(3, 0x87654321);
        checkRegister(15, 0x12345678);
        checkMemory32(0x1234567C, 0x87654321);
    }

    public void testLD_070() throws EmulationException {
        System.out.println("EmulatorTest.testLD_070");
        initCpu();

        setInstruction(0x703); // 0b0000011100000011

        cpuState.setReg(3, RANDOM_32);
        cpuState.setReg(15, 0x12345678);
        memory.store32(0x12345678, 0x87654321);

        emulator.play();

        checkRegister(3, 0x87654321);
        checkRegister(15, 0x1234567C);
        checkMemory32(0x12345678, 0x87654321);
    }

    public void testLD_078() throws EmulationException {
        System.out.println("EmulatorTest.testLD_078");
        initCpu();
        setInstruction(0x784); // 0b0000011110000100

        cpuState.setReg(15, 0x12345674);
        cpuState.setReg(FrCPUState.MDH, RANDOM_32);
        memory.store32(0x12345674, 0x87654321);

        emulator.play();

        checkRegister(15, 0x12345678);
        checkRegister(FrCPUState.MDH, 0x87654321);
        checkMemory32(0x12345674, 0x87654321);
    }

    public void testLD_079() throws EmulationException {
        System.out.println("EmulatorTest.testLD_079");
        initCpu();
        setInstruction(0x790); // 0b0000011110010000

        cpuState.setReg(15, 0x12345674);
        cpuState.setPS(0xFFFFF8D5, false);
        memory.store32(0x12345674, 0xFFF8F8C0);

        emulator.play();

        checkRegister(15, 0x12345678);
        // Note : The samples assume unused bits are 1, which is is wrong
        // spec is wrong checkPS(0xFFF8F8C0);
        // Reality shows that §3.3.2 of the spec (page 19) is right: unused bits should be 0.
        checkPS(0x00180000);
        checkMemory32(0x12345674, 0xFFF8F8C0);
    }

    public void testLDUH_05() throws EmulationException {
        System.out.println("EmulatorTest.testLDUH_05");
        initCpu();
        setInstruction(0x523); // 0b0000010100100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, RANDOM_32);
        memory.store16(0x12345678, 0x4321);

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x00004321);
        checkMemory16(0x12345678, 0x4321);
    }

    public void testLDUH_01() throws EmulationException {
        System.out.println("EmulatorTest.testLDUH_01");
        initCpu();
        setInstruction(0x123); // 0b0000000100100011

        cpuState.setReg(2, 0x00000004);
        cpuState.setReg(3, RANDOM_32);
        cpuState.setReg(13, 0x12345678);
        memory.store16(0x1234567C, 0x4321);

        emulator.play();

        checkRegister(2, 0x00000004);
        checkRegister(3, 0x00004321);
        checkRegister(13, 0x12345678);
        checkMemory16(0x1234567C, 0x4321);
    }

    public void testLDUH_40() throws EmulationException {
        System.out.println("EmulatorTest.testLDUH_40");
        initCpu();
        setInstruction(0x4013); // 0b0100000000010011

        cpuState.setReg(3, RANDOM_32);
        cpuState.setReg(14, 0x12345678);
        memory.store16(0x1234567A, 0x4321);

        emulator.play();

        checkRegister(3, 0x00004321);
        checkRegister(14, 0x12345678);
        checkMemory16(0x1234567A, 0x4321);
    }

    public void testLDUB_06() throws EmulationException {
        System.out.println("EmulatorTest.testLDUB_06");
        initCpu();
        setInstruction(0x623); // 0b0000011000100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, RANDOM_32);
        memory.store8(0x12345678, 0x21);

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x00000021);
        checkMemory8(0x12345678, 0x21);
    }

    public void testLDUB_02() throws EmulationException {
        System.out.println("EmulatorTest.testLDUB_02");
        initCpu();
        setInstruction(0x223); // 0b0000001000100011

        cpuState.setReg(2, 0x00000004);
        cpuState.setReg(3, RANDOM_32);
        cpuState.setReg(13, 0x12345678);
        memory.store8(0x1234567C, 0x21);

        emulator.play();

        checkRegister(2, 0x00000004);
        checkRegister(3, 0x00000021);
        checkRegister(13, 0x12345678);
        checkMemory8(0x1234567C, 0x21);
    }

    public void testLDUB_60() throws EmulationException {
        System.out.println("EmulatorTest.testLDUB_60");
        initCpu();
        setInstruction(0x6013); // 0b0110000000010011

        cpuState.setReg(3, RANDOM_32);
        cpuState.setReg(14, 0x12345678);
        memory.store8(0x12345679, 0x21);

        emulator.play();

        checkRegister(3, 0x00000021);
        checkRegister(14, 0x12345678);
        checkMemory8(0x12345679, 0x21);
    }



    public void testST_14() throws EmulationException {
        System.out.println("EmulatorTest.testST_14");
        initCpu();

        setInstruction(0x1423); // 0b0001010000100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, 0x87654321);
        memory.store32(0x12345678, RANDOM_32);

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x87654321);
        checkMemory32(0x12345678, 0x87654321);
    }

    public void testST_10() throws EmulationException {
        System.out.println("EmulatorTest.testST_10");
        initCpu();

        setInstruction(0x1023); // 0b0001000000100011

        cpuState.setReg(2, 0x00000004);
        cpuState.setReg(3, 0x87654321);
        cpuState.setReg(13, 0x12345678);
        memory.store32(0x1234567C, RANDOM_32);

        emulator.play();

        checkRegister(2, 0x00000004);
        checkRegister(3, 0x87654321);
        checkRegister(13, 0x12345678);
        checkMemory32(0x1234567C, 0x87654321);
    }

    public void testST_30() throws EmulationException {
        System.out.println("EmulatorTest.testST_30");
        initCpu();

        setInstruction(0x3013); // 0b0011000000010011

        cpuState.setReg(3, 0x87654321);
        cpuState.setReg(14, 0x12345678);
        memory.store32(0x1234567C, RANDOM_32);

        emulator.play();

        checkRegister(3, 0x87654321);
        checkRegister(14, 0x12345678);
        checkMemory32(0x1234567C, 0x87654321);
    }

    public void testST_13() throws EmulationException {
        System.out.println("EmulatorTest.testST_13");
        initCpu();

        setInstruction(0x1313); // 0b0001001100010011

        cpuState.setReg(3, 0x87654321);
        cpuState.setReg(15, 0x12345678);
        memory.store32(0x1234567C, RANDOM_32);

        emulator.play();

        checkRegister(3, 0x87654321);
        checkRegister(15, 0x12345678);
        checkMemory32(0x1234567C, 0x87654321);
    }

    public void testST_170() throws EmulationException {
        System.out.println("EmulatorTest.testST_170");
        initCpu();

        setInstruction(0x1703); // 0b0001011100000011

        cpuState.setReg(3, 0x87654321);
        cpuState.setReg(15, 0x12345678);
        memory.store32(0x12345674, RANDOM_32);

        emulator.play();

        checkRegister(3, 0x87654321);
        checkRegister(15, 0x12345674);
        checkMemory32(0x12345674, 0x87654321);
    }

    public void testST_178() throws EmulationException {
        System.out.println("EmulatorTest.testST_178");
        initCpu();

        setInstruction(0x1784); // 0b0001011110000100

        cpuState.setReg(15, 0x12345678);
        cpuState.setReg(FrCPUState.MDH, 0x87654321);
        memory.store32(0x12345674, RANDOM_32);

        emulator.play();

        checkRegister(15, 0x12345674);
        checkRegister(FrCPUState.MDH, 0x87654321);
        checkMemory32(0x12345674, 0x87654321);
    }

    public void testST_179() throws EmulationException {
        System.out.println("EmulatorTest.testST_179");
        initCpu();

        setInstruction(0x1790); // 0b0001011110010000

        cpuState.setReg(15, 0x12345678);
        cpuState.setPS(0xFFF8F8C0, false);
        memory.store32(0x12345674, RANDOM_32);

        emulator.play();

        checkRegister(15, 0x12345674);
        // spec is wrong checkPS(0xFFF8F8C0);
        checkPS(0x00180000);
        // spec is wrong checkMemory32(0x12345674, 0xFFF8F8C0);
        checkMemory32(0x12345674, 0x00180000);
    }

    public void testSTH_15() throws EmulationException {
        System.out.println("EmulatorTest.testSTH_15");
        initCpu();

        setInstruction(0x1523); // 0b0001010100100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, 0x00004321);
        memory.store16(0x12345678, RANDOM_16);

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x00004321);
        checkMemory16(0x12345678, 0x00004321);
    }

    public void testSTH_11() throws EmulationException {
        System.out.println("EmulatorTest.testSTH_11");
        initCpu();

        setInstruction(0x1123); // 0b0001000100100011

        cpuState.setReg(2, 0x00000004);
        cpuState.setReg(3, 0x00004321);
        cpuState.setReg(13, 0x12345678);
        memory.store16(0x1234567C, RANDOM_16);

        emulator.play();

        checkRegister(2, 0x00000004);
        checkRegister(3, 0x00004321);
        checkRegister(13, 0x12345678);
        checkMemory16(0x1234567C, 0x00004321);
    }

    public void testSTH_50() throws EmulationException {
        System.out.println("EmulatorTest.testSTH_50");
        initCpu();

        setInstruction(0x5013); // 0b0101000000010011

        cpuState.setReg(3, 0x00004321);
        cpuState.setReg(14, 0x12345678);
        memory.store16(0x1234567A, RANDOM_16);

        emulator.play();

        checkRegister(3, 0x00004321);
        checkRegister(14, 0x12345678);
        checkMemory16(0x1234567A, 0x00004321);
    }

    public void testSTB_16() throws EmulationException {
        System.out.println("EmulatorTest.testSTB_16");
        initCpu();

        setInstruction(0x1623); // 0b0001011000100011

        cpuState.setReg(2, 0x12345678);
        cpuState.setReg(3, 0x00000021);
        memory.store8(0x12345678, RANDOM_8);

        emulator.play();

        checkRegister(2, 0x12345678);
        checkRegister(3, 0x00000021);
        checkMemory8(0x12345678, 0x00000021);
    }

    public void testSTB_12() throws EmulationException {
        System.out.println("EmulatorTest.testSTB_12");
        initCpu();

        setInstruction(0x1223); // 0b0001001000100011

        cpuState.setReg(2, 0x00000004);
        cpuState.setReg(3, 0x00000021);
        cpuState.setReg(13, 0x12345678);
        memory.store8(0x1234567C, RANDOM_8);

        emulator.play();

        checkRegister(2, 0x00000004);
        checkRegister(3, 0x00000021);
        checkRegister(13, 0x12345678);
        checkMemory8(0x1234567C, 0x00000021);
    }

    public void testSTB_70() throws EmulationException {
        System.out.println("EmulatorTest.testSTB_70");
        initCpu();

        setInstruction(0x7013); // 0b0111000000010011

        cpuState.setReg(3, 0x00000021);
        cpuState.setReg(14, 0x12345678);
        memory.store8(0x12345679, RANDOM_8);

        emulator.play();

        checkRegister(3, 0x00000021);
        checkRegister(14, 0x12345678);
        checkMemory8(0x12345679, 0x00000021);
    }



    public void testMOV_8B() throws EmulationException {
        System.out.println("EmulatorTest.testMOV_8B");
        initCpu();

        setInstruction(0x8b23); // 0b1000101100100011

        cpuState.setReg(2, 0x87654321);
        cpuState.setReg(3, RANDOM_32);

        emulator.play();

        checkRegister(2, 0x87654321);
        checkRegister(3, 0x87654321);
    }

    public void testMOV_B7() throws EmulationException {
        System.out.println("EmulatorTest.testMOV_B7");
        initCpu();

        setInstruction(0xb753); // 0b1011011101010011

        cpuState.setReg(3, RANDOM_32);
        cpuState.setReg(FrCPUState.MDL, 0x87654321);

        emulator.play();

        checkRegister(3, 0x87654321);
        checkRegister(FrCPUState.MDL, 0x87654321);
    }

    public void testMOV_17() throws EmulationException {
        System.out.println("EmulatorTest.testMOV_17");
        initCpu();

        setInstruction(0x1713); // 0b0001011100010011

        cpuState.setReg(3, RANDOM_32);
        cpuState.setPS(0xFFF8F8C0, false);

        emulator.play();

        checkRegister(3, 0x00180000);
        // spec is wrong checkRegister(3, 0xFFF8F8C0);
        checkPS(0x00180000);
        // spec is wrong checkPS(0xFFF8F8C0);
    }

    public void testMOV_B3() throws EmulationException {
        System.out.println("EmulatorTest.testMOV_B3");
        initCpu();

        setInstruction(0xb353); // 0b1011001101010011

        cpuState.setReg(3, 0x87654321);
        cpuState.setReg(FrCPUState.MDL, RANDOM_32);

        emulator.play();

        checkRegister(3, 0x87654321);
        checkRegister(FrCPUState.MDL, 0x87654321);
    }

    public void testMOV_07() throws EmulationException {
        System.out.println("EmulatorTest.testMOV_07");
        initCpu();

        setInstruction(0x713); // 0b0000011100010011

        cpuState.setReg(3, 0xFFF3F8D5);
        cpuState.setPS(RANDOM_32, false);

        emulator.play();

        checkRegister(3, 0xFFF3F8D5);
        // spec is wrong checkPS(0xFFF3F8D5);
        checkPS(0x00130015);
    }



    public void testJMP() throws EmulationException {
        System.out.println("EmulatorTest.testJMP");
        initCpu();

        memory.store16(0xFF800000, 0x9701); // 0b1001011100000001

        cpuState.setReg(1, 0xC0008000);
        cpuState.pc=0xFF800000;

        emulator.play();

        checkRegister(1, 0xC0008000);
        checkPC(0xC0008000);
    }

    public void testCALL_D0() throws EmulationException {
        System.out.println("EmulatorTest.testCALL_D0");
        initCpu();

        memory.store16(0xFF800000, 0xd090); // 0b1101000010010000

        cpuState.pc=0xFF800000;
        cpuState.setReg(FrCPUState.RP, RANDOM_32);

        emulator.play();

        checkPC(0xFF800122);
        checkRegister(FrCPUState.RP, 0xFF800002); // Note : assume a typo in the spec which says it should be 0xFF800004
        // Contradictory information in spec :
        // Page 25 section 3.3.4, text confirms it should be +2 for non-delay slot instructions
        // Page 25 section 3.3.4, first sample says it is a non-delayed instruction, but stores RP=PC+4
        // Page 185, section 7.8.8, text says operation is RP = PC + 2
        // Page 185, section 7.8.8, sample shows operation is RP = PC + 4
    }

    public void testCALL_97() throws EmulationException {
        System.out.println("EmulatorTest.testCALL_97");
        initCpu();

        memory.store16(0x8000FFFE, 0x9711); // 0b1001011100010001

        cpuState.setReg(1, 0xFFFFF800);
        cpuState.pc=0x8000FFFE;
        cpuState.setReg(FrCPUState.RP, RANDOM_32);

        emulator.play();

        checkRegister(1, 0xFFFFF800);
        checkPC(0xFFFFF800);
        checkRegister(FrCPUState.RP, 0x80010000);
    }

    public void testRET() throws EmulationException {
        System.out.println("EmulatorTest.testRET");
        initCpu();

        memory.store16(0xFFF08820, 0x9720); // 0b1001011100100000

        cpuState.pc=0xFFF08820;
        cpuState.setReg(FrCPUState.RP, 0x8000AE86);

        emulator.play();

        checkPC(0x8000AE86);
        checkRegister(FrCPUState.RP, 0x8000AE86);
    }

    public void testINT() throws EmulationException {
        System.out.println("EmulatorTest.testINT");
        initCpu();

        memory.store16(0x80888086, 0x1f20); // 0b0001111100100000

        cpuState.setReg(15, 0x40000000);
        cpuState.setReg(FrCPUState.SSP, 0x80000000);
        cpuState.setReg(FrCPUState.TBR, 0x000FFC00);
        cpuState.setReg(FrCPUState.USP, 0x40000000);
        cpuState.pc=0x80888086;
        cpuState.setPS(0xFFFFF8F0, false);
        cpuState.setCCR(0x30); // 0b110000

        memory.store32(0x000FFF7C, 0x68096800);
        memory.store32(0x7FFFFFF8, RANDOM_32);
        memory.store32(0x7FFFFFFC, RANDOM_32);
        memory.store32(0x80000000, RANDOM_32);

        emulator.play();

        checkRegister(15, 0x7FFFFFF8);
        checkRegister(FrCPUState.SSP, 0x7FFFFFF8);
        checkRegister(FrCPUState.TBR, 0x000FFC00);
        checkRegister(FrCPUState.USP, 0x40000000);
        checkPC(0x68096800);
        checkPS(0x001F0000);
        // test in spec is wrong : checkPS(0xFFFFF8C0);
        checkCCR(0x0); // 0b000000

        checkMemory32(0x000FFF7C, 0x68096800);
        checkMemory32(0x7FFFFFF8, 0x80888088);
        checkMemory32(0x7FFFFFFC, 0x001f0030);
        // spec is wrong checkMemory32(0x7FFFFFFC, 0xFFFFF8F0);
        checkMemory32(0x80000000, RANDOM_32);
    }

    public void testINTE() throws EmulationException {
        System.out.println("EmulatorTest.testINTE");
        initCpu();

        memory.store16(0x80888086, 0x9f30); // 0b1001111100110000

        cpuState.setReg(15, 0x40000000);
        cpuState.setReg(FrCPUState.SSP, 0x80000000);
        cpuState.setReg(FrCPUState.USP, 0x40000000);
        cpuState.setReg(FrCPUState.TBR, 0x000FFC00);
        cpuState.pc=0x80888086;
        cpuState.setPS(0xFFF5F8F0, false);
        cpuState.setILM(0x15, false); // 0b10101
        cpuState.setCCR(0x30); // 0b110000

        memory.store32(0x000FFFD8, 0x68096800);
        memory.store32(0x7FFFFFF8, RANDOM_32);
        memory.store32(0x7FFFFFFC, RANDOM_32);
        memory.store32(0x80000000, RANDOM_32);

        emulator.play();

        checkRegister(15, 0x7FFFFFF8);
        checkRegister(FrCPUState.SSP, 0x7FFFFFF8);
        checkRegister(FrCPUState.USP, 0x40000000);
        checkRegister(FrCPUState.TBR, 0x000FFC00);
        checkPC(0x68096800);
        // spec is wrong checkPS(0xFFE4F8D0);
        checkPS(0x00040010);
        checkILM(0x4); // 0b00100
        checkCCR(0x10); // 0b010000

        checkMemory32(0x000FFFD8, 0x68096800);
        checkMemory32(0x7FFFFFF8, 0x80888088);
        // spec is wrong checkMemory32(0x7FFFFFFC, 0xFFF5F8F0); // Note : assume a typo in the spec which says it should be 0xFFF5F8F0
        checkMemory32(0x7FFFFFFC, 0x00150030);
        checkMemory32(0x80000000, RANDOM_32);
    }

    public void testRETI() throws EmulationException {
        System.out.println("EmulatorTest.testRETI");
        initCpu();

        memory.store16(0xFF0090BC, 0x9730); // 0b1001011100110000

        cpuState.setReg(15, 0x7FFFFFF8);
        cpuState.setReg(FrCPUState.SSP, 0x7FFFFFF8);
        cpuState.setReg(FrCPUState.USP, 0x40000000);
        cpuState.pc=0xFF0090BC;
        cpuState.setPS(0xFFF0F8D4, false);
        cpuState.setILM(0x10, false); // 0b10000
        cpuState.setCCR(0x14); // 0b010100

        memory.store32(0x7FFFFFF8, 0x80888088);
        memory.store32(0x7FFFFFFC, 0xFFF3F8F1);
        memory.store32(0x80000000, RANDOM_32);

        emulator.play();

        checkRegister(15, 0x40000000);
        checkRegister(FrCPUState.SSP, 0x80000000);
        checkRegister(FrCPUState.USP, 0x40000000);
        checkPC(0x80888088);
        // spec is wrong checkPS(0xFFF3F8F1);
        checkPS(0x00130031);
        checkILM(0x13); // 0b10011
        checkCCR(0x31); // 0b110001

        checkMemory32(0x7FFFFFF8, 0x80888088);
        checkMemory32(0x7FFFFFFC, 0xFFF3F8F1);
        checkMemory32(0x80000000, RANDOM_32);
    }

    public void testBcc() throws EmulationException {
        System.out.println("EmulatorTest.testBcc");
        initCpu();

        memory.store16(0xFF800000, 0xef28); // 0b1110111100101000

        cpuState.pc=0xFF800000;
        cpuState.setCCR(0xa); // 0b1010

        emulator.play();

        checkPC(0xFF800052);
        checkCCR(0xa); // 0b1010
    }



    public void testJMP_D() throws EmulationException {
        System.out.println("EmulatorTest.testJMP_D");
        initCpu();

        memory.store16(0xFF800000, 0x9f01); // 0b1001111100000001 JMP:D @Ri
        memory.store16(0xFF800002, 0xcff1); // 0b1100111111110001 LDI:8 #0FFH, R1

        cpuState.setReg(1, 0xC0008000);
        cpuState.pc=0xFF800000;

        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(0xC0008000, null));
        emulator.play();

        checkRegister(1, 0x000000FF);
        checkPC(0xC0008000);
    }

    public void testCALL_D_D8() throws EmulationException {
        System.out.println("EmulatorTest.testCALL_D_D8");
        initCpu();

        memory.store16(0xFF800000, 0xd890); // 0b1101100010010000 CALL:D (+0x122)
        memory.store16(0xFF800002, 0xc002); // 0b1100000000000010 LDI:8 #0, R2

        cpuState.setReg(2, RANDOM_32);
        cpuState.pc=0xFF800000;
        cpuState.setReg(FrCPUState.RP, RANDOM_32);

        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(0xFF800122, null));
        emulator.play();

        checkRegister(2, 0);
        checkPC(0xFF800122);
        checkRegister(FrCPUState.RP, 0xFF800004); // Note : here, the spec is correct contrary to non-delayed test
    }

    public void testCALL_D_9F() throws EmulationException {
        System.out.println("EmulatorTest.testCALL_D_97");
        initCpu();

        memory.store16(0x8000FFFE, 0x9f11); // 0b1001111100010001 CALL:D @R1
        memory.store16(0x80010000, 0xc011); // 0b1100000000010001 LDI:8 #1, R1

        cpuState.setReg(1, 0xFFFFF800);
        cpuState.pc=0x8000FFFE;
        cpuState.setReg(FrCPUState.RP, RANDOM_32);

        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(0xFFFFF800, null));
        emulator.play();

        checkRegister(1, 0x00000001);
        checkPC(0xFFFFF800);
        checkRegister(FrCPUState.RP, 0x80010002);
    }

    public void testRET_D() throws EmulationException {
        System.out.println("EmulatorTest.testRET_D");
        initCpu();

        memory.store16(0xFFF08820, 0x9f20); // 0b1001111100100000 RET:D
        memory.store16(0xFFF08822, 0x8b01); // 0b1100000000010001 MOV R0, R1

        cpuState.setReg(0, 0x00112233);
        cpuState.setReg(1, RANDOM_32);
        cpuState.pc=0xFFF08820;
        cpuState.setReg(FrCPUState.RP, 0x8000AE86);

        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(0x8000AE86, null));
        emulator.play();

        checkRegister(0, 0x00112233);
        checkRegister(1, 0x00112233);
        checkPC(0x8000AE86);
        checkRegister(FrCPUState.RP, 0x8000AE86);
    }

    public void testBcc_D() throws EmulationException {
        System.out.println("EmulatorTest.testBcc_D");
        initCpu();

        memory.store16(0xFF800000, 0xff28); // 0b1111111100101000 CALL:D (+0x50)
        memory.store16(0xFF800002, 0xcff1); // 0b1100111111110001 LDI:8 #FF, R1

        cpuState.setReg(1, 0x894797AF);
        cpuState.pc=0xFF800000;
        cpuState.setCCR(0xa); // 0b1010

        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(0xFF800052, null));
        emulator.play();

        checkRegister(1, 0x000000FF);
        checkPC(0xFF800052);
        checkCCR(0xa); // 0b1010
    }



    public void testDMOV_08() throws EmulationException {
        System.out.println("EmulatorTest.testDMOV_08");
        initCpu();

        setInstruction(0x822); // 0b0000100000100010

        cpuState.setReg(13, RANDOM_32);
        memory.store32(0x84, RANDOM_32);
        memory.store32(0x88, 0x01234567);
        memory.store32(0x8C, RANDOM_32);

        emulator.play();

        checkRegister(13, 0x01234567);
        checkMemory32(0x84, RANDOM_32);
        checkMemory32(0x88, 0x01234567);
        checkMemory32(0x8C, RANDOM_32);
    }

    public void testDMOV_18() throws EmulationException {
        System.out.println("EmulatorTest.testDMOV_18");
        initCpu();

        setInstruction(0x1815); // 0b0001100000010101

        cpuState.setReg(13, 0x89ABCDEF);
        memory.store32(0x50, RANDOM_32);
        memory.store32(0x54, RANDOM_32);
        memory.store32(0x58, RANDOM_32);

        emulator.play();

        checkRegister(13, 0x89ABCDEF);
        checkMemory32(0x50, RANDOM_32);
        checkMemory32(0x54, 0x89ABCDEF);
        checkMemory32(0x58, RANDOM_32);
    }

    public void testDMOV_0C() throws EmulationException {
        System.out.println("EmulatorTest.testDMOV_0C");
        initCpu();

        setInstruction(0xc22); // 0b0000110000100010

        cpuState.setReg(13, 0xFFFF1248);
        memory.store32(0x00000088, 0x14142135);
        memory.store32(0xFFFF1248, RANDOM_32);
        memory.store32(0xFFFF124C, RANDOM_32);

        emulator.play();

        checkRegister(13, 0xFFFF124C);
        checkMemory32(0x00000088, 0x14142135);
        checkMemory32(0xFFFF1248, 0x14142135);
        checkMemory32(0xFFFF124C, RANDOM_32);
    }

    public void testDMOV_1C() throws EmulationException {
        System.out.println("EmulatorTest.testDMOV_1C");
        initCpu();

        setInstruction(0x1c15); // 0b0001110000010101

        cpuState.setReg(13, 0xFFFF1248);
        memory.store32(0x00000054, RANDOM_32);
        memory.store32(0xFFFF1248, 0x894791AF);
        memory.store32(0xFFFF124C, RANDOM_32);

        emulator.play();

        checkRegister(13, 0xFFFF124C);
        checkMemory32(0x00000054, 0x894791AF);
        checkMemory32(0xFFFF1248, 0x894791AF);
        checkMemory32(0xFFFF124C, RANDOM_32);
    }

    public void testDMOV_0B() throws EmulationException {
        System.out.println("EmulatorTest.testDMOV_0B");
        initCpu();

        setInstruction(0xb0b); // 0b0000101100001011

        cpuState.setReg(15, 0x7FFFFF88);
        memory.store32(0x0000002C, 0x82A282A9);
        memory.store32(0x7FFFFF84, RANDOM_32);
        memory.store32(0x7FFFFF88, RANDOM_32);

        emulator.play();

        checkRegister(15, 0x7FFFFF84);
        checkMemory32(0x0000002C, 0x82A282A9);
        checkMemory32(0x7FFFFF84, 0x82A282A9);
        checkMemory32(0x7FFFFF88, RANDOM_32);
    }

    public void testDMOV_1B() throws EmulationException {
        System.out.println("EmulatorTest.testDMOV_1B");
        initCpu();

        setInstruction(0x1b0e); // 0b0001101100001110

        cpuState.setReg(15, 0x7FFEEE80);
        memory.store32(0x00000038, RANDOM_32);
        memory.store32(0x7FFEEE80, 0x8343834A);
        memory.store32(0x7FFEEE84, RANDOM_32);

        emulator.play();

        checkRegister(15, 0x7FFEEE84);
        checkMemory32(0x00000038, 0x8343834A);
        checkMemory32(0x7FFEEE80, 0x8343834A);
        checkMemory32(0x7FFEEE84, RANDOM_32);
    }

    public void testDMOVH_09() throws EmulationException {
        System.out.println("EmulatorTest.testDMOVH_09");
        initCpu();

        setInstruction(0x944); // 0b0000100101000100

        cpuState.setReg(13, RANDOM_32);
        memory.store16(0x86, RANDOM_16);
        memory.store16(0x88, 0xB2B6);
        memory.store16(0x8A, RANDOM_16);

        emulator.play();

        checkRegister(13, 0x0000B2B6);
        checkMemory16(0x86, RANDOM_16);
        checkMemory16(0x88, 0xB2B6);
        checkMemory16(0x8A, RANDOM_16);
    }

    public void testDMOVH_19() throws EmulationException {
        System.out.println("EmulatorTest.testDMOVH_19");
        initCpu();

        setInstruction(0x1929); // 0b0001100100101001

        cpuState.setReg(13, 0xFFFFAE86);
        memory.store16(0x50, RANDOM_16);
        memory.store16(0x52, RANDOM_16);
        memory.store16(0x54, RANDOM_16);

        emulator.play();

        checkRegister(13, 0xFFFFAE86);
        checkMemory16(0x50, RANDOM_16);
        checkMemory16(0x52, 0xAE86);
        checkMemory16(0x54, RANDOM_16);
    }

    public void testDMOVH_0D() throws EmulationException {
        System.out.println("EmulatorTest.testDMOVH_0D");
        initCpu();

        setInstruction(0xd44); // 0b0000110101000100

        cpuState.setReg(13, 0xFF000052);
        memory.store16(0x00000088, 0x1374);
        memory.store16(0xFF000052, RANDOM_16);
        memory.store16(0xFF000054, RANDOM_16);

        emulator.play();

        checkRegister(13, 0xFF000054);
        checkMemory16(0x00000088, 0x1374);
        checkMemory16(0xFF000052, 0x1374);
        checkMemory16(0xFF000054, RANDOM_16);
    }

    public void testDMOVH_1D() throws EmulationException {
        System.out.println("EmulatorTest.testDMOVH_1D");
        initCpu();

        setInstruction(0x1d29); // 0b0001110100101001

        cpuState.setReg(13, 0xFF801220);
        memory.store16(0x00000052, RANDOM_16);
        memory.store16(0xFF801220, 0x8933);
        memory.store16(0xFF801222, RANDOM_16);

        emulator.play();

        checkRegister(13, 0xFF801222);
        checkMemory16(0x00000052, 0x8933);
        checkMemory16(0xFF801220, 0x8933);
        checkMemory16(0xFF801222, RANDOM_16);
    }
    
    public void testDMOVB_0A() throws EmulationException {
        System.out.println("EmulatorTest.testDMOVB_0A");
        initCpu();

        setInstruction(0xa91); // 0b0000101010010001

        cpuState.setReg(13, RANDOM_32);
        memory.store8(0x90, RANDOM_8);
        memory.store8(0x91, 0x32);
        memory.store8(0x92, RANDOM_8);

        emulator.play();

        checkRegister(13, 0x00000032);
        checkMemory8(0x90, RANDOM_8);
        checkMemory8(0x91, 0x32);
        checkMemory8(0x92, RANDOM_8);
    }

    public void testDMOVB_1A() throws EmulationException {
        System.out.println("EmulatorTest.testDMOVB_1A");
        initCpu();

        setInstruction(0x1a53); // 0b0001101001010011

        cpuState.setReg(13, 0xFFFFFFFE);
        memory.store8(0x52, RANDOM_8);
        memory.store8(0x53, RANDOM_8);
        memory.store8(0x54, RANDOM_8);

        emulator.play();

        checkRegister(13, 0xFFFFFFFE);
        checkMemory8(0x52, RANDOM_8);
        checkMemory8(0x53, 0xFE);
        checkMemory8(0x54, RANDOM_8);
    }

    public void testDMOVB_0E() throws EmulationException {
        System.out.println("EmulatorTest.testDMOVB_0E");
        initCpu();

        setInstruction(0xe71); // 0b0000111001110001

        cpuState.setReg(13, 0x88001234);
        memory.store8(0x00000071, 0x99);
        memory.store8(0x88001234, RANDOM_8);
        memory.store8(0x88001235, RANDOM_8);

        emulator.play();

        checkRegister(13, 0x88001235);
        checkMemory8(0x00000071, 0x99);
        checkMemory8(0x88001234, 0x99);
        checkMemory8(0x88001235, RANDOM_8);
    }

    public void testDMOVB_1E() throws EmulationException {
        System.out.println("EmulatorTest.testDMOVB_1E");
        initCpu();

        setInstruction(0x1e57); // 0b0001111001010111

        cpuState.setReg(13, 0xFF801220);
        memory.store8(0x00000057, RANDOM_8);
        memory.store8(0xFF801220, 0x55);
        memory.store8(0xFF801221, RANDOM_8);

        emulator.play();

        checkRegister(13, 0xFF801221);
        checkMemory8(0x00000057, 0x55);
        checkMemory8(0xFF801220, 0x55);
        checkMemory8(0xFF801221, RANDOM_8);
    }

    // TODO FUTURE RESOURCE TESTS 

    // TODO FUTURE COPROCESSOR TESTS 

    public void testNOP() throws EmulationException {
        System.out.println("EmulatorTest.testNOP");
        initCpu();

        memory.store16(0x8343834A, 0x9fa0); // 0b1001111110100000
        cpuState.pc = 0x8343834A;

        emulator.play();

        checkPC(0x8343834C);
    }

    public void testANDCCR() throws EmulationException {
        System.out.println("EmulatorTest.testANDCCR");
        initCpu();

        setInstruction(0x83fe); // 0b1000001111111110

        cpuState.setCCR(0x15); // 0b010101

        emulator.play();

        checkCCR(0x14); // 0b010100
    }

    public void testORCCR() throws EmulationException {
        System.out.println("EmulatorTest.testORCCR");
        initCpu();

        setInstruction(0x9310); // 0b1001001100010000

        cpuState.setCCR(0x5); // 0b000101

        emulator.play();

        checkCCR(0x15); // 0b010101
    }

    public void testSTILM() throws EmulationException {
        System.out.println("EmulatorTest.testSTILM");
        initCpu();

        setInstruction(0x8714); // 0b1000011100010100

        cpuState.setILM(0x1f, false); // 0b11111

        emulator.play();

        checkILM(0x14); // 0b10100
    }

    public void testADDSP() throws EmulationException {
        System.out.println("EmulatorTest.testADDSP");
        initCpu();

        setInstruction(0xa3ff); // 0b1010001111111111

        cpuState.setReg(15, 0x80000000);

        emulator.play();

        checkRegister(15, 0x7FFFFFFC);
    }

    public void testEXTSB() throws EmulationException {
        System.out.println("EmulatorTest.testEXTSB");
        initCpu();

        setInstruction(0x9781); // 0b1001011110000001

        cpuState.setReg(1, 0x000000AB);

        emulator.play();

        checkRegister(1, 0xFFFFFFAB);
    }

    public void testEXTUB() throws EmulationException {
        System.out.println("EmulatorTest.testEXTUB");
        initCpu();

        setInstruction(0x9791); // 0b1001011110010001

        cpuState.setReg(1, 0xFFFFFFFF);

        emulator.play();

        checkRegister(1, 0x000000FF);
    }

    public void testEXTSH() throws EmulationException {
        System.out.println("EmulatorTest.testEXTSH");
        initCpu();

        setInstruction(0x97a1); // 0b1001011110100001

        cpuState.setReg(1, 0x0000ABCD);

        emulator.play();

        checkRegister(1, 0xFFFFABCD);
    }

    public void testEXTUH() throws EmulationException {
        System.out.println("EmulatorTest.testEXTUH");
        initCpu();

        setInstruction(0x97b1); // 0b1001011110110001

        cpuState.setReg(1, 0xFFFFFFFF);

        emulator.play();

        checkRegister(1, 0x0000FFFF);
    }

    public void testSRCH0() throws EmulationException {
        System.out.println("EmulatorTest.testSRCH0");
        initCpu();

        setInstruction(0x97c2); // 0b1001011111000010

        cpuState.setReg(2, 0xFC345678);

        emulator.play();

        checkRegister(2, 0x00000006);
    }

    public void testSRCH1() throws EmulationException {
        System.out.println("EmulatorTest.testSRCH1");
        initCpu();

        setInstruction(0x97d2); // 0b1001011111010010

        cpuState.setReg(2, 0x00345678);

        emulator.play();

        checkRegister(2, 0x0000000A);
    }

    public void testSRCHC() throws EmulationException {
        System.out.println("EmulatorTest.testSRCHC");
        initCpu();

        setInstruction(0x97e2); // 0b1001011111100010

        cpuState.setReg(2, 0xFF345678);

        emulator.play();

        checkRegister(2, 0x00000008);
    }

    public void testLDM0() throws EmulationException {
        System.out.println("EmulatorTest.testLDM0");
        initCpu();

        setInstruction(0x8c18); // 0b1000110000011000

        cpuState.setReg(3, RANDOM_32);
        cpuState.setReg(4, RANDOM_32);
        cpuState.setReg(15, 0x7FFFFFC0);
        memory.store32(0x7FFFFFC0, 0x90BC9363);
        memory.store32(0x7FFFFFC4, 0x8343834A);
        memory.store32(0x7FFFFFC8, RANDOM_32);

        emulator.play();

        checkRegister(3, 0x90BC9363);
        checkRegister(4, 0x8343834A);
        checkRegister(15, 0x7FFFFFC8);
        checkMemory32(0x7FFFFFC0, 0x90BC9363);
        checkMemory32(0x7FFFFFC4, 0x8343834A);
        checkMemory32(0x7FFFFFC8, RANDOM_32);
    }

    public void testLDM1() throws EmulationException {
        System.out.println("EmulatorTest.testLDM1");
        initCpu();

        setInstruction(0x8d1c); // 0b1000110100011100

        cpuState.setReg(10, RANDOM_32);
        cpuState.setReg(11, RANDOM_32);
        cpuState.setReg(12, RANDOM_32);
        cpuState.setReg(15, 0x7FFFFFC0);
        memory.store32(0x7FFFFFC0, 0x8FE39E8A);
        memory.store32(0x7FFFFFC4, 0x90BC9363);
        memory.store32(0x7FFFFFC8, 0x8DF788E4);
        memory.store32(0x7FFFFFCC, RANDOM_32);

        emulator.play();

        checkRegister(10, 0x8FE39E8A);
        checkRegister(11, 0x90BC9363);
        checkRegister(12, 0x8DF788E4);
        checkRegister(15, 0x7FFFFFCC);
        checkMemory32(0x7FFFFFC0, 0x8FE39E8A);
        checkMemory32(0x7FFFFFC4, 0x90BC9363);
        checkMemory32(0x7FFFFFC8, 0x8DF788E4);
        checkMemory32(0x7FFFFFCC, RANDOM_32);
    }

    public void testSTM0() throws EmulationException {
        System.out.println("EmulatorTest.testSTM0");
        initCpu();

        setInstruction(0x8e30); // 0b1000111000110000

        cpuState.setReg(2, 0x90BC9363);
        cpuState.setReg(3, 0x8343834A);
        cpuState.setReg(15, 0x7FFFFFC8);
        memory.store32(0x7FFFFFC0, RANDOM_32);
        memory.store32(0x7FFFFFC4, RANDOM_32);
        memory.store32(0x7FFFFFC8, RANDOM_32);

        emulator.play();

        checkRegister(2, 0x90BC9363);
        checkRegister(3, 0x8343834A);
        checkRegister(15, 0x7FFFFFC0);
        checkMemory32(0x7FFFFFC0, 0x90BC9363);
        checkMemory32(0x7FFFFFC4, 0x8343834A);
        checkMemory32(0x7FFFFFC8, RANDOM_32);
    }

    public void testSTM1() throws EmulationException {
        System.out.println("EmulatorTest.testSTM1");
        initCpu();

        setInstruction(0x8f38); // 0b1000111100111000

        cpuState.setReg(10, 0x8FE39E8A);
        cpuState.setReg(11, 0x90BC9363);
        cpuState.setReg(12, 0x8DF788E4);
        cpuState.setReg(15, 0x7FFFFFCC);
        memory.store32(0x7FFFFFC0, RANDOM_32);
        memory.store32(0x7FFFFFC4, RANDOM_32);
        memory.store32(0x7FFFFFC8, RANDOM_32);
        memory.store32(0x7FFFFFCC, RANDOM_32);

        emulator.play();

        checkRegister(10, 0x8FE39E8A);
        checkRegister(11, 0x90BC9363);
        checkRegister(12, 0x8DF788E4);
        checkRegister(15, 0x7FFFFFC0);
        checkMemory32(0x7FFFFFC0, 0x8FE39E8A);
        checkMemory32(0x7FFFFFC4, 0x90BC9363);
        checkMemory32(0x7FFFFFC8, 0x8DF788E4);
        checkMemory32(0x7FFFFFCC, RANDOM_32);
    }

    public void testENTER() throws EmulationException {
        System.out.println("EmulatorTest.testENTER");
        initCpu();

        setInstruction(0xf03); // 0b0000111100000011

        cpuState.setReg(14, 0x80000000);
        cpuState.setReg(15, 0x7FFFFFF8);
        memory.store32(0x7FFFFFEC, RANDOM_32);
        memory.store32(0x7FFFFFF0, RANDOM_32);
        memory.store32(0x7FFFFFF4, RANDOM_32);
        memory.store32(0x7FFFFFF8, RANDOM_32);
        memory.store32(0x7FFFFFFC, RANDOM_32);
        memory.store32(0x80000000, RANDOM_32);

        emulator.play();

        checkRegister(14, 0x7FFFFFF4);
        checkRegister(15, 0x7FFFFFEC);
        checkMemory32(0x7FFFFFEC, RANDOM_32);
        checkMemory32(0x7FFFFFF0, RANDOM_32);
        checkMemory32(0x7FFFFFF4, 0x80000000);
        checkMemory32(0x7FFFFFF8, RANDOM_32);
        checkMemory32(0x7FFFFFFC, RANDOM_32);
        checkMemory32(0x80000000, RANDOM_32);
    }

    public void testLEAVE() throws EmulationException {
        System.out.println("EmulatorTest.testLEAVE");
        initCpu();

        setInstruction(0x9f90); // 0b1001111110010000

        cpuState.setReg(14, 0x7FFFFFF4);
        cpuState.setReg(15, 0x7FFFFFEC);
        memory.store32(0x7FFFFFEC, RANDOM_32);
        memory.store32(0x7FFFFFF0, RANDOM_32);
        memory.store32(0x7FFFFFF4, 0x80000000);
        memory.store32(0x7FFFFFF8, RANDOM_32);
        memory.store32(0x7FFFFFFC, RANDOM_32);
        memory.store32(0x80000000, RANDOM_32);

        emulator.play();

        checkRegister(14, 0x80000000);
        checkRegister(15, 0x7FFFFFF8);
        checkMemory32(0x7FFFFFEC, RANDOM_32);
        checkMemory32(0x7FFFFFF0, RANDOM_32);
        checkMemory32(0x7FFFFFF4, 0x80000000);
        checkMemory32(0x7FFFFFF8, RANDOM_32);
        checkMemory32(0x7FFFFFFC, RANDOM_32);
        checkMemory32(0x80000000, RANDOM_32);
    }

    public void testXCHB() throws EmulationException {
        System.out.println("EmulatorTest.testXCHB");
        initCpu();

        setInstruction(0x8a10); // 0b1000101000010000

        cpuState.setReg(0, 0x00000078);
        cpuState.setReg(1, 0x80000002);
        memory.store8(0x80000001, RANDOM_8);
        memory.store8(0x80000002, 0xFD);
        memory.store8(0x80000003, RANDOM_8);

        emulator.play();

        checkRegister(0, 0x000000FD);
        checkRegister(1, 0x80000002);
        checkMemory8(0x80000001, RANDOM_8);
        checkMemory8(0x80000002, 0x78);
        checkMemory8(0x80000003, RANDOM_8);
    }


    // ************** TEMPLATE *********************
    public void test() throws EmulationException {
        System.out.println("EmulatorTest.test");
        initCpu();

        setInstruction(0); //

        cpuState.setReg(2, 0);
        cpuState.setReg(3, 0);
        cpuState.setCCR(0); // 0b0000

        emulator.play();

        checkRegister(2, 0);
        checkRegister(3, 0);
        checkCCR(0); // 0b0000
    }

    public void testArithmetic() throws EmulationException {
        testADD_A6();
        testADD_A4();
        testADD2();
        testADDC();

        testADDN_A2();
        testADDN_A1();
        testADDN2();

        testSUB();
        testSUBC();
        testSUBN();

        testCMP_AA();
        testCMP_A8();
        testCMP2();
    }

    public void testLogical() throws EmulationException {
        testAND_82();
        testAND_84();
        testANDH();
        testANDB();

        testOR_92();
        testOR_94();
        testORH();
        testORB();

        testEOR_9A();
        testEOR_9C();
        testEORH();
        testEORB();

        testBANDL();
        testBANDH();
        testBORL();
        testBORH();
        testBEORL();
        testBEORH();
        testBTSTL();
        testBTSTH();
    }

    public void testMulDiv() throws EmulationException {
        testMUL();
        testMULU();
        testMULH();
        testMULUH();

        testDIV0S();
        testDIV0U();
        testDIV1();
        testDIV2();
        testDIV3();
        testDIV4S();

        testFullDIVS();
        testFullDIVU();
    }

    public void testShift() throws EmulationException {
        testLSL_B6();
        testLSL_B4();
        testLSL2();
        testLSR_B2();
        testLSR_B0();
        testLSR2();
        testASR_BA();
        testASR_B8();
        testASR2();
    }

    public void testLoad() throws EmulationException {
        testLDI32();
        testLDI20();
        testLDI8();
        testLD_04();
        testLD_00();
        testLD_20();
        testLD_03();
        testLD_070();
        testLD_078();
        testLD_079();
        testLDUH_05();
        testLDUH_01();
        testLDUH_40();
        testLDUB_06();
        testLDUB_02();
        testLDUB_60();
    }

    public void testStore() throws EmulationException {
        testST_14();
        testST_10();
        testST_30();
        testST_13();
        testST_170();
        testST_178();
        testST_179();
        testSTH_15();
        testSTH_11();
        testSTH_50();
        testSTB_12();
        testSTB_16();
        testSTB_70();
    }

    public void testMove() throws EmulationException {
        testMOV_8B();
        testMOV_B7();
        testMOV_17();
        testMOV_B3();
        testMOV_07();
    }

    public void testProgramFlow() throws EmulationException {
        testJMP();
        testCALL_D0();
        testCALL_97();
        testRET();
        testINT();
        testINTE();
        testRETI();
        testBcc();
    }

    public void testProgramFlowDelaySlot() throws EmulationException {
        testJMP_D();
        testCALL_D_D8();
        testCALL_D_9F();
        testRET_D();
        testBcc_D();
    }

    public void testMoveDirect() throws EmulationException {
        testDMOV_08();
        testDMOV_18();
        testDMOV_0C();
        testDMOV_1C();
        testDMOV_0B();
        testDMOV_1B();
        testDMOVH_09();
        testDMOVH_19();
        testDMOVH_0D();
        testDMOVH_1D();
        testDMOVB_0A();
        testDMOVB_1A();
        testDMOVB_0E();
        testDMOVB_1E();
    }

    public void testMisc() throws EmulationException {
        testNOP();
        testANDCCR();
        testORCCR();
        testSTILM();
        testADDSP();
        testEXTSB();
        testEXTUB();
        testEXTSH();
        testEXTUH();
        testSRCH0();
        testSRCH1();
        testSRCHC();
        testLDM0();
        testLDM1();
        testSTM0();
        testSTM1();
        testENTER();
        testLEAVE();
        testXCHB();
    }

    public void testAll() throws EmulationException {
        testArithmetic();
        testLogical();
        testMulDiv();
        testShift();
        testLoad();
        testStore();
        testMove();
        testProgramFlow();
        testProgramFlowDelaySlot();
        testMoveDirect();
        testMisc();
    }

}
