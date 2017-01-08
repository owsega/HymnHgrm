package com.owsega.hgrm_hymns.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.owsega.hgrm_hymns.R;
import com.owsega.hgrm_hymns.Utils;
import com.owsega.hgrm_hymns.data.HymnContract;
import com.owsega.hgrm_hymns.data.HymnsHelper;

import java.lang.ref.WeakReference;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.owsega.hgrm_hymns.views.HymnDetailActivity.LANGUAGE_SETTING;
import static com.owsega.hgrm_hymns.views.HymnDetailActivity.LANG_ENGLISH;
import static com.owsega.hgrm_hymns.views.HymnDetailActivity.LANG_YORUBA;

/**
 * An activity representing a list of Hymns. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of hymns, which when touched,
 * lead to a {@link HymnDetailActivity} representing
 * hymn lyrics. On tablets, the activity presents the list of hymns and
 * hymn details side-by-side using two vertical panes.
 * <p>
 * todo fix bug whereby when rotation is done from 2-pane mode (where hymn lyrics is already showing on the right side) to single-pane, the app crashes
 */
public class HymnListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    public static final String PREF_SEARCH_LYRICS = "pref_search_lyrics";
    public static final String PREF_SORT_BY_ID = "sort_by_hymn_id";
    public static final int HYMN_LOADER_ID = 1;
    public static final String SEARCH_QUERY = "search_query";
    private static final String SORT_ALPHABETIC = HymnContract.EnglishEntry.COL_HYMN_TITLE + " ASC";
    private static final String SORT_NUMERIC = HymnContract.EnglishEntry.COL_HYMN_ID + " ASC";
    private static final String SEARCH_TITLES = HymnContract.EnglishEntry.COL_HYMN_TITLE + " LIKE ?";
    private static final String SEARCH_LYRICS = HymnContract.EnglishEntry.COL_HYMN_CONTENT + " LIKE ?";
    private static final String SEARCH_IDS = HymnContract.EnglishEntry.COL_HYMN_ID + " LIKE ?";

    /**
     * previously selected view
     * It's used for toggling the highlighted list item when in two-pane mode
     */
    private static WeakReference<View> previousSelected = null;
    /**
     * id of the hymn whose lyrics is currently being displayed.
     * We need for highlighting in the list, and toggling lyrics language
     */
    int currentHymnId = -1;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    boolean mTwoPane;
    /**
     * adapter holding the hymns in the list (recyclerView)
     */
    HymnCursorAdapter mAdapter;
    /**
     * array holding the 2 settings stored in SharedPref and shown in Settings Dialog
     */
    boolean[] prefs;
    /**
     * holds the search query
     */
    Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hymn_list);

        // initialize settings
        SharedPreferences preferences =
                getDefaultSharedPreferences(getApplicationContext());
        prefs = new boolean[]{
                preferences.getBoolean(PREF_SEARCH_LYRICS, true),
                preferences.getBoolean(PREF_SORT_BY_ID, true)
        };

        // initialize data
        new HymnsHelper.LoadDataTask(this, false).execute();
        getSupportLoaderManager().initLoader(HYMN_LOADER_ID, null, this);

        // initialize views
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        View searchView = findViewById(R.id.searchView);
        assert searchView != null;
        setupSearchView((SearchView) searchView);

        View recyclerView = findViewById(R.id.hymn_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        // The detail container view will be present only in the
        // large-screen layouts (res/values-w900dp).
        // If this view is present, then the activity should be in two-pane mode.
        mTwoPane = findViewById(R.id.hymn_detail_container) != null;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hymnId = R.string.no_audio;
                try { // if nothing is showing on the list yet
                    if (mTwoPane && ((TextView) findViewById(R.id.content)).getText().length() < 0)
                        hymnId = R.string.no_hymn_selected;
                } catch (NullPointerException ignored) {
                    hymnId = R.string.no_hymn_selected;
                }
                Snackbar.make(view, hymnId, Snackbar.LENGTH_SHORT).show();
            }
        });
