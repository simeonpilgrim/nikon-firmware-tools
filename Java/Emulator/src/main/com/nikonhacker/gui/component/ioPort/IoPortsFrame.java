package com.nikonhacker.gui.component.ioPort;

import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

public class IoPortsFrame extends DocumentFrame {
    protected final IoPort[] ioPorts;

    public IoPortsFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, final IoPort[] ioPorts) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.ioPorts = ioPorts;
    }
}
