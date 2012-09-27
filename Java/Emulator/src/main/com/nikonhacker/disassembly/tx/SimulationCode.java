package com.nikonhacker.disassembly.tx;

import com.nikonhacker.emu.EmulationContext;
import com.nikonhacker.emu.EmulationException;

/*
Copyright (c) 2003-2006,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
 * Interface to represent the method for simulating the execution of a specific MIPS basic
 * instruction.  It will be implemented by the anonymous class created in the last
 * argument to the BasicInstruction constructor.
 *
 * @author Pete Sanderson
 * @version August 2003
 *
 */

public interface SimulationCode {

    /**
     * Method to simulate the execution of a specific MIPS basic instruction.
     *
     * @param statement A ProgramStatement representing the MIPS instruction to simulate.
     * @param emulationContext
     * @throws EmulationException This is a run-time exception generated during simulation.
     * @return true if the simulation code updates the PC (no more PC increment is needed)
     **/
    public boolean simulate(TxStatement statement, EmulationContext emulationContext) throws EmulationException;
}
