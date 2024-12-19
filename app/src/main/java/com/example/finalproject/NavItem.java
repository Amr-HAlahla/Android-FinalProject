// NavItem.java
package com.example.finalproject;

public class NavItem {
    private final int id;    // Unique identifier for the navigation item
    private final int icon;
    private final String text;

    public NavItem(int id, int icon, String text) {
        this.id = id;
        this.icon = icon;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public int getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }
}
