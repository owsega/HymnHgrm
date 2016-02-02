package com.owsega.hgrm_hymns.data;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.owsega.hgrm_hymns.R;
import com.owsega.hgrm_hymns.views.HymnListActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Helper for hymns
 * This is essentially a view-model where model/data is in db {@link HymnContract} and {@link HymnProvider}
 * and the views are in the package {@link com.owsega.hgrm_hymns.views}
 */
public class HymnsHelper {
    private static final String ENGLISH_FILE_NAME = "English.txt";
    private static final String YORUBA_FILE_NAME = "Yoruba.txt";

    private static final Uri ENGLISH_URI = HymnContract.EnglishEntry.CONTENT_URI;
    private static final Uri YORUBA_URI = HymnContract.YorubaEntry.CONTENT_URI;

    static StringBuilder nextHymn = new StringBuilder();

    //todo "seyi" add yoruba support
    public static Hymn get(ContentResolver cr, int id) {
        Uri uri = HymnContract.EnglishEntry.CONTENT_URI;
        String selection = HymnContract.EnglishEntry.COL_HYMN_ID + " = ? ";
        Hymn retVal = null;

        Cursor c = cr.query(uri, null, selection, new String[]{String.valueOf(id)}, null);
        if (c != null && c.moveToFirst()) {
            retVal = new Hymn(
                    c.getInt(c.getColumnIndex(HymnContract.EnglishEntry.COL_HYMN_ID)),
                    c.getString(c.getColumnIndex(HymnContract.EnglishEntry.COL_HYMN_TITLE)),
                    c.getString(c.getColumnIndex(HymnContract.EnglishEntry.COL_HYMN_CONTENT)),
                    c.getInt(c.getColumnIndex(HymnContract.EnglishEntry.COL_STANZA_COUNT)),
                    c.getInt(c.getColumnIndex(HymnContract.EnglishEntry.COL_HAS_CHORUS)) == 1
            );
            c.close();
        }
        return retVal;
    }

    /**
     * sets the default settings in SharedPreferences and loads the db
     */
    public static class LoadDataTask extends AsyncTask<Void, Void, Void> {
        private static final String LOG_TAG = "HymnsHelper";
        ProgressDialog pDialog;
        Context mContext;
        boolean force;

        public LoadDataTask(Context context, boolean _forceUpdate) {
            mContext = context;
            force = _forceUpdate;
        }

        @Override
        protected void onPreExecute() {
            pDialog = ProgressDialog.show(mContext,
                    mContext.getString(R.string.please_wait),
                    mContext.getString(R.string.loading_hymns),
                    true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pDialog.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // exit, if we are not to force-update the db when it has stuffs in it already.
            if (!force && dbHasStuffsAlready())
                return null;

            PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).edit()
                    .putBoolean(HymnListActivity.PREF_SEARCH_LYRICS, true)
                    .putBoolean(HymnListActivity.PREF_SORT_BY_ID, true)
                    .apply();

            processHymnAsset(ENGLISH_FILE_NAME, ENGLISH_URI);
            processHymnAsset(YORUBA_FILE_NAME, YORUBA_URI);
            return null;
        }

        private void processHymnAsset(String name, Uri uri) {
            String file;
            String endOfLinePattern = "####";
            String newSongFormat = "%03d";

            String[] songs = new String[307];
            String[] titles = new String[307];
            try {
                file = getStringFromStream(mContext.getAssets().open(name));

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

                Log.d(LOG_TAG, "all songs: " + songs.length);
                putHymnsInDb(songs, titles, uri);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /**
         * This snippet reads all of the InputStream into a String.
         *
         * @throws IOException
         */
        private String getStringFromStream(InputStream is) throws IOException {
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

        private boolean dbHasStuffsAlready() {
            Cursor cursor = mContext.getContentResolver().query(ENGLISH_URI, null, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0)
                    return true;
                cursor.close();
            }
            cursor = mContext.getContentResolver().query(YORUBA_URI, null, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0)
                    return true;
                cursor.close();
            }
            return false;
        }

        private void putHymnsInDb(String[] songs, String[] titles, Uri uri) {
            ContentValues values;

            mContext.getContentResolver().delete(uri, null, null);

            for (int i = 1; i < songs.length; i++) {
                values = new ContentValues();
                values.put(HymnContract.EnglishEntry.COL_HYMN_ID, i);
                values.put(HymnContract.EnglishEntry.COL_HYMN_TITLE, titles[i]);
                values.put(HymnContract.EnglishEntry.COL_HAS_CHORUS, true);
                values.put(HymnContract.EnglishEntry.COL_HYMN_CONTENT, songs[i]);
                values.put(HymnContract.EnglishEntry.COL_STANZA_COUNT, 3);
                if (mContext.getContentResolver().insert(uri, values) != null)
                    Log.d(LOG_TAG, "inserted hymn " + i);
            }
        }
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
