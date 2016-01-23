package com.owsega.odevotional.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Helper for hymns
 * This is essentially a view-model where model/data is in db {@link HymnContract} and {@link HymnProvider}
 * and the views are in the package {@link com.owsega.odevotional.views}
 */
public class HymnsHelper {

    static Uri uri = HymnContract.Entry.CONTENT_URI;

    static StringBuilder nextHymn = new StringBuilder();

    public static void processText(Context context, boolean force) {
        // exit, if we are not to force-update the db when it has stuffs in it already.
        if (!force && dbHasStuffsAlready(context))
            return;

        String file;
        String endOfLinePattern = "####";
        String newSongFormat = "%03d";

        String[] songs = new String[307];
        String[] titles = new String[307];
        try {
            file = getStringFromStream(context.getAssets().open("Hymns.txt"));

            String[] lines = file.split(endOfLinePattern);

            int songsSoFar = 0;
            String nextSong;
            for (String line : lines) {
                nextSong = String.format(newSongFormat, songsSoFar + 1);
                if (line.contains(nextSong)) { // if we have a new song
                    songs[songsSoFar] = nextHymn.toString();

                    songsSoFar++;

                    // remove dots and numbers from title
                    line = line.replace(".", "");
                    Scanner sc = new Scanner(line);
                    sc.nextInt();
                    titles[songsSoFar] = sc.nextLine().trim().toUpperCase();
                    nextHymn = new StringBuilder();
                } else {
                    nextHymn.append(line).append("\n");
                }
            }
            // flush last song out
            songs[songsSoFar] = nextHymn.toString();

            Log.e("seyi", "all songs: " + songs.length);
            putHymnsInDb(context, songs, titles);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean dbHasStuffsAlready(Context context) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0)
                return true;
            cursor.close();
        }
        return false;
    }

    private static void putHymnsInDb(Context context, String[] songs, String[] titles) {
        ContentValues values;

        context.getContentResolver().delete(uri, null, null);

        for (int i = 1; i < songs.length; i++) {
            values = new ContentValues();
            values.put(HymnContract.Entry.COL_HYMN_ID, i);
            values.put(HymnContract.Entry.COL_HYMN_TITLE, titles[i]);
            values.put(HymnContract.Entry.COL_HAS_CHORUS, true);
            values.put(HymnContract.Entry.COL_HYMN_CONTENT, songs[i]);
            values.put(HymnContract.Entry.COL_STANZA_COUNT, 3);
            if (context.getContentResolver().insert(uri, values) != null)
                Log.e("seyi", "inserted hymn " + i);
        }
    }

    /**
     * This snippet reads all of the InputStream into a String.
     *
     * @throws IOException
     */
    private static String getStringFromStream(InputStream is) throws IOException {
        String line;
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        // Read response until the end
        while ((line = rd.readLine()) != null) {
            total.append(line);
        }

        // Return full string
        return total.toString();
    }

    public static Hymn get(ContentResolver cr, int id) {
        Uri uri = HymnContract.Entry.CONTENT_URI;
        String selection = HymnContract.Entry.COL_HYMN_ID + " = ? ";
        Hymn retVal = null;

        Cursor c = cr.query(uri, null, selection, new String[]{String.valueOf(id)}, null);
        if (c != null && c.moveToFirst()) {
            retVal = new Hymn(
                    c.getInt(c.getColumnIndex(HymnContract.Entry.COL_HYMN_ID)),
                    c.getString(c.getColumnIndex(HymnContract.Entry.COL_HYMN_TITLE)),
                    c.getString(c.getColumnIndex(HymnContract.Entry.COL_HYMN_CONTENT)),
                    c.getInt(c.getColumnIndex(HymnContract.Entry.COL_STANZA_COUNT)),
                    c.getInt(c.getColumnIndex(HymnContract.Entry.COL_HAS_CHORUS)) == 1
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
