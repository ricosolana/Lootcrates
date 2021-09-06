package com.crazicrafter1.lootcrates.util;

public class Int {

    public int value;

    public Int() {
    }

    public Int(int value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
