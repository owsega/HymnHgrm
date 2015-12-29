package com.owsega.odevotional.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.owsega.odevotional.R;
import com.owsega.odevotional.data.HymnContract.Entry;

/**
 * Helper class for providing hymn content for ui
 */
public class HymnHelper {

    public static void initHymns(Context context, boolean force) {
        Uri uri = Entry.CONTENT_URI;
        ContentResolver cr = context.getContentResolver();

        // exit this method if stuffs are in db already
        Cursor c = cr.query(uri, null, null, null, null);
        if (c != null && c.getCount() > 0) {
            c.close();
            if (!force)
                return;
            else // since "force" is set, we should delete all entries in db
                cr.delete(uri, null, null);
        }

        String hymnsData[] = context.getString(R.string.default_hymn).split("888888");

        for (int i = 0; i < hymnsData.length; i++) {
            // extract data for each hymn item
            String[] oneHymn = hymnsData[i].split("zzzxzz");
            String title = oneHymn[0];
            int stanzas = Integer.parseInt(oneHymn[1].trim());
            boolean hasChorus = Boolean.parseBoolean(oneHymn[2].trim());
            String lyrics = oneHymn[3];

            // insert hymn item inside HymnProvider
            ContentValues values;
            values = new ContentValues();
            values.put(Entry.COL_HYMN_ID, 1000 + i);
            values.put(Entry.COL_HYMN_TITLE, title);
            values.put(Entry.COL_HAS_CHORUS, hasChorus);
            values.put(Entry.COL_HYMN_CONTENT, lyrics);
            values.put(Entry.COL_STANZA_COUNT, stanzas);
            try {
                cr.insert(uri, values);
            } catch (SQLException ignored) { // hymn with same id already exists in db
            }
        }
    }

    public static Hymn get(ContentResolver cr, int id) {
        Uri uri = Entry.CONTENT_URI;
        String selection = Entry.COL_HYMN_ID + " = ? ";
        Hymn retVal = null;

        Cursor c = cr.query(uri, null, selection, new String[]{String.valueOf(id)}, null);
        if (c != null && c.moveToFirst()) {
            retVal = new Hymn(
                    c.getInt(c.getColumnIndex(Entry.COL_HYMN_ID)),
                    c.getString(c.getColumnIndex(Entry.COL_HYMN_TITLE)),
                    c.getString(c.getColumnIndex(Entry.COL_HYMN_CONTENT)),
                    c.getInt(c.getColumnIndex(Entry.COL_STANZA_COUNT)),
                    c.getInt(c.getColumnIndex(Entry.COL_HAS_CHORUS)) == 1
            );
            c.close();
        }
        return retVal;
    }

    /**
     * A piece of hymn.
     */
    public static class Hymn {
        public final int id;
        public final String title;
        public final String details;
        public final int stanzas;
        public final boolean hasChorus;

        public Hymn(int id, String _title, String details, int stanzas, boolean hasChorus) {
            this.id = id;
            this.title = _title;
            this.details = details;
            this.stanzas = stanzas;
            this.hasChorus = hasChorus;
        }

        @Override
        public String toString() {
            return id + " " + title;
        }
    }
}
