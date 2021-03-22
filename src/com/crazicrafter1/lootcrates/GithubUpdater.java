package com.crazicrafter1.lootcrates;

import java.io.*;
import java.net.*;
import com.google.gson.*;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

public class GithubUpdater {

    public static boolean autoUpdate(final Main main, String author, String githubProject, String jarname) {
        try {
            String version = main.getDescription().getVersion();
            String parseVersion = version.replace(".", "");

            String tagname;
            String s = "https://api.github.com/repos/" + author + "/" + githubProject + "/releases/latest";
            main.debug("autoupdater url: " + s);
            URL api = new URL(s);
            URLConnection con = api.openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);

            JsonObject json;
            try {
                json = new JsonParser().parse(new InputStreamReader(con.getInputStream())).getAsJsonObject();
            } catch (Exception e) {
                return false;
            }
            tagname = json.get("tag_name").getAsString();

            int latestVersion = Integer.parseInt(tagname.replaceAll("\\.", ""));
            int myVersion = Integer.parseInt(parseVersion.replaceAll("\\.", ""));

            s = "https://github.com/" + author + "/" + githubProject + "/releases/download/"
                    + tagname + "/" + jarname;
            main.debug("download url: " + s);
            final URL download = new URL(s);

            main.debug("latestVersion: " + latestVersion);
            main.debug("myVersion: " + myVersion);

            if (latestVersion > myVersion) {
                main.important(ChatColor.GREEN + "Found a new version of " + ChatColor.GOLD
                                + main.getDescription().getName() + ": " + ChatColor.WHITE + tagname
                                + ChatColor.LIGHT_PURPLE + " downloading now!!");

                new BukkitRunnable() {

                    @Override
                    public void run() {
                        try {

                            InputStream in = download.openStream();

                            File pluginFile;

                            try {
                                pluginFile = new File(URLDecoder.decode(
                                        this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
                                        "UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException("You don't have a good text codec on your system", e);
                            }

                            // File temp = new File("plugins/update");
                            // if (!temp.exists()) {
                            // temp.mkdir();
                            // }

                            // Copy the current plugin to 'plugin-backup.jar'
                            File tempInCaseSomethingGoesWrong = new File(main.getName() + "-backup.jar");
                            copy(new FileInputStream(pluginFile), new FileOutputStream(tempInCaseSomethingGoesWrong));


                            // Delete the old plugin,
                            pluginFile.setWritable(true, false);
                            pluginFile.delete();

                            // Write the new plugin to the old plugin
                            copy(in, new FileOutputStream(pluginFile));

                            if (pluginFile.length() < 1000) {
                                // Plugin is too small. Keep old version in case new one is
                                // incomplete/nonexistant
                                copy(new FileInputStream(tempInCaseSomethingGoesWrong),
                                        new FileOutputStream(pluginFile));
                            } else {
                                // Plugin is valid, and we can delete the temp
                                tempInCaseSomethingGoesWrong.delete();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.runTaskAsynchronously(main);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static long copy(InputStream in, OutputStream out) throws IOException {
        long bytes = 0;
        byte[] buf = new byte[0x1000];
        while (true) {
            int r = in.read(buf);
            if (r == -1)
                break;
            out.write(buf, 0, r);
            bytes += r;
        }
        out.flush();
        out.close();
        in.close();
        return bytes;
    }

}

