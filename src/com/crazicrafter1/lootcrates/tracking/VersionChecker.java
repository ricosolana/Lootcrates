package com.crazicrafter1.lootcrates.tracking;

import com.crazicrafter1.lootcrates.Main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class VersionChecker {
    private int project = 0;
    private URL checkURL;
    private String latestVersion = "";
    private Main plugin;

    public VersionChecker(Main plugin, int projectID) {
        this.plugin = plugin;
        //this.latestVersion = plugin.getDescription().getVersion();
        this.project = projectID;

        try {
            this.checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectID + "/");
        } catch (Exception e) {
        }

    }

    public String getLatestVersion() {
        return this.latestVersion;
    }

    public String getResourceURL() {
        return "https://www.spigotmc.org/resources/" + this.project;
    }

    public boolean hasNewUpdate() throws Exception {
        URLConnection con = this.checkURL.openConnection();
        this.latestVersion = (new BufferedReader(new InputStreamReader(con.getInputStream()))).readLine();
        //plugin.important("LATEST VERSION: " + latestVersion + " " + checkURL.toString());
        return Integer.parseInt(latestVersion.replaceAll("\\.", "")) >
                Integer.parseInt(this.plugin.getDescription().getVersion().replaceAll("\\.", ""));
        //return !this.plugin.getDescription().getVersion().equals(this.latestVersion);
    }
}
