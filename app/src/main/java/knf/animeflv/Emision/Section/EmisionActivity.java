package knf.animeflv.Emision.Section;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.Bundler;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import knf.animeflv.Application;
import knf.animeflv.R;
import xdroid.toaster.Toaster;

/**
 * Created by Jordy on 05/03/2016.
 */
public class EmisionActivity extends AppCompatActivity {
    Toolbar toolbar;
    ViewPager viewPager;
    SmartTabLayout viewPagerTab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("is_amoled", false)) {
            setTheme(R.style.AppThemeDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emision);
        initActivity();
        Application application = (Application) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("Emision");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        toolbar = (Toolbar) findViewById(R.id.emision_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Emision");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.vp_emision);
        viewPagerTab = (SmartTabLayout) findViewById(R.id.st_Emision);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("is_amoled", false)) {
            toolbar.setBackgroundColor(getResources().getColor(android.R.color.black));
            viewPagerTab.setBackgroundColor(getResources().getColor(android.R.color.black));
            viewPagerTab.setSelectedIndicatorColors(getResources().getColor(R.color.prim));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(getResources().getColor(R.color.negro));
                getWindow().setNavigationBarColor(getResources().getColor(R.color.negro));
            }
        }
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("LUNES", DayFragment.class, new Bundler().putInt("code", 1).get())
                .add("MARTES", DayFragment.class, new Bundler().putInt("code", 2).get())
                .add("MIERCOLES", DayFragment.class, new Bundler().putInt("code", 3).get())
                .add("JUEVES", DayFragment.class, new Bundler().putInt("code", 4).get())
                .add("VIERNES", DayFragment.class, new Bundler().putInt("code", 5).get())
                .add("SABADO", DayFragment.class, new Bundler().putInt("code", 6).get())
                .add("DOMINGO", DayFragment.class, new Bundler().putInt("code", 7).get())
                .create());
        viewPager.setOffscreenPageLimit(7);
        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);
        viewPager.setCurrentItem(Math.abs(getActualDayCode() - 1), true);
    }

    private int getActualDayCode() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int code;
        switch (day) {
            case Calendar.MONDAY:
                code = 1;
                break;
            case Calendar.TUESDAY:
                code = 2;
                break;
            case Calendar.WEDNESDAY:
                code = 3;
                break;
            case Calendar.THURSDAY:
                code = 4;
                break;
            case Calendar.FRIDAY:
                code = 5;
                break;
            case Calendar.SATURDAY:
                code = 6;
                break;
            case Calendar.SUNDAY:
                code = 7;
                break;
            default:
                code = 0;
                break;
        }
        return code;
    }

    private void initActivity() {
        if (!isXLargeScreen(getApplicationContext())) { //Portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.dark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.prim));
        }
    }

    public static boolean isXLargeScreen(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (!isXLargeScreen(getApplicationContext())) {
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }
}
