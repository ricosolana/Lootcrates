package com.crazicrafter1.lootcrates.util;

public class IntegerC {

    public int value;

    public IntegerC() {
    }

    public IntegerC(int value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
