package fr.keke142.tumblrautomatorserver;

import fr.keke142.tumblrautomatorserver.managers.AutopostManager;
import fr.keke142.tumblrautomatorserver.managers.ConfigManager;
import fr.keke142.tumblrautomatorserver.managers.DataManager;
import fr.keke142.tumblrautomatorserver.tasks.AutofollowTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class TumblrAutomatorServer {
    private static final Logger LOGGER = LogManager.getLogger(TumblrAutomatorServer.class);

    private ConfigManager configManager;
    private DataManager dataManager;

    public TumblrAutomatorServer() {
        try {
            configManager = new ConfigManager(this);
            dataManager = new DataManager(this);

            ScheduledExecutorService autofollowRunnableExeuctor = Executors.newScheduledThreadPool(1);
            autofollowRunnableExeuctor.scheduleAtFixedRate(new AutofollowTask(this), 0, 5, TimeUnit.SECONDS);

            new AutopostManager(this);

        } catch (IOException ex) {
            LOGGER.error("Failed to load configuration", ex);
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new TumblrAutomatorServer();
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public File getDataFolder() {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    /*
     * Create a default configuration file from the .jar.
     *
     * @param actual      The destination file
     * @param defaultName The name of the file inside the jar's defaults folder
     */
    public void createDefaultConfiguration(File actual, String defaultName) {
        // Make parent directories
        File parent = actual.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }


        if (actual.exists()) {
            return;
        }

        JarFile file = null;
        InputStream input = null;
        try {

            //   getClass().getProtectionDomain().getCodeSource().getLocation().getPath()
            file = new JarFile(new File(getClass().getProtectionDomain().getCodeSource().getLocation()
                    .getPath()));
            ZipEntry copy = file.getEntry(defaultName);
            if (copy == null) {
                file.close();
                throw new FileNotFoundException();
            }
            input = file.getInputStream(copy);
        } catch (IOException e) {
            LOGGER.error("Unable to read default configuration: " + defaultName);
        }

        if (input != null) {
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(actual);
                byte[] buf = new byte[8192];
                int length = 0;
                while ((length = input.read(buf)) > 0) {
                    output.write(buf, 0, length);
                }

                LOGGER.info("Default configuration file written: " + actual.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (IOException ignore) {
                }

                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ignore) {
                }
            }
        }
        if (file != null) {
            try {
                file.close();
            } catch (IOException ignore) {
            }
        }
    }
}
