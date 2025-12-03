package com.streaks;

public enum PatchType
{
    HERB("Herb patch"),
    HOPS("Hops patch"),
    ALLOTMENT("Allotment");

    public final String label;

    private PatchType(String label)
    {
        this.label = label;
    }
}
