package in.hng.mpos.gettersetter.BillPrintDetailsPojo;

import java.util.ArrayList;

public class BillPrintPojo {
    private String otc_licence;

    private ArrayList<Terms_Condition> Terms_Condition;

    private String earnedpoint;

    private String printtext;

    private ArrayList<Bill_detail> bill_detail;

    private Bill_payment bill_payment;

    private Customer_Casher Customer_Casher;

    private Taxdetails taxdetails;

    private Billheaders billheaders;

    private String locationcode;

    public BillPrintPojo() {
    }

    public String getOtc_licence() {
        return otc_licence;
    }

    public void setOtc_licence(String otc_licence) {
        this.otc_licence = otc_licence;
    }

    public ArrayList<in.hng.mpos.gettersetter.BillPrintDetailsPojo.Terms_Condition> getTerms_Condition() {
        return Terms_Condition;
    }

    public void setTerms_Condition(ArrayList<in.hng.mpos.gettersetter.BillPrintDetailsPojo.Terms_Condition> terms_Condition) {
        Terms_Condition = terms_Condition;
    }

    public String getEarnedpoint() {
        return earnedpoint;
    }

    public void setEarnedpoint(String earnedpoint) {
        this.earnedpoint = earnedpoint;
    }

    public String getPrinttext() {
        return printtext;
    }

    public void setPrinttext(String printtext) {
        this.printtext = printtext;
    }

    public ArrayList<Bill_detail> getBill_detail() {
        return bill_detail;
    }

    public void setBill_detail(ArrayList<Bill_detail> bill_detail) {
        this.bill_detail = bill_detail;
    }

    public Bill_payment getBill_payment() {
        return bill_payment;
    }

    public void setBill_payment(Bill_payment bill_payment) {
        this.bill_payment = bill_payment;
    }

    public in.hng.mpos.gettersetter.BillPrintDetailsPojo.Customer_Casher getCustomer_Casher() {
        return Customer_Casher;
    }

    public void setCustomer_Casher(in.hng.mpos.gettersetter.BillPrintDetailsPojo.Customer_Casher customer_Casher) {
        Customer_Casher = customer_Casher;
    }

    public Taxdetails getTaxdetails() {
        return taxdetails;
    }

    public void setTaxdetails(Taxdetails taxdetails) {
        this.taxdetails = taxdetails;
    }

    public Billheaders getBillheaders() {
        return billheaders;
    }

    public void setBillheaders(Billheaders billheaders) {
        this.billheaders = billheaders;
    }

    public String getLocationcode() {
        return locationcode;
    }

    public void setLocationcode(String locationcode) {
        this.locationcode = locationcode;
    }

    public BillPrintPojo(String otc_licence, ArrayList<in.hng.mpos.gettersetter.BillPrintDetailsPojo.Terms_Condition> terms_Condition, String earnedpoint, String printtext, ArrayList<Bill_detail> bill_detail, Bill_payment bill_payment, in.hng.mpos.gettersetter.BillPrintDetailsPojo.Customer_Casher customer_Casher, Taxdetails taxdetails, Billheaders billheaders, String locationcode) {
        this.otc_licence = otc_licence;
        Terms_Condition = terms_Condition;
        this.earnedpoint = earnedpoint;
        this.printtext = printtext;
        this.bill_detail = bill_detail;
        this.bill_payment = bill_payment;
        Customer_Casher = customer_Casher;
        this.taxdetails = taxdetails;
        this.billheaders = billheaders;
        this.locationcode = locationcode;
    }
}
