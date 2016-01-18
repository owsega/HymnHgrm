package com.owsega.odevotional.views;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.owsega.odevotional.R;
import com.owsega.odevotional.data.HymnsHelper;
import com.owsega.odevotional.data.HymnsHelper.Hymn;

/**
 * A fragment representing a single Hymn detail screen.
 * This fragment is either contained in a {@link HymnListActivity}
 * in two-pane mode (on tablets) or a {@link HymnDetailActivity}
 * on handsets.
 */
public class HymnDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "hymn_id";

    private String content = "";
    private String title = "";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HymnDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            Hymn mItem = HymnsHelper.get(getActivity().getContentResolver(), getArguments().getInt(ARG_ITEM_ID));

            content = mItem.details;
            title = mItem.id + "    " + mItem.title;

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(title);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.hymn_detail, container, false);
        ((TextView) view.findViewById(R.id.content)).setText(content);
        ((TextView) view.findViewById(R.id.title)).setText(title);
        return view;
    }
}
