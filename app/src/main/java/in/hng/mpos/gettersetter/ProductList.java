package in.hng.mpos.gettersetter;

/**
 * Created by Cbly on 2/3/2018.
 */
public class ProductList
{
    String billNo;
    String orderID;
    String skuName;
    String skuCode;
    String storeSKU;
    String Qty;
    String orderQty;
    String Mrp;
    String taxCode;
    String taxRate;

    String totalAmt;
    String sellValue;
    String discMRP;
    String unitDisc;
    String unitSell;

    String eanCode;


    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getStoreSKU() {
        return storeSKU;
    }

    public void setStoreSKU(String storeSKU) {
        this.storeSKU = storeSKU;
    }

    public void setQty(String qty) {
        this.Qty = qty;
    }

    public String getQty() {
        return Qty;
    }

    public void setOrderQty(String orderQty) {
        this.orderQty = orderQty;
    }

    public String getOrderQty() {
        return orderQty;
    }

    public void setMrp(String mrp) {
        this.Mrp = mrp;
    }

    public String getMrp() { return Mrp; }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getTaxCode() { return taxCode; }

    public void setTaxRate(String taxRate) {
        this.taxRate = taxRate;
    }

    public String getTaxRate() { return taxRate; }

    public void setTotalAmt(String totalAmt) {
        this.totalAmt = totalAmt;
    }

    public String getTotalAmt() { return totalAmt; }

    public void setSellValue(String sellValue) {
        this.sellValue = sellValue;
    }

    public String getSellValue() { return sellValue; }

    public void setDiscMRP(String discMRP) {
        this.discMRP = discMRP;
    }

    public String getDiscMRP() { return discMRP; }

    public void setUnitDisc(String unitDisc) {
        this.unitDisc = unitDisc;
    }

    public String getUnitDisc() { return unitDisc; }

    public void setUnitSell(String unitSell) {
        this.unitSell = unitSell;
    }

    public String getUnitSell() { return unitSell; }

    public String getEanCode() {
        return eanCode;
    }

    public void setEanCode(String eanCode) {
        this.eanCode = eanCode;
    }





}
