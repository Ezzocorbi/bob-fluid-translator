package com.fluidtranslator;

public enum TankModes {

    RECEIVER (0),
    BUFFER (1),
    SENDER (2),
    DISABLED (3);

    public final int ordinal;

    TankModes(int ordinal) {
        this.ordinal = ordinal;
    }

    public static TankModes byOrdinal(int ord) {
        return TankModes.values()[ord];
    }
}
