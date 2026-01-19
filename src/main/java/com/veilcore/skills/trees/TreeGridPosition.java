package com.veilcore.skills.trees;

/**
 * Represents a position in the skill tree grid
 * Used for positioning nodes in the GUI
 */
public class TreeGridPosition {
    private final int x;
    private final int y;

    public TreeGridPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TreeGridPosition)) return false;
        TreeGridPosition other = (TreeGridPosition) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
