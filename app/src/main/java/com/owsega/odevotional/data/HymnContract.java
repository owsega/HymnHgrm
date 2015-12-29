package com.owsega.odevotional.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class HymnContract {

    public static final String CONTENT_AUTHORITY = "com.owsega.odevotional.hymn";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH = "hymns";

    public static final class Entry implements BaseColumns {
        public static final String TABLE_NAME = "hymns";
        public static final String COL_HYMN_ID = "hymn_id";
        public static final String COL_HYMN_CONTENT = "lyrics";
        public static final String COL_HYMN_TITLE = "title";
        public static final String COL_STANZA_COUNT = "stanzas";
        public static final String COL_HAS_CHORUS = "hasChorus";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
