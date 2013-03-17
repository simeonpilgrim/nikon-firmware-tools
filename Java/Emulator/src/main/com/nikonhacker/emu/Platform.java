package com.nikonhacker.emu;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.clock.ClockGenerator;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.peripherials.dmaController.DmaController;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;

import java.util.List;

/**
 * A platform represents a microcontroller hardware in a given state: CPU, memory, clock generator,
 * interrupt controller, timers, i/o ports, serial interfaces, etc.
 */
public class Platform {

    private CPUState cpuState;
    private Memory memory;
    private ClockGenerator clockGenerator;
    private InterruptController interruptController;
    private ProgrammableTimer[] programmableTimers;
    private IoPort[] ioPorts;
    private SerialInterface[] serialInterfaces;
    private List<SerialDevice> serialDevices;
    private DmaController dmaController;

    public CPUState getCpuState() {
        return cpuState;
    }

    public void setCpuState(CPUState cpuState) {
        this.cpuState = cpuState;
    }

    public Memory getMemory() {
        return memory;
    }

    public void setMemory(Memory memory) {
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
}
