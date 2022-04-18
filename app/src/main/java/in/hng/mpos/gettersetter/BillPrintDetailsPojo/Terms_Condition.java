package in.hng.mpos.gettersetter.BillPrintDetailsPojo;

public class Terms_Condition {
    private String modifieduser;

    private String creationdatetime;

    private String createdby;

    private String modifieddate;

    private String short_name;

    private String value;

    public String getModifieduser() {
        return modifieduser;
    }

    public void setModifieduser(String modifieduser) {
        this.modifieduser = modifieduser;
    }

    public String getCreationdatetime() {
        return creationdatetime;
    }

    public void setCreationdatetime(String creationdatetime) {
        this.creationdatetime = creationdatetime;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public String getModifieddate() {
        return modifieddate;
    }

    public void setModifieddate(String modifieddate) {
        this.modifieddate = modifieddate;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Terms_Condition() {
    }

    public Terms_Condition(String modifieduser, String creationdatetime, String createdby, String modifieddate, String short_name, String value) {
        this.modifieduser = modifieduser;
        this.creationdatetime = creationdatetime;
        this.createdby = createdby;
        this.modifieddate = modifieddate;
        this.short_name = short_name;
        this.value = value;
    }
}
