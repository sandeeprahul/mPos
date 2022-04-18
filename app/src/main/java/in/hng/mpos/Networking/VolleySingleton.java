package in.hng.mpos.Networking;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;

import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.OkHttpStack;

public class VolleySingleton {
    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;

    private VolleySingleton() {
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance() {
        if (mInstance == null) {
            mInstance = new VolleySingleton();
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            OkHttpClient client = new OkHttpClient();
            client.networkInterceptors().add(new StethoInterceptor());
            mRequestQueue = Volley.newRequestQueue(AppController.getInstance().getApplicationContext(), new OkHttpStack(client));
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


    public void addToArrayRequestQueue(JsonArrayRequest req) {
        getRequestQueue().add(req);
    }
}
