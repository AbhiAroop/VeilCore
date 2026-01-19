package com.veilcore.skills;

/**
 * Enum representing all available skills in VeilCore
 * Each skill has a unique ID, display name, color, and symbol
 */
public enum Skill {
    MINING("mining", "Mining", "#FFD700", "⛏"),
    COMBAT("combat", "Combat", "#FF5555", "⚔"),
    FARMING("farming", "Farming", "#55FF55", "🌾"),
    FISHING("fishing", "Fishing", "#55FFFF", "🎣");

    private final String id;
    private final String displayName;
    private final String color; // Hex color code
    private final String symbol;

    Skill(String id, String displayName, String color, String symbol) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.symbol = symbol;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Get a skill by its ID
     * @param id The skill ID
     * @return The skill, or null if not found
     */
    public static Skill fromId(String id) {
        for (Skill skill : values()) {
            if (skill.id.equalsIgnoreCase(id)) {
                return skill;
            }
        }
        return null;
    }

    /**
     * Get the colored display name with symbol
     * @return Formatted skill name
     */
    public String getFormattedName() {
        return color + symbol + " " + displayName;
    }
}
