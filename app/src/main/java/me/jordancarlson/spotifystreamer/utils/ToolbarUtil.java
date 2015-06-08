package me.jordancarlson.spotifystreamer.utils;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import me.jordancarlson.spotifystreamer.R;

/**
 * Created by jcarlson on 5/21/15.
 */
public class ToolbarUtil {

    public static void setupToolbar(final AppCompatActivity activity, final String title, final String subtitle, final boolean isHome) {

        // Set a toolbar to replace the action bar
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

//         Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Call some material design APIs here
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(activity.getResources().getColor(R.color.green_700));
        }

        toolbar.setTitle(title);
        if (!TextUtils.isEmpty(subtitle)) {
            toolbar.setSubtitle(subtitle);
        }

        if (!isHome) {
            try {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
