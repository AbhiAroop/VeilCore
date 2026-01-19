package com.veilcore.skills.tokens;

/**
 * Represents skill tokens used to unlock and upgrade skill tree nodes
 * Tokens come in three tiers: Basic, Advanced, and Master
 * Higher tier tokens can be used for lower tier nodes
 */
public class SkillToken {

    /**
     * Token tiers determine which nodes can be unlocked
     * Higher tiers can unlock lower tier nodes as well
     */
    public enum TokenTier {
        BASIC(1, "Basic", "#AAAAAA", "◆"),
        ADVANCED(2, "Advanced", "#5555FF", "◈"),
        MASTER(3, "Master", "#FF55FF", "✦");

        private final int level;
        private final String displayName;
        private final String color;
        private final String symbol;

        TokenTier(int level, String displayName, String color, String symbol) {
            this.level = level;
            this.displayName = displayName;
            this.color = color;
            this.symbol = symbol;
        }

        public int getLevel() {
            return level;
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
         * Get a tier by its level
         * @param level The tier level (1-3)
         * @return The token tier, or BASIC if invalid
         */
        public static TokenTier fromLevel(int level) {
            for (TokenTier tier : values()) {
                if (tier.level == level) {
                    return tier;
                }
            }
            return BASIC;
        }

        /**
         * Check if this tier can be used for a required tier
         * @param required The required tier
         * @return True if this tier is equal or higher
         */
        public boolean canUseFor(TokenTier required) {
            return this.level >= required.level;
        }

        /**
         * Get formatted display with color and symbol
         * @return Formatted tier name
         */
        public String getFormatted() {
            return color + symbol + " " + displayName;
        }
    }

    private final com.veilcore.skills.Skill skill;
    private final TokenTier tier;
    private final int amount;

    public SkillToken(com.veilcore.skills.Skill skill, TokenTier tier, int amount) {
        this.skill = skill;
        this.tier = tier;
        this.amount = amount;
    }

    public com.veilcore.skills.Skill getSkill() {
        return skill;
    }

    public TokenTier getTier() {
        return tier;
    }

    public int getAmount() {
        return amount;
    }
}
