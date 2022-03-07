package com.crazicrafter1.lootcrates;

import java.util.HashMap;

public class PlayerStat {

    // get crate open information and other stuff to analyze possible duping or whatever
    public HashMap<String, Integer> openedCrates = new HashMap<>();

    public void crateInc(String id) {
        Integer i = openedCrates.get(id);
        if (i == null) {
            i = 0;
        }
        openedCrates.put(id, i+1);
    }

}
