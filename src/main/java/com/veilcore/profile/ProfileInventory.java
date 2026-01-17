package com.veilcore.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a profile's inventory and equipment.
 * Stores serialized item data that can be restored when switching profiles.
 */
public class ProfileInventory {
    
    private List<String> items;          // Serialized inventory items
    private List<String> armorSlots;     // Serialized armor pieces
    private List<String> hotbar;         // Serialized hotbar items
    
    public ProfileInventory() {
        this.items = new ArrayList<>();
        this.armorSlots = new ArrayList<>();
        this.hotbar = new ArrayList<>();
    }
    
    public ProfileInventory(List<String> items, List<String> armorSlots, List<String> hotbar) {
        this.items = items != null ? items : new ArrayList<>();
        this.armorSlots = armorSlots != null ? armorSlots : new ArrayList<>();
        this.hotbar = hotbar != null ? hotbar : new ArrayList<>();
    }
    
    public List<String> getItems() {
        return items;
    }
    
    public void setItems(List<String> items) {
        this.items = items;
    }
    
    public List<String> getArmorSlots() {
        return armorSlots;
    }
    
    public void setArmorSlots(List<String> armorSlots) {
        this.armorSlots = armorSlots;
    }
    
    public List<String> getHotbar() {
        return hotbar;
    }
    
    public void setHotbar(List<String> hotbar) {
        this.hotbar = hotbar;
    }
}
