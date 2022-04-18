package in.hng.mpos.Events;

import org.json.JSONArray;
import org.json.JSONObject;

public class BaseHttpResponseEvent {
    public JSONObject responseObj;
    public JSONArray responseArray;
    public String responseString;
    public int statusCode;

    public BaseHttpResponseEvent(JSONObject responseObj, int statusCode) {
        this.responseObj = responseObj;
        this.statusCode = statusCode;
    }

    public BaseHttpResponseEvent(String responseString, int statusCode) {
        this.responseString = responseString;
        this.statusCode = statusCode;
    }

    public BaseHttpResponseEvent(JSONArray responseArray, int statusCode) {
        this.responseArray = responseArray;
        this.statusCode = statusCode;
    }


    public JSONObject getResponseObj() {
        return responseObj;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "BaseHttpResponseEvent{" +
                "responseObj=" + responseObj +
                ", statusCode=" + statusCode +
                '}';
    }

}
