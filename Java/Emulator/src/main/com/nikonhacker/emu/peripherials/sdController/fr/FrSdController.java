package com.nikonhacker.emu.peripherials.sdController.fr;

import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.sdController.SdController;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;

/*
    Usecase: init
    
             read from 0x63000034 : 0x00000000  (@0x001B31A4)
 0x00000000 written to 0x63000034               (@0x001B31A4)
             read from 0x63000038 : 0x00000000  (@0x001B31AA)
 0x00000000 written to 0x63000038               (@0x001B31AA)
 0xF1FF01FF written to 0x63000030               (@0x001B08BC)
             read from 0x6300002C : 0x00000000  (@0x001B08C8)
 0x00000000 written to 0x6300002C               (@0x001B08C8)
             read from 0x6300002C : 0x00000000  (@0x001B08D0)
 0x00000001 written to 0x6300002C               (@0x001B08D0)

*/
public class FrSdController implements SdController {
    private int channelNumber;
    private Platform platform;
    
    private int clock;
    private int timeout;
    
    public FrSdController(int channelNumber, Platform platform) {
        this.channelNumber = channelNumber;
        this.platform = platform;
    }
 
    public void setClockTimeout(int value) {
        timeout = (value &0xFFFF);
        clock = (clock >> 16)&0xFFFF;
    }

    public int getClockTimeout() {
        return (clock<<16) | timeout;
    }
}
