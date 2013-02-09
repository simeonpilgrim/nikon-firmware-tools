package com.nikonhacker.realos;

public interface ITron3 {
    int SYSCALL_NUMBER_REF_TSK = 0xEC;
    int SYSCALL_NUMBER_REF_SEM = 0xCC;
    int SYSCALL_NUMBER_REF_FLG = 0xD4;
    int SYSCALL_NUMBER_REF_MBX = 0xC4;
    int SYSCALL_NUMBER_SET_FLG = 0xD0;
    int SYSCALL_NUMBER_CLR_FLG = 0xD1;
}
