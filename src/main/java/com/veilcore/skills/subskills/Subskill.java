package com.veilcore.skills.subskills;

import com.veilcore.skills.Skill;

/**
 * Represents a subskill within a main skill
 * Subskills grant bonus XP to their parent skill when performing specific actions
 */
public class Subskill {
    private final String id;
    private final String name;
    private final String description;
    private final Skill parentSkill;
    private final SubskillType type;
    
    public enum SubskillType {
        PASSIVE,  // Always active once unlocked
        ACTIVE    // Requires player action to trigger
    }
    
    public Subskill(String id, String name, String description, Skill parentSkill, SubskillType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentSkill = parentSkill;
        this.type = type;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Skill getParentSkill() {
        return parentSkill;
    }
    
    public SubskillType getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return name + " (" + parentSkill.getDisplayName() + ")";
    }
}
