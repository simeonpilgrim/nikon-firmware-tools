package com.nikonhacker.emu.peripherials.programmableTimer.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;

/**
     Counter use case:
    Starting listener for FR80 range 0x00000100 - 0x0000013F
    0xFFFFFFFF written to 0x00000130               (@0x001017E8)
    0x0613     written to 0x0000013E               (@0x001017F0)
    
    0x00000271 written to 0x00000100               (@0x001017F6)
    0x0273     written to 0x0000010E               (@0x00101800)
    
                read from 0x00000138 : 0x00000000  (@0x00107660)
                ...
                read from 0x00000138 : 0x00000000  (@0x00107660)

 * This is an implementation of a FR80 custom ASIC 32-bit reload timer
 * Based on section 20 of the 91605 hardware spec http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-10147-2E.pdf
 * But register adresses do not match and extended 32-bit value is in use.
 */
public class FrReloadTimer32 extends FrReloadTimer {

    public FrReloadTimer32(int timerNumber, Platform platform) {
        super(timerNumber, platform);
    }

    @Override
    protected String getName() {
        return Constants.CHIP_LABEL[Constants.CHIP_FR] + " Reload 32-bit timer #" + timerNumber;
    }

    @Override
    protected boolean requestInterrupt() {
        return platform.getSharedInterruptCircuit().request(FrInterruptController.RELOAD_TIMER_32_INTERRUPT_REQUEST_NR, 20+timerNumber);
    }

    @Override
    protected void removeInterrupt() {
        platform.getSharedInterruptCircuit().removeRequest(FrInterruptController.RELOAD_TIMER_32_INTERRUPT_REQUEST_NR, 20+timerNumber);
    }

    /**
     * Overridden because it seems there is a slight difference in count:
     * - round numbers like 25000 are loaded to 0x48 timer or 0x58.
     * - timers 0x100-0x150 are loaded with numbers like 999.
     * it seems be the 32-bit reload timer always counts (tmrlra+1) pulses according to datasheet while the 16-bit reload timer counts exactly tmrlra...
     */
    @Override
    public Object onClockTick() throws Exception {
        if (active) {
            if (currentValue==0) {
                isInUnderflowCondition = true;
                if (isTmcsrInteSet()) {
                    requestInterrupt();
                }
                if (isTmcsrReldSet()) {
                    // reload timer counts always (tmrlra+1) pulses according to datasheet
                    currentValue = tmrlra;
                } else {
                    enabled = false;
                    unRegister();
                }
            } else {
                currentValue--;
            }
        }
        return null;
    }

}
