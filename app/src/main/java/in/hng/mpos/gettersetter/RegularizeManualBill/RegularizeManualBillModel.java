package in.hng.mpos.gettersetter.RegularizeManualBill;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegularizeManualBillModel {

    @SerializedName("statusCode")
    @Expose
    private String statusCode;
    @SerializedName("Status")
    @Expose
    private String status;
    @SerializedName("detail")
    @Expose
    private List<Detail> detail = null;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Detail> getDetail() {
        return detail;
    }

    public void setDetail(List<Detail> detail) {
        this.detail = detail;
    }

}

