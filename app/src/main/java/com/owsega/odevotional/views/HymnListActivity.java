package com.owsega.odevotional.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.owsega.odevotional.R;
import com.owsega.odevotional.data.HymnContract;
import com.owsega.odevotional.model.HymnHelper;

import java.lang.ref.WeakReference;

/**
 * An activity representing a list of Hymns. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of hymns, which when touched,
 * lead to a {@link HymnDetailActivity} representing
 * hymn lyrics. On tablets, the activity presents the list of hymns and
 * hymn details side-by-side using two vertical panes.
 */
public class HymnListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int HYMN_LOADER_ID = 1;
    private static final String SEARCH_QUERY = "search_query";
    private static final String PREF_SEARCH_LYRICS = "pref_search_lyrics";
    private static final String PREF_SORT_BY_ID = "sort_by_hymn_id";
    private static final String SORT_ALPHABETIC = HymnContract.Entry.COL_HYMN_TITLE + " ASC";
    private static final String SORT_NUMERIC = HymnContract.Entry.COL_HYMN_ID + " ASC";
    //todo ::: use FTS (full text search queries) instead
    private static final String SEARCH_TITLES = HymnContract.Entry.COL_HYMN_TITLE + " LIKE ?";
    private static final String SEARCH_LYRICS = HymnContract.Entry.COL_HYMN_CONTENT + " LIKE ?";

    /**
     * previously selected view
     * It's used for toggling the highlighted list item when in two-pane mode
     */
    private static WeakReference<View> previousSelected = null;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    /**
     * adapter holding the hymns in the list (recyclerView)
     */
    private HymnCursorAdapter mAdapter;
    /**
     * array holding the 2 settings stored in SharedPref and shown in Settings Dialog
     */
    private boolean[] prefs;
    /**
     * holds the search query
     */
    private Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hymn_list);

        // initialize settings
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        prefs = new boolean[]{
                preferences.getBoolean(PREF_SEARCH_LYRICS, false),
                preferences.getBoolean(PREF_SORT_BY_ID, false)
        };

        // initialize data
        HymnHelper.initHymns(this, true);
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

        if (findViewById(R.id.hymn_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the activity should be in two-pane mode.
            mTwoPane = true;
        }

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
        if (!mTwoPane)
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
        Uri uri = HymnContract.Entry.CONTENT_URI;

        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = (prefs[1]) ? SORT_NUMERIC : SORT_ALPHABETIC;

        if (args != null && args.containsKey(SEARCH_QUERY)) {
            selection = args.getString(SEARCH_QUERY, null);
            if (selection != null) {
                selectionArgs = new String[]{"%" + selection + "%"};
                selection = (prefs[0] ? SEARCH_LYRICS : SEARCH_TITLES);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                showSettingsDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        final CharSequence[] options = new CharSequence[]{
                getString(R.string.pref_search_content),
                getString(R.string.pref_sort_by_id)};

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

    /**
     * Adapter for items in the fragment's list
     */
    private class HymnCursorAdapter extends RecyclerView.Adapter<HymnCursorAdapter.ViewHolder> {

        // PATCH: Because RecyclerView.Adapter in its current form doesn't natively support
        // cursors, we "wrap" a CursorAdapter that will do all the job for us
        CursorAdapter mCursorAdapter;

        Context mContext;

        public HymnCursorAdapter(Context context, Cursor c) {

            mContext = context;

            mCursorAdapter = new CursorAdapter(mContext, c, 0) {

                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    return LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.hymn_list_content, parent, false);
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    final int _id = cursor.getInt(cursor.getColumnIndex(HymnContract.Entry.COL_HYMN_ID));
                    String _title = cursor.getString(cursor.getColumnIndex(HymnContract.Entry.COL_HYMN_TITLE));

                    TextView id = (TextView) view.findViewById(R.id.id);
                    TextView title = (TextView) view.findViewById(R.id.hymn_title);

                    id.setText(String.valueOf(_id));
                    title.setText(_title);
                    view.setTag(_id);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mTwoPane) {
                                Bundle arguments = new Bundle();
                                arguments.putInt(HymnDetailFragment.ARG_ITEM_ID, _id);
                                HymnDetailFragment fragment = new HymnDetailFragment();
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.hymn_detail_container, fragment)
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                        .commit();

                                // do highlight and de-highlight
                                v.animate().alpha(0.5f).setDuration(500).start();
                                if (previousSelected != null)
                                    previousSelected.get().animate().alpha(1).setDuration(500).start();
                                previousSelected = new WeakReference<>(v);
                            } else {
                                Context context = v.getContext();
                                Intent intent = new Intent(context, HymnDetailActivity.class);
                                intent.putExtra(HymnDetailFragment.ARG_ITEM_ID, _id);

                                context.startActivity(intent);
                            }
                        }
                    });
                }
            };
        }

        @Override
        public int getItemCount() {
            return mCursorAdapter.getCount();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // Passing the binding operation to cursor loader
            mCursorAdapter.getCursor().moveToPosition(position);
            mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Passing the inflater job to the cursor-adapter
            View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
            return new ViewHolder(v);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView idView;
            public final TextView titleView;

            public ViewHolder(View view) {
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
