package knf.animeflv;

import android.content.Context;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.karumi.dexter.Dexter;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import knf.animeflv.Emision.EmisionChecker;
import knf.animeflv.Utils.FileUtil;
import knf.animeflv.Utils.UtilsInit;


public class Application extends android.app.Application {
    Context context;
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        UtilsInit.init(this);
        EmisionChecker.Ginit(this);
        Dexter.initialize(getApplicationContext());
        android.webkit.CookieSyncManager.createInstance(this);
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
        WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(null, java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(coreCookieManager);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                Log.e("Uncaught", "Error", e);
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                File logdir = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache/logs");
                if (!logdir.exists()) {
                    logdir.mkdirs();
                }
                FileUtil.writeToFile(e.getMessage() + "\n" + exceptionAsString, new File(logdir, "Uncaught.log"));
                System.exit(0);
            }
        });
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
