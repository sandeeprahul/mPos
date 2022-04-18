package in.hng.mpos.gettersetter;

public class FetchVendorsModel {
    private String Status;
    private String Message;

    private Detail[] detail;

    private String statusCode;

    public FetchVendorsModel() {
    }

    public FetchVendorsModel(String status, Detail[] detail, String statusCode) {
        Status = status;
        this.detail = detail;
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Detail[] getDetail() {
        return detail;
    }

    public void setDetail(Detail[] detail) {
        this.detail = detail;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}
