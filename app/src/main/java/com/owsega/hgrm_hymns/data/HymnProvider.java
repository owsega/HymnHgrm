package com.owsega.hgrm_hymns.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * ContentProvider for managing hymns.
 */
public class HymnProvider extends ContentProvider {
    private static final String LOG_TAG = "Hgrm.Hymns";
    private static final int HYMN_ITEM = 100;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private HymnDbHelper hymnDbHelper;

    public HymnProvider() {
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(HymnContract.CONTENT_AUTHORITY, HymnContract.PATH, HYMN_ITEM);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        hymnDbHelper = new HymnDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;

        try {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            switch (sUriMatcher.match(uri)) {
                case HYMN_ITEM:
                    qb.setTables(HymnContract.Entry.TABLE_NAME);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid URI: " + uri);
            }
            cursor = qb.query(hymnDbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);

            Context context = getContext();
            if (context != null) cursor.setNotificationUri(context.getContentResolver(), uri);

        } catch (Exception e) {
            Log.e(LOG_TAG, "HymnProvider query error" + e.getMessage(), e);
        }
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case HYMN_ITEM:
                return HymnContract.Entry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) throws SQLException {
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case HYMN_ITEM:
                final SQLiteDatabase db = hymnDbHelper.getWritableDatabase();
                long _id = db.insert(HymnContract.Entry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = HymnContract.Entry.buildUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        Context context = getContext();
        if (context != null) context.getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = hymnDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case HYMN_ITEM:
                rowsDeleted = db.delete(
                        HymnContract.Entry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = hymnDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case HYMN_ITEM:
                rowsUpdated = db.update(HymnContract.Entry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }
        if (rowsUpdated != 0) {
            if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = hymnDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case HYMN_ITEM:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(HymnContract.Entry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    /**
     * Database helper for the provider
     */
    private class HymnDbHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "hymns.db";
        private static final int DATABASE_VERSION = 1;

        public HymnDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            final String SQL_CREATE_NOTIFICATION_TABLE = "CREATE TABLE " +
                    HymnContract.Entry.TABLE_NAME + " (" +
                    HymnContract.Entry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    HymnContract.Entry.COL_HYMN_ID + " INTEGER UNIQUE, " +
                    HymnContract.Entry.COL_HYMN_CONTENT + " TEXT NOT NULL COLLATE NOCASE, " +
                    HymnContract.Entry.COL_HYMN_TITLE + " TEXT NOT NULL COLLATE NOCASE, " +
                    HymnContract.Entry.COL_STANZA_COUNT + " INTEGER, " +
                    HymnContract.Entry.COL_HAS_CHORUS + " BOOLEAN"
                    + " );";
            db.execSQL(SQL_CREATE_NOTIFICATION_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + HymnContract.Entry.TABLE_NAME);
            onCreate(db);
        }
    }

}
