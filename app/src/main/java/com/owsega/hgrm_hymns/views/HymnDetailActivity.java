package com.owsega.hgrm_hymns.views;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.owsega.hgrm_hymns.R;
import com.owsega.hgrm_hymns.data.HymnContract;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

/**
 * An activity representing a single Hymn detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link HymnListActivity}.
 */
public class HymnDetailActivity extends AppCompatActivity {

    public static final String LANGUAGE_SETTING = "pref_language";
    public static final String FONT_SETTING = "pref_font";
    public static final int LANG_ENGLISH = 1;
    public static final int LANG_YORUBA = 2;
    public static final int action_change_language = 123;
    public static final int action_change_font_size = 125;
    public static final int MINIMUM_FONT = 12;
    public static final int DEFAULT_FONT_OFFSET = 4;  // to make default font size 16sp

    HymnDetailFragment fragment;
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindowAnimations();
        setContentView(R.layout.activity_hymn_detail);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // keep scree on while user reads hymn
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.no_audio, Snackbar.LENGTH_SHORT).show();
            }
        });
        fab.hide();

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            int lang = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getInt(LANGUAGE_SETTING, LANG_ENGLISH);
            Uri uri = lang == LANG_YORUBA ? HymnContract.YorubaEntry.CONTENT_URI
                    : HymnContract.EnglishEntry.CONTENT_URI;
            arguments.putParcelable(HymnDetailFragment.ARG_ITEM_URI, uri);
            arguments.putInt(HymnDetailFragment.ARG_ITEM_ID,
                    getIntent().getIntExtra(HymnDetailFragment.ARG_ITEM_ID, 1));
            fragment = new HymnDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.hymn_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int languageSetting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt(FONT_SETTING, LANG_ENGLISH);

        String menuTitle = (languageSetting == LANG_ENGLISH) ? getString(R.string.action_english)
                : getString(R.string.action_yoruba);

        menu.add(1, action_change_language, 20, menuTitle)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, HymnListActivity.class));
                return true;
            case action_change_language:
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                int oldSetting = pref.getInt(LANGUAGE_SETTING, LANG_ENGLISH);

                // toggle the setting
                if (oldSetting == LANG_ENGLISH) {
                    pref.edit().putInt(LANGUAGE_SETTING, LANG_YORUBA).apply();
                    item.setTitle(R.string.action_yoruba);
                    reloadFragment(LANG_YORUBA);
                } else if (oldSetting == LANG_YORUBA) {
                    pref.edit().putInt(LANGUAGE_SETTING, LANG_ENGLISH).apply();
                    item.setTitle(R.string.action_english);
                    reloadFragment(LANG_ENGLISH);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        //todo work on tihs later
        //  change for listActivity android:fitsSystemWindows="true"todo
//        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
//        getWindow().setExitTransition(new Fade().setDuration(3000));
    }

    /**
     * reload the hymn with the new language setting
     *     todo don't create a new fragment, transition it using animations ;)
     * @param lang new language setting
     */
    private void reloadFragment(int lang) {
        Uri uri = lang == LANG_YORUBA ? HymnContract.YorubaEntry.CONTENT_URI
                : HymnContract.EnglishEntry.CONTENT_URI;
        Bundle arguments = new Bundle();
        arguments.putParcelable(HymnDetailFragment.ARG_ITEM_URI, uri);
        arguments.putInt(HymnDetailFragment.ARG_ITEM_ID,
                getIntent().getIntExtra(HymnDetailFragment.ARG_ITEM_ID, 1));
        fragment = new HymnDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.hymn_detail_container, fragment)
                .commit();
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        collapsingToolbarLayout.setTitle(title);
    }

    /**
     * sets a given resource id as the background image for the activity's appBar
     */
    public void setCollapsingToolbarImage(@DrawableRes int background) {
//        collapsingToolbarLayout.findViewById(R.id.toolbar_img).setBackgroundResource(background);
        collapsingToolbarLayout.setBackgroundResource(background);
    }
}
