package in.hng.mpos.gettersetter.BillPrintDetailsPojo;

public class Bill_detail {
    private String hgprice;

    private String taxvalue;

    private String hsncode;

    private String qty;

    private String skucode;

    private String netPrice;

    private String mrp;

    private String skuname;

    public String getHgprice() {
        return hgprice;
    }

    public void setHgprice(String hgprice) {
        this.hgprice = hgprice;
    }

    public String getTaxvalue() {
        return taxvalue;
    }

    public void setTaxvalue(String taxvalue) {
        this.taxvalue = taxvalue;
    }

    public String getHsncode() {
        return hsncode;
    }

    public void setHsncode(String hsncode) {
        this.hsncode = hsncode;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getSkucode() {
        return skucode;
    }

    public void setSkucode(String skucode) {
        this.skucode = skucode;
    }

    public String getNetPrice() {
        return netPrice;
    }

    public void setNetPrice(String netPrice) {
        this.netPrice = netPrice;
    }

    public String getMrp() {
        return mrp;
    }

    public void setMrp(String mrp) {
        this.mrp = mrp;
    }

    public String getSkuname() {
        return skuname;
    }

    public void setSkuname(String skuname) {
        this.skuname = skuname;
    }

    public Bill_detail() {
    }

    public Bill_detail(String hgprice, String taxvalue, String hsncode, String qty, String skucode, String netPrice, String mrp, String skuname) {
        this.hgprice = hgprice;
        this.taxvalue = taxvalue;
        this.hsncode = hsncode;
        this.qty = qty;
        this.skucode = skucode;
        this.netPrice = netPrice;
        this.mrp = mrp;
        this.skuname = skuname;
    }
}