//        if (!mTwoPane)
        fab.hide();


    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter = new HymnCursorAdapter(HymnListActivity.this, null);
        recyclerView.setAdapter(mAdapter);
    }

    private void setupSearchView(@NonNull final SearchView searchView) {
        args = new Bundle();
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                args.putString(SEARCH_QUERY, query);
                getSupportLoaderManager().restartLoader(HYMN_LOADER_ID, args, HymnListActivity.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                if (text.trim().length() > 0)
                    args.putString(SEARCH_QUERY, text);
                else
                    args.putString(SEARCH_QUERY, null);
                getSupportLoaderManager().restartLoader(HYMN_LOADER_ID, args, HymnListActivity.this);
                return false;
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int languageSetting = getDefaultSharedPreferences(getApplicationContext())
                .getInt(LANGUAGE_SETTING, LANG_ENGLISH);

        Uri uri = languageSetting == LANG_YORUBA ? HymnContract.YorubaEntry.CONTENT_URI
                : HymnContract.EnglishEntry.CONTENT_URI;

        // this variable will be re-used heavily in this function, hope you can keep up ;)
        // I did this so it can always fall back to null ... because when selection is null we
        // will at least get a query result (the whole db!)
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = (prefs[1]) ? SORT_NUMERIC : SORT_ALPHABETIC;

        if (args != null && args.containsKey(SEARCH_QUERY)) {
            selection = args.getString(SEARCH_QUERY, null);

            if (selection != null) {
                // if search query contains integer, search ids for that value
                // else search the titles/content for the full query text
                if (selection.matches(".*\\d.*")) {
                    selectionArgs = new String[]{"%" + selection.replaceAll("\\D", "") + "%"};
                    selection = SEARCH_IDS;
                } else {
                    selectionArgs = new String[]{"%" + selection + "%"};
                    selection = (prefs[0] ? SEARCH_LYRICS : SEARCH_TITLES);
                }
            }
        }
        return new CursorLoader(this, uri, null, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.mCursorAdapter.changeCursor(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.mCursorAdapter.changeCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hymn_list, menu);

        // add language swapper
        int languageSetting = getDefaultSharedPreferences(getApplicationContext())
                .getInt(LANGUAGE_SETTING, LANG_ENGLISH);
        String menuTitle = (languageSetting == LANG_ENGLISH) ? getString(R.string.action_english)
                : getString(R.string.action_yoruba);
        menu.add(1, HymnDetailActivity.action_change_language, 20, menuTitle)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                showSettingsDialog();
                return true;
            case HymnDetailActivity.action_change_language:
                SharedPreferences pref = getDefaultSharedPreferences(getApplicationContext());
                int oldSetting = pref.getInt(LANGUAGE_SETTING, LANG_ENGLISH);

                // toggle the setting
                if (oldSetting == LANG_ENGLISH) {
                    pref.edit().putInt(LANGUAGE_SETTING, LANG_YORUBA).apply();
                    item.setTitle(R.string.action_yoruba);
                    reloadListAndLyrics(LANG_YORUBA);
                } else if (oldSetting == LANG_YORUBA) {
                    pref.edit().putInt(LANGUAGE_SETTING, LANG_ENGLISH).apply();
                    item.setTitle(R.string.action_english);
                    reloadListAndLyrics(LANG_ENGLISH);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadListAndLyrics(int lang) {
        //reload list
        getSupportLoaderManager().restartLoader(HYMN_LOADER_ID, args, HymnListActivity.this);

        // reload lyrics
        if (mTwoPane && currentHymnId >= 0) {
            Uri uri = lang == LANG_YORUBA ? HymnContract.YorubaEntry.CONTENT_URI
                    : HymnContract.EnglishEntry.CONTENT_URI;
            Bundle arguments = new Bundle();
            arguments.putParcelable(HymnDetailFragment.ARG_ITEM_URI, uri);
            arguments.putInt(HymnDetailFragment.ARG_ITEM_ID, currentHymnId);
            HymnDetailFragment fragment = new HymnDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.hymn_detail_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    private void showSettingsDialog() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // use yoruba resources if we are on yoruba setting on english locale device
        //  or english resources if we are on yoruba locale device
        int languageSetting = sharedPref.getInt(LANGUAGE_SETTING, LANG_ENGLISH);
        Resources resources = Utils.getLanguageResources(this, languageSetting);
        final CharSequence[] options = new CharSequence[]{
                resources.getString(R.string.pref_search_content),
                resources.getString(R.string.pref_sort_by_id)};

        final SharedPreferences.Editor editor = sharedPref.edit();
        new AlertDialog.Builder(this)
                .setMultiChoiceItems(options, prefs, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        switch (which) {
                            case 0:
                                editor.putBoolean(PREF_SEARCH_LYRICS, isChecked).apply();
                                break;
                            case 1:
                                editor.putBoolean(PREF_SORT_BY_ID, isChecked).apply();
                                break;
                        }
                        // refresh the list
                        getSupportLoaderManager().restartLoader(HYMN_LOADER_ID, args, HymnListActivity.this);
                    }
                }).show();
    }

    @Override
    public void onClick(View v) {
        int _id = (int) v.getTag();
        currentHymnId = _id;
        View transitionView = v.findViewById(R.id.hymn_title);
        String transitionName = getString(R.string.trans_title);

        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putInt(HymnDetailFragment.ARG_ITEM_ID, _id);
            int lang = getDefaultSharedPreferences(getApplicationContext())
                    .getInt(LANGUAGE_SETTING, LANG_ENGLISH);
            Uri uri = lang == LANG_YORUBA ? HymnContract.YorubaEntry.CONTENT_URI
                    : HymnContract.EnglishEntry.CONTENT_URI;
            arguments.putParcelable(HymnDetailFragment.ARG_ITEM_URI, uri);
            HymnDetailFragment fragment = new HymnDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.hymn_detail_container, fragment)
                    .addSharedElement(transitionView, transitionName)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();

            // de-highlight and do highlight
            if (previousSelected != null && previousSelected.get() != null)
                previousSelected.get().animate().alpha(1).setDuration(500).start();
            previousSelected = new WeakReference<>(v);
            v.animate().alpha(0.5f).setDuration(500).start();
        } else {
            Activity context = (Activity) v.getContext();
            Intent intent = new Intent(context, HymnDetailActivity.class);
            intent.putExtra(HymnDetailFragment.ARG_ITEM_ID, _id);

            ActivityCompat.startActivity(context,
                    intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(context,
                            transitionView,
                            transitionName).toBundle());
        }
    }

    /**
     * Adapter for items in the fragment's list
     */
    class HymnCursorAdapter extends RecyclerView.Adapter<HymnCursorAdapter.ViewHolder> {

        // PATCH: Because RecyclerView.Adapter in its current form doesn't natively support
        // cursors, we "wrap" a CursorAdapter that will do all the job for us
        CursorAdapter mCursorAdapter;

        Context mContext;

        HymnCursorAdapter(Context context, Cursor c) {

            mContext = context;

            mCursorAdapter = new CursorAdapter(mContext, c, 0) {

                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    return LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.hymn_list_content, parent, false);
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    final int _id = cursor.getInt(cursor.getColumnIndex(HymnContract.EnglishEntry.COL_HYMN_ID));
                    String _title = cursor.getString(cursor.getColumnIndex(HymnContract.EnglishEntry.COL_HYMN_TITLE));

                    TextView id = (TextView) view.findViewById(R.id.id);
                    TextView title = (TextView) view.findViewById(R.id.hymn_title);

                    id.setText(String.valueOf(_id));
                    title.setText(_title);
                    view.setTag(_id);
                    view.setOnClickListener(HymnListActivity.this);
                    view.setAlpha(_id == currentHymnId ? 0.6f : 1);
//                    id.setTypeface(null, _id == currentHymnId ? Typeface.BOLD : Typeface.NORMAL);
//                    title.setTypeface(null, _id == currentHymnId ? Typeface.BOLD : Typeface.NORMAL);
                }
            };
        }

        @Override
        public int getItemCount() {
            return mCursorAdapter.getCount();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // Passing the binding operation to cursorAdapter
            mCursorAdapter.getCursor().moveToPosition(position);
            mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Passing the inflater job to the cursorAdapter
            View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
            return new ViewHolder(v);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView idView;
            final TextView titleView;

            ViewHolder(View view) {
                super(view);
                mView = view;
                idView = (TextView) view.findViewById(R.id.id);
                titleView = (TextView) view.findViewById(R.id.hymn_title);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + titleView.getText() + "'";
            }
        }
    }
}
