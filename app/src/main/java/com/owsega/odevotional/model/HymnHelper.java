package com.owsega.odevotional.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing hymn content for ui
 */
public class HymnHelper {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Hymn> ITEMS = new ArrayList<>();
    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Hymn> ITEM_MAP = new HashMap<>();
    private static final int COUNT = 25;
    static String defaultHymn = "";

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Hymn item) {
        ITEMS.add(item);
        ITEM_MAP.put(String.valueOf(item.id), item);
    }

    private static Hymn createDummyItem(int position) {
        return new Hymn(position, "Hymn " + position, makeDetails());
    }

    private static String makeDetails() {
        return defaultHymn;
    }

    public static void initDefaultHymn(String hymn) {
        defaultHymn = hymn;
    }

    /**
     * A piece of hymn.
     */
    public static class Hymn {
        public final int id;
        public final String title;
        public final String details;

        public Hymn(int id, String content, String details) {
            this.id = id;
            this.title = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
