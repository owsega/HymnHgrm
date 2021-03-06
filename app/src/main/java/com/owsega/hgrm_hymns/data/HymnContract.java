package com.owsega.hgrm_hymns.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.owsega.hgrm_hymns.BuildConfig;

public class HymnContract {

    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID + ".hymn";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ENGLISH = "english";
    public static final String PATH_YORUBA = "yoruba";

    public static class EnglishEntry implements BaseColumns {
        public static final String TABLE_NAME = "english";
        public static final String COL_HYMN_ID = "hymn_id";
        public static final String COL_HYMN_CONTENT = "lyrics";
        public static final String COL_HYMN_TITLE = "title";
        public static final String COL_STANZA_COUNT = "stanzas";
        public static final String COL_HAS_CHORUS = "hasChorus";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENGLISH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_ENGLISH;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class YorubaEntry extends EnglishEntry {
        public static final String TABLE_NAME = "yoruba";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_YORUBA).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_YORUBA;
    }
}
