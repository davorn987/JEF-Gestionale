package it.jeansandfriends.gestionale.db;

import java.io.File;

public final class AppPaths {

    private AppPaths() {}

    /**
     * Cartella dati in home utente:
     * Windows: C:\Users\<user>\.gestionale-jeansandfriends
     * Linux/Mac: /home/<user>/.gestionale-jeansandfriends
     */
    public static File getAppDataDir() {
        String home = System.getProperty("user.home");
        File dir = new File(home, ".gestionale-jeansandfriends");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getDbFile() {
        return new File(getAppDataDir(), "gestionale.db");
    }
}
