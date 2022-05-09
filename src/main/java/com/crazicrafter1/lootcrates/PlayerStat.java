package com.crazicrafter1.lootcrates;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PlayerStat {

    // get crate open information and other stuff to analyze possible duping or whatever
    //
    public HashMap<String, List<String>> openedCrates = new HashMap<>();

    public void crateInc(String id) {
        List<String> i = openedCrates.computeIfAbsent(id, k -> new ArrayList<>());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        i.add(formatter.format(date));
    }

}
