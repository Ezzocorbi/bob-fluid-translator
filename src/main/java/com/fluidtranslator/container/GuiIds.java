package com.fluidtranslator.container;

public enum GuiIds {
    SIMPLE_FLUID_TANK(0),
    HBM_ADAPTER(1);

    public final int ordinal;

    GuiIds(int ordinal) {
        this.ordinal = ordinal;
    }

    public static GuiIds byOrdinal(int ord) {
        return GuiIds.values()[ord];
    }

}
