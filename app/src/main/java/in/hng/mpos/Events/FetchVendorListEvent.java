package in.hng.mpos.Events;

import org.json.JSONObject;

public class FetchVendorListEvent extends BaseHttpResponseEvent {



    public FetchVendorListEvent(String responseObj, int statusCode) {
        super(responseObj, statusCode);
    }
}

