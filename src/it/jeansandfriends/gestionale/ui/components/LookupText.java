package it.jeansandfriends.gestionale.ui.components;

public final class LookupText {
    private LookupText() {}

    public static String safe(String s) {
        return s == null ? "" : s;
    }
}