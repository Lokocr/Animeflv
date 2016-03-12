package knf.animeflv;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.karumi.dexter.Dexter;
import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Jordy on 29/10/2015.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Dexter.initialize(getApplicationContext());
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(Message.class);
        Parse.initialize(this, "SSAa2CfCYAzpY3uqFf7ZMy19RU6jCgnVr2IbM6zC", "kownbaCYyGG07ZtlQDM5TEVZBezLzR32dzJIdzcF");
        android.webkit.CookieSyncManager.createInstance(this);
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
        WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(null, java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(coreCookieManager);
    }

    private Tracker mTracker;

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
