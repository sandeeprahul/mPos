package in.hng.mpos.gettersetter.RegularizeManualBill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BillDetail {

    @SerializedName("bill_no")
    @Expose
    private String billNo;
    @SerializedName("line_no")
    @Expose
    private String lineNo;
    @SerializedName("store_sku_loc_stock_no")
    @Expose
    private String storeSkuLocStockNo;
    @SerializedName("sku_code")
    @Expose
    private String skuCode;
    @SerializedName("ean_code")
    @Expose
    private String eanCode;
    @SerializedName("sku_name")
    @Expose
    private String skuName;
    @SerializedName("sku_qty")
    @Expose
    private String skuQty;
    @SerializedName("mrp")
    @Expose
    private String mrp;
    @SerializedName("camp_code")
    @Expose
    private String campCode;
    @SerializedName("sku_discount")
    @Expose
    private String skuDiscount;
    @SerializedName("sell_value")
    @Expose
    private String sellValue;
    @SerializedName("tax_code")
    @Expose
    private String taxCode;
    @SerializedName("tax_value")
    @Expose
    private String taxValue;
    @SerializedName("other_discount")
    @Expose
    private String otherDiscount;
    @SerializedName("discounted_qty")
    @Expose
    private String discountedQty;
    @SerializedName("reason_id")
    @Expose
    private String reasonId;

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getLineNo() {
        return lineNo;
    }

    public void setLineNo(String lineNo) {
        this.lineNo = lineNo;
    }

    public String getStoreSkuLocStockNo() {
        return storeSkuLocStockNo;
    }

    public void setStoreSkuLocStockNo(String storeSkuLocStockNo) {
        this.storeSkuLocStockNo = storeSkuLocStockNo;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getEanCode() {
        return eanCode;
    }

    public void setEanCode(String eanCode) {
        this.eanCode = eanCode;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getSkuQty() {
        return skuQty;
    }

    public void setSkuQty(String skuQty) {
        this.skuQty = skuQty;
    }

    public String getMrp() {
        return mrp;
    }

    public void setMrp(String mrp) {
        this.mrp = mrp;
    }

    public String getCampCode() {
        return campCode;
    }

    public void setCampCode(String campCode) {
        this.campCode = campCode;
    }

    public String getSkuDiscount() {
        return skuDiscount;
    }

    public void setSkuDiscount(String skuDiscount) {
        this.skuDiscount = skuDiscount;
    }

    public String getSellValue() {
        return sellValue;
    }

    public void setSellValue(String sellValue) {
        this.sellValue = sellValue;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getTaxValue() {
        return taxValue;
    }

    public void setTaxValue(String taxValue) {
        this.taxValue = taxValue;
    }

    public String getOtherDiscount() {
        return otherDiscount;
    }

    public void setOtherDiscount(String otherDiscount) {
        this.otherDiscount = otherDiscount;
    }

    public String getDiscountedQty() {
        return discountedQty;
    }

    public void setDiscountedQty(String discountedQty) {
        this.discountedQty = discountedQty;
    }

    public String getReasonId() {
        return reasonId;
    }

    public void setReasonId(String reasonId) {
        this.reasonId = reasonId;
    }
}
