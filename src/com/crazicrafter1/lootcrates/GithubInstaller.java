package com.crazicrafter1.lootcrates;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Paths;

class GithubInstaller {

    public static boolean installDepend(final Plugin main, String author, String githubProject, String jarname, String dependName) {
        try {
            URL api = new URL("https://api.github.com/repos/" + author + "/" + githubProject + "/releases/latest");
            URLConnection con = api.openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);

            JsonObject json;
            try {
                json = JsonParser.parseReader(new InputStreamReader(con.getInputStream())).getAsJsonObject();
            } catch (Error | Exception e45) {
                return false;
            }
            String tagName = json.get("tag_name").getAsString();

            final URL download = new URL("https://github.com/" + author + "/" + githubProject + "/releases/download/"
                    + tagName + "/" + jarname);

            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.GREEN + "Installing " + ChatColor.GOLD
                            + dependName + ": " + ChatColor.WHITE + tagName
                            + ChatColor.GREEN + " as it was missing");

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        InputStream in = download.openStream();

                        //File pluginFile;
                        File pluginToInstall;

                        try {
                            File thisPluginFile = new File(URLDecoder.decode(
                                    this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
                                    "UTF-8"));

                            pluginToInstall = Paths.get(thisPluginFile.getParentFile().getPath(), jarname).toFile();

                            pluginToInstall.createNewFile();

                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException("You don't have a good text codec on your system", e);
                        }

                        // File temp = new File("plugins/update");
                        // if (!temp.exists()) {
                        // temp.mkdir();
                        // }

                        copy(in, new FileOutputStream(pluginToInstall));

                        if (pluginToInstall.length() < 1000) {
                            // Plugin is too small. Keep old version in case new one is
                            // incomplete/nonexistant
                            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to install " + dependName);
                        } else {
                            // Plugin is valid, and we can delete the temp

                            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Installed " + dependName);

                            //Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Restart server to use " + main.getName());
                            //Bukkit.getPluginManager().disablePlugin(main);

                            //new BukkitRunnable() {
                            //    @Override
                            //    public void run() {
                            //        try {
                            //            Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(pluginToInstall));
                            //        } catch (Exception e) {
                            //            e.printStackTrace();
                            //        }
                            //    }
                            //}.runTaskLater(main, 1);
                            // ENABLE
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.run(); //(main, 0);
            return true;

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
            // debug("Another 4K, current: " + r);
        }
        out.flush();
        out.close();
        in.close();
        return bytes;
    }
}
