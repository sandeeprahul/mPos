package in.hng.mpos.gettersetter;

import java.util.ArrayList;

public class PreviousBillApiPojo {
    private String Status;

    private ArrayList<Details> detail;

    private String statusCode;

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public ArrayList<Details> getDetail() {
        return detail;
    }

    public void setDetail(ArrayList<Details> detail) {
        this.detail = detail;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public PreviousBillApiPojo(String status, ArrayList<Details> detail, String statusCode) {
        Status = status;
        this.detail = detail;
        this.statusCode = statusCode;
    }

    public PreviousBillApiPojo() {
    }

    @Override
    public String toString() {
        return "PreviousBillApiPojo{" +
                "Status='" + Status + '\'' +
                ", detail=" + detail +
                ", statusCode='" + statusCode + '\'' +
                '}';
    }
}
