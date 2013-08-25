package com.nikonhacker.emu;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.adConverter.AdConverter;
import com.nikonhacker.emu.peripherials.clock.ClockGenerator;
import com.nikonhacker.emu.peripherials.dmaController.DmaController;
import com.nikonhacker.emu.peripherials.interruptController.DummyInterruptController;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.keyCircuit.KeyCircuit;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.emu.peripherials.realtimeClock.RealtimeClock;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;

import java.util.List;

/**
 * A platform represents a microcontroller hardware in a given state: CPU, memory, interrupt controller,
 * clock generator, timers, i/o ports, serial interfaces, etc.
 * It has a pointer to the master clock giving the frequency to this platform
 * It also points to the devices depending on this microcontroller, like attached serial devices
 */
public class Platform {

    private MasterClock         masterClock;

    // The first 3 are so frequently used that we skip the getter and give them package access
    CPUState            cpuState;
    DebuggableMemory    memory;
    InterruptController interruptController = new DummyInterruptController();
    private ClockGenerator      clockGenerator;
    private ProgrammableTimer[] programmableTimers;
    private IoPort[]            ioPorts;
    private SerialInterface[]   serialInterfaces;
    private DmaController       dmaController;
    private AdConverter         adConverter;
    private RealtimeClock       realtimeClock;
    private KeyCircuit          keyCircuit;

    private List<SerialDevice>  serialDevices;


    public Platform(MasterClock masterClock) {
        this.masterClock = masterClock;
    }

    public MasterClock getMasterClock() {
        return masterClock;
    }

    public CPUState getCpuState() {
        return cpuState;
    }

    public void setCpuState(CPUState cpuState) {
        this.cpuState = cpuState;
    }

    public DebuggableMemory getMemory() {
        return memory;
    }

    public void setMemory(DebuggableMemory memory) {
        this.memory = memory;
    }

    public ClockGenerator getClockGenerator() {
        return clockGenerator;
    }

    public void setClockGenerator(ClockGenerator clockGenerator) {
        this.clockGenerator = clockGenerator;
    }

    public InterruptController getInterruptController() {
        return interruptController;
    }

    public void setInterruptController(InterruptController interruptController) {
        this.interruptController = interruptController;
    }

    public ProgrammableTimer[] getProgrammableTimers() {
        return programmableTimers;
    }

    public void setProgrammableTimers(ProgrammableTimer[] programmableTimers) {
        this.programmableTimers = programmableTimers;
    }

    public IoPort[] getIoPorts() {
        return ioPorts;
    }

    public void setIoPorts(IoPort[] ioPorts) {
        this.ioPorts = ioPorts;
    }

    public SerialInterface[] getSerialInterfaces() {
        return serialInterfaces;
    }

    public void setSerialInterfaces(SerialInterface[] serialInterfaces) {
        this.serialInterfaces = serialInterfaces;
    }

    public DmaController getDmaController() {
        return dmaController;
    }

    public void setDmaController(DmaController dmaController) {
        this.dmaController = dmaController;
    }

    public AdConverter getAdConverter() {
        return adConverter;
    }

    public void setAdConverter(AdConverter adConverter) {
        this.adConverter = adConverter;
    }

    public RealtimeClock getRealtimeClock() {
        return realtimeClock;
    }

    public void setRealtimeClock(RealtimeClock realtimeClock) {
        this.realtimeClock = realtimeClock;
    }

    public KeyCircuit getKeyCircuit() {
        return keyCircuit;
    }

    public void setKeyCircuit(KeyCircuit keyCircuit) {
        this.keyCircuit = keyCircuit;
    }

    public List<SerialDevice> getSerialDevices() {
        return serialDevices;
    }

    public void setSerialDevices(List<SerialDevice> serialDevices) {
        this.serialDevices = serialDevices;
    }
}
