package in.hng.mpos.gettersetter.BillPrintDetailsPojo;

public class Customer_Casher {
    private String mobilenumber;

    private String casher_UserCode;

    private String GST_TIN;

    private String customername;

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getCasher_UserCode() {
        return casher_UserCode;
    }

    public void setCasher_UserCode(String casher_UserCode) {
        this.casher_UserCode = casher_UserCode;
    }

    public String getGST_TIN() {
        return GST_TIN;
    }

    public void setGST_TIN(String GST_TIN) {
        this.GST_TIN = GST_TIN;
    }

    public String getCustomername() {
        return customername;
    }

    public void setCustomername(String customername) {
        this.customername = customername;
    }

    public Customer_Casher() {
    }

    public Customer_Casher(String mobilenumber, String casher_UserCode, String GST_TIN, String customername) {
        this.mobilenumber = mobilenumber;
        this.casher_UserCode = casher_UserCode;
        this.GST_TIN = GST_TIN;
        this.customername = customername;
    }
}
