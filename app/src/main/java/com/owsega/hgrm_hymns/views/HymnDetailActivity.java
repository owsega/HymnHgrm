package com.owsega.hgrm_hymns.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.owsega.hgrm_hymns.R;
import com.owsega.hgrm_hymns.data.HymnContract;

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

    HymnDetailFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hymn_detail);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // keep scree on while user reads hymn
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

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

        menu.add(1, action_change_font_size, 20, R.string.action_font_size)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

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
            case action_change_font_size:
                int fontSetting = PreferenceManager.getDefaultSharedPreferences(this)
                        .getInt(FONT_SETTING, 0);
                showChangeFontDialog(fontSetting);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * show an alert dialog for changing the
     */
    private void showChangeFontDialog(int fontSetting) {
        View v = LayoutInflater.from(this).inflate(R.layout.hymn_detail_settings, null);
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
        AlertDialog dialog = new AlertDialog.Builder(this).setView(v).create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                int newFont = fontSeekBar.getProgress();
                if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putInt(FONT_SETTING, newFont).commit())
                    fragment.setHymnFontSize(newFont + MINIMUM_FONT);
                else
                    Toast.makeText(HymnDetailActivity.this,
                            R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    /**
     * reload the hymn with the new language setting
     *
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
}
