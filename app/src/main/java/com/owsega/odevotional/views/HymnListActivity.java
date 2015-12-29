package com.owsega.odevotional.views;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link HymnDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class HymnListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int HYMN_LOADER_ID = 1;
    /**
     * previously selected view
     * It's used for toggling the higlighted list item when in two-pane mode
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hymn_list);

        HymnHelper.initHymns(this, true);
        getSupportLoaderManager().initLoader(HYMN_LOADER_ID, null, this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        View recyclerView = findViewById(R.id.hymn_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.hymn_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = HymnContract.Entry.CONTENT_URI;

        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.mCursorAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.mCursorAdapter.changeCursor(null);
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

                                // do highlight and de-highlight  todo should test if we can use WeakREference
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
