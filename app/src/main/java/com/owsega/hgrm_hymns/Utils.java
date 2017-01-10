package com.owsega.hgrm_hymns;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Locale;

import static com.owsega.hgrm_hymns.views.HymnDetailActivity.LANG_ENGLISH;

/**
 * @author Seyi Owoeye. Created on 1/8/17.
 */
public class Utils {

    private final static String YORUBA_LOCALE = "yo";
    private final static String ENGLISH_LOCALE = "en";
    private static int[] backgrounds = new int[]{
            R.drawable.bg_1,
            R.drawable.bg_2,
            R.drawable.bg_3,
            R.drawable.bg_4
    };
    private static Resources englishRes;
    private static Resources yorubaRes;

    /**
     * @return the resource id of a random background image
     */
    @DrawableRes
    public static int getRandomBackgroundImg() {
        return backgrounds[(int) (Math.random() * backgrounds.length)];
    }

    /**
     * Works if the api level is greater or equal to 17.
     * This returns a proper Resources for the language setting regardless of device locale.
     * If the setting is on yoruba, this returns a resources for yoruba even if we are on a
     * device with an English Language locale.
     */
    @NonNull
    public static Resources getLanguageResources(Context context, int languageSetting) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

            Log.e("seyi", "about to try to use a cached Resources");
            // if we have a cached Resources, return it
            if (languageSetting == LANG_ENGLISH && englishRes != null)
                return englishRes;
            if (yorubaRes != null)
                return yorubaRes;

            Log.e("seyi", "not using a cached Resources");
            // else create a new Resources object
            Configuration conf = context.getResources().getConfiguration();
            conf = new Configuration(conf);
            conf.setLocale(languageSetting == LANG_ENGLISH ?
                    new Locale(ENGLISH_LOCALE) :
                    new Locale(YORUBA_LOCALE));
            Context localizedContext = context.createConfigurationContext(conf);

            // cache the new Resources object for future use
            if (languageSetting == LANG_ENGLISH)
                englishRes = localizedContext.getResources();
            else yorubaRes = localizedContext.getResources();

            // return the new Resources object
            return localizedContext.getResources();
        }
        return context.getResources();
    }
}
