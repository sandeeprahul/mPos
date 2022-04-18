package in.hng.mpos.gettersetter.BillPrintDetailsPojo;

public class Taxdetails {
    private String taxvalue;

    private String SGST;

    private String hsnname;

    private String taxamount;

    private String taxcode;

    private String CGST;

    private String IGST;

    public String getTaxvalue() {
        return taxvalue;
    }

    public void setTaxvalue(String taxvalue) {
        this.taxvalue = taxvalue;
    }

    public String getSGST() {
        return SGST;
    }

    public void setSGST(String SGST) {
        this.SGST = SGST;
    }

    public String getHsnname() {
        return hsnname;
    }

    public void setHsnname(String hsnname) {
        this.hsnname = hsnname;
    }

    public String getTaxamount() {
        return taxamount;
    }

    public void setTaxamount(String taxamount) {
        this.taxamount = taxamount;
    }

    public String getTaxcode() {
        return taxcode;
    }

    public void setTaxcode(String taxcode) {
        this.taxcode = taxcode;
    }

    public String getCGST() {
        return CGST;
    }

    public void setCGST(String CGST) {
        this.CGST = CGST;
    }

    public String getIGST() {
        return IGST;
    }

    public void setIGST(String IGST) {
        this.IGST = IGST;
    }

    public Taxdetails() {
    }

    public Taxdetails(String taxvalue, String SGST, String hsnname, String taxamount, String taxcode, String CGST, String IGST) {
        this.taxvalue = taxvalue;
        this.SGST = SGST;
        this.hsnname = hsnname;
        this.taxamount = taxamount;
        this.taxcode = taxcode;
        this.CGST = CGST;
        this.IGST = IGST;
    }
}
