package com.example.finalproject;

public class NavItem {
    private final int icon;
    private final String text;

    public NavItem(int icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    public int getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }
}
