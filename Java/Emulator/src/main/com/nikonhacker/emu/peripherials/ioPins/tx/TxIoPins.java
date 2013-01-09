package com.nikonhacker.emu.peripherials.ioPins.tx;

import com.nikonhacker.emu.peripherials.ioPins.IoPins;

@SuppressWarnings("UnusedDeclaration")
public class TxIoPins extends IoPins {



    /* page 6.32
    Preconfiguration 1 (Interrupt from external pin)
        Set the port of the corresponding pin. Setting PnFCx [m] of the corresponding port function register to “1”
        allows the pin to be used as the function pin. Clearing PnCR [m] to “0” allows the pin to be used as the input
        port.
    Port register
        PnFCx<PnmFx> ← “1”
        PnCR<PnmC> ← “0”
    (Note)
        n: port number
        m: corresponding bit
        x: function register number
    */

    //FF004140 Port 5 register

    boolean control[];
    boolean function1[], function2[], function3[];
    boolean openDrain[];
    boolean pullUp[];
    boolean inputEnable[];


    public TxIoPins() {
        values = new boolean[241];

        control = new boolean[241];
        function1 = new boolean[241]; function2 = new boolean[241]; function3 = new boolean[241];
        openDrain = new boolean[241];
        pullUp = new boolean[241];
        inputEnable = new boolean[241];
    }


}
