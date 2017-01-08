package com.owsega.hgrm_hymns;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Locale;

import static com.owsega.hgrm_hymns.views.HymnDetailActivity.LANG_ENGLISH;

/**
 * @author Seyi Owoeye. Created on 1/8/17.
 */
public class Utils {
    private final static String YORUBA_LOCALE = "yo";
    private final static String ENGLISH_LOCALE = "en";

    /**
     * Works if the api level is greater or equal to 17.
     * This returns a proper Resources for the language setting regardless of device locale.
     * If the setting is on yoruba, this returns a resources for yoruba even if we are on a
     * device with an English Language locale.
     * todo cache Resources for each language for efficiency
     */
    @NonNull
    public static Resources getLanguageResources(Context context, int languageSetting) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration conf = context.getResources().getConfiguration();
            conf = new Configuration(conf);
            conf.setLocale(languageSetting == LANG_ENGLISH ?
                    new Locale(ENGLISH_LOCALE) :
                    new Locale(YORUBA_LOCALE));
            Context localizedContext = context.createConfigurationContext(conf);
            return localizedContext.getResources();
        }
        return context.getResources();
    }
}
