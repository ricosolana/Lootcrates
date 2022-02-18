package com.crazicrafter1.lootcrates.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubUpdater {
    private static final Pattern SEMVER_PATTERN = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)");

    private static boolean isOutdated(Plugin plugin, String latestVersion) {
        final String pluginVersion = plugin.getDescription().getVersion();

        plugin.getLogger().info("Checking current " + plugin.getDescription().getVersion() + " against latest " + latestVersion);

        Matcher pluginMatcher = SEMVER_PATTERN.matcher(pluginVersion);
        if (pluginMatcher.find()) {
            Matcher latestMatcher = SEMVER_PATTERN.matcher(latestVersion);
            if (latestMatcher.find()) {
                String[] pluginSemver = pluginVersion.split("\\.");
                String[] otherSemver = latestVersion.substring(latestMatcher.start(), latestMatcher.end()).split("\\.");

                for (int i = 0; i < otherSemver.length; i++) {
                    //  pl                other
                    // 1.3.4      vs      2.4.3
                    // ^                  ^
                    //   ^                  ^
                    if (Integer.parseInt(pluginSemver[i]) < Integer.parseInt(otherSemver[i])) {
                        return true;
                    }
                }
                plugin.getLogger().info("Using latest version");

                return false;
            } else {
                plugin.getLogger().warning("Latest version has incomparable semver (" + latestVersion + ")");
            }
        } else {
            plugin.getLogger().warning("Current version has incomparable semver (" + pluginVersion + ")");
        }

        plugin.getLogger().severe("Update failed");

        return false;
    }

    public static void autoUpdate(final Plugin plugin, String author, String githubProject, String jarname) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    /// Check version
                    URL api = new URL("https://api.github.com/repos/" + author + "/" + githubProject + "/releases/latest");
                    URLConnection con = api.openConnection();
                    con.setConnectTimeout(15000);
                    con.setReadTimeout(15000);

                    JsonObject json = JsonParser.parseReader(new InputStreamReader(con.getInputStream())).getAsJsonObject();
                    final String latestVersion = json.get("tag_name").getAsString();

                    if (!isOutdated(plugin, latestVersion))
                        return;

                    plugin.getLogger().info("Updating from " + plugin.getDescription().getVersion()
                        + " to " + latestVersion);

                    final URL download = new URL("https://github.com/" + author + "/" + githubProject + "/releases/download/"
                            + latestVersion + "/" + jarname);

                    File pluginFile = new File(URLDecoder.decode(
                            this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
                            "UTF-8"));

                    File backupFile = new File(plugin.getName() + "-backup.jar");
                    copy(new FileInputStream(pluginFile), new FileOutputStream(backupFile));

                    pluginFile.setWritable(true, false);
                    pluginFile.delete();
                    copy(download.openStream(), new FileOutputStream(pluginFile));

                    if (pluginFile.length() < 1000) {
                        // Too small to have been successful
                        copy(new FileInputStream(backupFile),
                                new FileOutputStream(pluginFile));
                    } else {
                        // Plugin is valid, and we can delete the temp
                        backupFile.delete();
                    }

                } catch (Exception e) {
                    plugin.getLogger().severe("Unable to update: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
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