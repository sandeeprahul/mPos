package in.hng.mpos.helper;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;


public class ApiCall extends AppCompatActivity {
    private static ApiCall mInstance;
    private RequestQueue mRequestQueue;
    private Context mCtx;


    public ApiCall(Context ctx) {
        mCtx = ctx;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized ApiCall getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ApiCall(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = AppController.getInstance().getRequestQueue();
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static void make(Context ctx, String URLpath, Response.Listener<String>
            listener, Response.ErrorListener errorListener) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLpath,
                listener, errorListener);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        ApiCall.getInstance(ctx).addToRequestQueue(stringRequest);
    }

    public static void makePost(Context ctx, String URLpath, Response.Listener<String>
            listener, Response.ErrorListener errorListener) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLpath,
                listener, errorListener);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        ApiCall.getInstance(ctx).addToRequestQueue(stringRequest);
    }

}