package com.crazicrafter1.lootcrates;

public class Result {

    public Code code;

    public Result(Code code) {
        this.code = code;
    }

    public enum Code {
        INVALID_COUNT,
        INVALID_RANGE,
        INVALID_ITEM,
        INVALID_NAME,
        INVALID_LORE,
        INVALID_COLOR,
        INVALID_GLOW,
        INVALID_EFFECT_FORMAT,
        INVALID_EFFECT,
        INVALID_DURATION,
        INVALID_COMMAND,
        INVALID_ENCHANT_FORMAT,
        INVALID_ENCHANT,
        INVALID_LEVEL,
        INVALID_QA,
        INVALID_CRATE,
        INVALID_LOOT
    }

}
