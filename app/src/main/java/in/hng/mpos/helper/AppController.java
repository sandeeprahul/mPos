package in.hng.mpos.helper;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.okhttp.OkHttpClient;

import io.fabric.sdk.android.Fabric;


public class AppController extends MultiDexApplication {
    private static AppController sInstance;
    private RequestQueue mRequestQueue;
     FirebaseAnalytics mFirebaseAnalytics;
    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        init();
        initFabrics();
        stethoInit();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


    }

    /**
     * Connect print service through interface library
     */
    private void init(){
        SunmiPrintHelper.getInstance().initSunmiPrinterService(this);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized AppController getInstance() {
        return sInstance;
    }

    public RequestQueue getRequestQueue() {

        if (mRequestQueue == null) {
            OkHttpClient client = new OkHttpClient();
            client.networkInterceptors().add(new StethoInterceptor());
            mRequestQueue = Volley.newRequestQueue(AppController.getInstance().getApplicationContext(), new OkHttpStack(client));
        }
        return mRequestQueue;
    }

    private void initFabrics() {
      //  Fabric.with(this, new Crashlytics());
       Fabric mFabric = new Fabric.Builder(this)
               .kits(new Crashlytics())
               .debuggable(true)
               .build();
       Fabric.with(mFabric);
    }

    private void stethoInit() {

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }


}
