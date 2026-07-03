package com.kushsmp.essentials.ranks;

import java.util.List;

/**
 * Immutable definition of a single rank, loaded from ranks.yml.
 * weight: lower numbers sort higher in the tab list and take priority.
 */
public class Rank {

    private final String name;
    private final String display;
    private final String prefix;
    private final String suffix;
    private final String color;
    private final int weight;
    private final boolean isDefault;
    private final List<String> inherits;
    private final List<String> permissions;

    public Rank(String name, String display, String prefix, String suffix, String color,
                int weight, boolean isDefault, List<String> inherits, List<String> permissions) {
        this.name = name;
        this.display = display;
        this.prefix = prefix;
        this.suffix = suffix;
        this.color = color;
        this.weight = weight;
        this.isDefault = isDefault;
        this.inherits = inherits;
        this.permissions = permissions;
    }

    public String getName() { return name; }
    public String getDisplay() { return display; }
    public String getPrefix() { return prefix; }
    public String getSuffix() { return suffix; }
    public String getColor() { return color; }
    public int getWeight() { return weight; }
    public boolean isDefault() { return isDefault; }
    public List<String> getInherits() { return inherits; }
    public List<String> getPermissions() { return permissions; }
}
