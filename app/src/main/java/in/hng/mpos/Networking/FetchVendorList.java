package in.hng.mpos.Networking;

import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import in.hng.mpos.Events.FetchVendorListEvent;
import in.hng.mpos.gettersetter.FetchVendorsModel;

public class FetchVendorList extends BaseStringRequest {
    public static final String TAG = FetchVendorList.class.getSimpleName();

    @Override
    public void onResponse(String response) {
        super.onResponse(response);
        Log.d(TAG, "[onResponse]{From Volley}" + response.toString());

        FetchVendorsModel fetchVendorsModel = new FetchVendorsModel();
        JsonParser parser = new JsonParser();
        JsonElement mJson = parser.parse(response);
        Gson gson = new Gson();
        fetchVendorsModel = gson.fromJson(mJson, FetchVendorsModel.class);
        if (fetchVendorsModel.getStatusCode().equalsIgnoreCase("200"))
            EventBus.getDefault().post(new FetchVendorListEvent(response, 200));
        else
            EventBus.getDefault().post(new FetchVendorListEvent(fetchVendorsModel.getMessage(), Integer.parseInt(fetchVendorsModel.getStatusCode())));
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        Log.d(TAG, "[onErrorResponse]{From Volley}" + error.toString());
        EventBus.getDefault().post(new FetchVendorListEvent(ApiUtils.getErrorResponseObject(error), ApiUtils.getErrorResponseCode(error)));

    }
}
