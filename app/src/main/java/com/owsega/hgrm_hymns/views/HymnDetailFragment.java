package com.owsega.hgrm_hymns.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.owsega.hgrm_hymns.R;
import com.owsega.hgrm_hymns.Utils;
import com.owsega.hgrm_hymns.data.HymnsHelper;
import com.owsega.hgrm_hymns.data.HymnsHelper.Hymn;

import static com.owsega.hgrm_hymns.views.HymnDetailActivity.DEFAULT_FONT_OFFSET;
import static com.owsega.hgrm_hymns.views.HymnDetailActivity.FONT_SETTING;
import static com.owsega.hgrm_hymns.views.HymnDetailActivity.LANGUAGE_SETTING;
import static com.owsega.hgrm_hymns.views.HymnDetailActivity.LANG_ENGLISH;
import static com.owsega.hgrm_hymns.views.HymnDetailActivity.MINIMUM_FONT;
import static com.owsega.hgrm_hymns.views.HymnDetailActivity.action_change_font_size;

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
    public static final String ARG_ITEM_URI = "hymn_uri";
    private final String LOG_TAG = getClass().getSimpleName();

    private String content = "";
    private String title = "";
    private TextView hymnText;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HymnDetailFragment() {
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            Uri uri = null;
            if (getArguments().containsKey(ARG_ITEM_URI))
                uri = getArguments().getParcelable(ARG_ITEM_URI);

            Hymn mItem = HymnsHelper.get(getActivity().getContentResolver(),
                    getArguments().getInt(ARG_ITEM_ID), uri);

            content = mItem.details;
            title = mItem.id + "    " + mItem.title;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hymn_detail, container, false);
        hymnText = ((TextView) view.findViewById(R.id.content));
        hymnText.setText(content);
        hymnText.setTextSize(MINIMUM_FONT + PreferenceManager.getDefaultSharedPreferences(
                view.getContext()).getInt(FONT_SETTING, DEFAULT_FONT_OFFSET));

        getActivity().setTitle(title);

        setBackgroundImages(view);
        hideNoHymnTextView();

        setHasOptionsMenu(true);  // add menu items too!!

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.add(1, action_change_font_size, 20, R.string.action_font_size)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        Resources resources = Utils.getLanguageResources(getContext(),
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .getInt(LANGUAGE_SETTING, LANG_ENGLISH));

        MenuItem font = menu.findItem(action_change_font_size);
        font.setTitle(resources.getString(R.string.action_font_size));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case action_change_font_size:
                int fontSetting = PreferenceManager.getDefaultSharedPreferences(getContext())
                        .getInt(FONT_SETTING, 0);
                showChangeFontDialog(getContext(), fontSetting);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setBackgroundImages(View rootView) {
        int random = (int) (Math.random() * 4);
        int[] backgrounds = new int[]{
                R.drawable.bg_1,
                R.drawable.bg_2,
                R.drawable.bg_3,
                R.drawable.bg_4
        };

        //set the top background for single-pane mode
        if (getActivity() instanceof HymnDetailActivity)
            ((HymnDetailActivity) getActivity()).setCollapsingToolbarImage(backgrounds[random]);

        // set the background for lyrics pane
        ImageView background = (ImageView) rootView.findViewById(R.id.img_bg);
        background.setBackgroundResource(backgrounds[random]);
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

    /**
     * set the textSize for the hymn text
     *
     * @param fontSize in sp (scaled pixel)
     */
    public void setHymnFontSize(int fontSize) {
        hymnText.setTextSize(fontSize);
    }

    /**
     * show an alert dialog for changing the
     */
    private void showChangeFontDialog(final Context context, int fontSetting) {
        final Resources resources = Utils.getLanguageResources(context,
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt(LANGUAGE_SETTING, LANG_ENGLISH));

        View v = LayoutInflater.from(context).inflate(R.layout.hymn_detail_settings, null);
        TextView titleTextView = (TextView) v.findViewById(R.id.current_font_title);
        titleTextView.setText(resources.getString(R.string.action_font_size));
        final TextView currentFont = (TextView) v.findViewById(R.id.current_font_size);
        currentFont.setText(String.valueOf(fontSetting + MINIMUM_FONT));
        final SeekBar fontSeekBar = (SeekBar) v.findViewById(R.id.font_size_bar);
        fontSeekBar.setProgress(fontSetting);
        fontSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentFont.setText(String.valueOf(MINIMUM_FONT + progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(context).setView(v).create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                int newFont = fontSeekBar.getProgress();
                if (PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putInt(FONT_SETTING, newFont).commit())
                    setHymnFontSize(newFont + MINIMUM_FONT);
                else
                    Toast.makeText(context, resources.getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}
