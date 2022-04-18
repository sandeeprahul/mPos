package in.hng.mpos.Networking;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

import in.hng.mpos.Utils.AppLog;

import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;

public class BaseStringRequest implements Response.Listener<String>, Response.ErrorListener {




    public static final int MY_SOCKET_TIMEOUT_MS = 30000;
    public static String TAG = BaseStringRequest.class.getSimpleName();



    public void makeGetCallToServer(String url) {
        AppLog.showDebugLog(TAG, TAG + " :: makeGetCallToServer " + "URL:  " + url);
        StringRequest stringRequest = new StringRequest(GET, url, this, this);

        addRequestToRequestQueue(stringRequest);
        VolleySingleton.getInstance().addToRequestQueue(stringRequest);

    }

    public void makePostCallToServer(String url, final Map<String, String> body) throws AuthFailureError {
        AppLog.showDebugLog(TAG, TAG + " :: makePostCallToServer " + "URL:  " + url +
                "payload:  " + body.toString());
        StringRequest stringRequest = new StringRequest(POST, url, this, this)
        {
            protected Map<String, String> getParams() {
                return body;
            }
        };
        addRequestToRequestQueue(stringRequest);
        VolleySingleton.getInstance().addToRequestQueue(stringRequest);

    }

    private void addRequestToRequestQueue(StringRequest stringRequest) {
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }






    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}
