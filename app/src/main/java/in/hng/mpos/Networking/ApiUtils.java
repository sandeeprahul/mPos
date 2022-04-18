package in.hng.mpos.Networking;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiUtils {
    public static final String TAG = ApiUtils.class.getSimpleName();


    public static String getErrorResponseObject(VolleyError error) {
        String errorData = null;
        try {
            if (error != null) {
                errorData = new String(error.networkResponse.data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorData;
    }

    public static JSONArray getErrorResponseArray(VolleyError error) {
        JSONArray errorData = null;
        try {
            if (error != null) {
                errorData = new JSONArray(new String(error.networkResponse.data, "UTF8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorData;
    }

    public static String getErrorResponseString(VolleyError error) {
        String errorData = "";
        try {
            if (error != null) {
                errorData = new String(error.networkResponse.data, "UTF8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorData;
    }



    public static int getErrorResponseCode(VolleyError error) {
        int httpStatusCode = 0;
        try {
            if (error != null) {
                httpStatusCode = error.networkResponse.statusCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpStatusCode;
    }






}



