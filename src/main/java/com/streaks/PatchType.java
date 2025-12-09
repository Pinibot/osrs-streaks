package com.streaks;

import java.util.Arrays;
import java.util.List;

import net.runelite.api.gameval.ItemID;

public enum PatchType
{
    HERB(
        "Herb patch",
        Arrays.asList(
            ItemID.UNIDENTIFIED_GUAM, 
            ItemID.UNIDENTIFIED_MARENTILL, 
            ItemID.UNIDENTIFIED_TARROMIN,
            ItemID.UNIDENTIFIED_HARRALANDER,
            ItemID.EADGAR_GOUTWEED_HERB,
            ItemID.UNIDENTIFIED_RANARR,
            ItemID.UNIDENTIFIED_TOADFLAX,
            ItemID.UNIDENTIFIED_IRIT,
            ItemID.UNIDENTIFIED_AVANTOE,
            ItemID.UNIDENTIFIED_KWUARM,
            ItemID.UNIDENTIFIED_SNAPDRAGON,
            ItemID.UNIDENTIFIED_HUASCA,
            ItemID.UNIDENTIFIED_CADANTINE,
            ItemID.UNIDENTIFIED_LANTADYME,
            ItemID.UNIDENTIFIED_DWARF_WEED,
            ItemID.UNIDENTIFIED_TORSTOL
        )
    ),
    HOPS(
        "Hops patch",
       Arrays.asList(
            ItemID.BARLEY,
            ItemID.HAMMERSTONE_HOPS,
            ItemID.ASGARNIAN_HOPS,
            ItemID.JUTE_FIBRE,
            ItemID.YANILLIAN_HOPS,
            ItemID.FLAX,
            ItemID.KRANDORIAN_HOPS,
            ItemID.WILDBLOOD_HOPS,
            ItemID.HEMP,
            ItemID.COTTON_BOLL
        )
    ),
    ALLOTMENT(
        "Allotment",
        Arrays.asList(
            ItemID.POTATO,
            ItemID.ONION,
            ItemID.CABBAGE,
            ItemID.TOMATO,
            ItemID.SWEETCORN,
            ItemID.STRAWBERRY,
            ItemID.WATERMELON,
            ItemID.SNAPE_GRASS
    ));

    public final String label;
    public final List<Integer> possibleItemIds;

    private PatchType(String label, List<Integer> possibleItemIds)
    {
        this.label = label;
        this.possibleItemIds = possibleItemIds;
    }
}
