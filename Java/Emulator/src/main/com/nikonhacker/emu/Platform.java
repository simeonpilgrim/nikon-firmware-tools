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
 * A platform represents a microcontroller hardware in a given state: CPU, memory, clock generator,
 * interrupt controller, timers, i/o ports, serial interfaces, etc.
 */
public class Platform {

    private MasterClock masterClock;
    CPUState         cpuState;
    DebuggableMemory memory;
    private ClockGenerator clockGenerator;
    InterruptController interruptController = new DummyInterruptController();
    private ProgrammableTimer[] programmableTimers;
    private IoPort[]            ioPorts;
    private SerialInterface[]   serialInterfaces;
    private List<SerialDevice>  serialDevices;
    private DmaController       dmaController;
    private AdConverter         adConverter;
    private RealtimeClock       realtimeClock;
    private KeyCircuit          keyCircuit;

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

    public List<SerialDevice> getSerialDevices() {
        return serialDevices;
    }

    public void setSerialDevices(List<SerialDevice> serialDevices) {
        this.serialDevices = serialDevices;
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
}
