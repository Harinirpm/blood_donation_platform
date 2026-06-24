package com.bloodbridge.model;

public enum BloodGroup {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");

    private final String display;

    BloodGroup(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public static BloodGroup fromDisplay(String display) {
        for (BloodGroup bg : values()) {
            if (bg.display.equalsIgnoreCase(display)) return bg;
        }
        throw new IllegalArgumentException("Unknown blood group: " + display);
    }
}
