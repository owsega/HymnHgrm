package com.owsega.hgrm_hymns.views;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.owsega.hgrm_hymns.R;
import com.owsega.hgrm_hymns.data.HymnsHelper;
import com.owsega.hgrm_hymns.data.HymnsHelper.Hymn;

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
    private final String LOG_TAG = getClass().getSimpleName();

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

            getActivity().setTitle(title);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.hymn_detail, container, false);
        ((TextView) view.findViewById(R.id.content)).setText(content);
        ((TextView) view.findViewById(R.id.title)).setText(title);

        setBackgroundImages(view);
        hideNoHymnTextView();

        return view;
    }

    private void setBackgroundImages(View rootView) {
        int random = (int) (Math.random() * 3);
        int[] backgrounds = new int[]{
                R.drawable.bg_1,
                R.drawable.bg_2,
                R.drawable.bg_3
        };

        //set the top background for single-pane mode
        CollapsingToolbarLayout layout = (CollapsingToolbarLayout) getActivity()
                .findViewById(R.id.toolbar_layout);
        if (layout != null)
            layout.setBackgroundDrawable(ResourcesCompat.getDrawable(
                    getResources(), backgrounds[random], null));

        // set the background for lyrics pane
        ImageView background = (ImageView) rootView.findViewById(R.id.img_bg);
        background.setBackgroundDrawable(ResourcesCompat.getDrawable(
                getResources(), backgrounds[random], null));
    }

    private void hideNoHymnTextView() {
        try {
            TextView noHymn = (TextView) getActivity().findViewById(R.id.no_hymn_yet);
            if (noHymn != null)
                noHymn.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed to hide no_hymn_yet text view");
        }
    }
}
