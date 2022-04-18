package in.hng.mpos.gettersetter.RegularizeManualBill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Detail implements Serializable {

    @SerializedName("bill_no")
    @Expose
    private String billNo;
    @SerializedName("bill_date")
    @Expose
    private String billDate;
    @SerializedName("location_code")
    @Expose
    private String locationCode;
    @SerializedName("till_no")
    @Expose
    private String tillNo;
    @SerializedName("bill_type")
    @Expose
    private String billType;
    @SerializedName("bill_mode")
    @Expose
    private String billMode;
    @SerializedName("bill_status")
    @Expose
    private String billStatus;
    @SerializedName("cashier_user_code")
    @Expose
    private String cashierUserCode;
    @SerializedName("total_sku_discount")
    @Expose
    private String totalSkuDiscount;
    @SerializedName("total_other_discount")
    @Expose
    private String totalOtherDiscount;
    @SerializedName("rounding_diff")
    @Expose
    private String roundingDiff;
    @SerializedName("total_tax_value")
    @Expose
    private String totalTaxValue;
    @SerializedName("bill_value")
    @Expose
    private String billValue;
    @SerializedName("customer_phone")
    @Expose
    private String customerPhone;
    @SerializedName("customer_name")
    @Expose
    private String customerName;
    @SerializedName("mbb_reason")
    @Expose
    private String mbbReason;
    @SerializedName("reason_id")
    @Expose
    private String reasonId;
    @SerializedName("Actual_bill_no")
    @Expose
    private String actualBillNo;
    @SerializedName("actual_bill_up_date")
    @Expose
    private String actualBillUpDate;
    @SerializedName("cashier_code_up")
    @Expose
    private String cashierCodeUp;

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getTillNo() {
        return tillNo;
    }

    public void setTillNo(String tillNo) {
        this.tillNo = tillNo;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public String getBillMode() {
        return billMode;
    }

    public void setBillMode(String billMode) {
        this.billMode = billMode;
    }

    public String getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(String billStatus) {
        this.billStatus = billStatus;
    }

    public String getCashierUserCode() {
        return cashierUserCode;
    }

    public void setCashierUserCode(String cashierUserCode) {
        this.cashierUserCode = cashierUserCode;
    }

    public String getTotalSkuDiscount() {
        return totalSkuDiscount;
    }

    public void setTotalSkuDiscount(String totalSkuDiscount) {
        this.totalSkuDiscount = totalSkuDiscount;
    }

    public String getTotalOtherDiscount() {
        return totalOtherDiscount;
    }

    public void setTotalOtherDiscount(String totalOtherDiscount) {
        this.totalOtherDiscount = totalOtherDiscount;
    }

    public String getRoundingDiff() {
        return roundingDiff;
    }

    public void setRoundingDiff(String roundingDiff) {
        this.roundingDiff = roundingDiff;
    }

    public String getTotalTaxValue() {
        return totalTaxValue;
    }

    public void setTotalTaxValue(String totalTaxValue) {
        this.totalTaxValue = totalTaxValue;
    }

    public String getBillValue() {
        return billValue;
    }

    public void setBillValue(String billValue) {
        this.billValue = billValue;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getMbbReason() {
        return mbbReason;
    }

    public void setMbbReason(String mbbReason) {
        this.mbbReason = mbbReason;
    }

    public String getReasonId() {
        return reasonId;
    }

    public void setReasonId(String reasonId) {
        this.reasonId = reasonId;
    }

    public String getActualBillNo() {
        return actualBillNo;
    }

    public void setActualBillNo(String actualBillNo) {
        this.actualBillNo = actualBillNo;
    }

    public String getActualBillUpDate() {
        return actualBillUpDate;
    }

    public void setActualBillUpDate(String actualBillUpDate) {
        this.actualBillUpDate = actualBillUpDate;
    }

    public String getCashierCodeUp() {
        return cashierCodeUp;
    }

    public void setCashierCodeUp(String cashierCodeUp) {
        this.cashierCodeUp = cashierCodeUp;
    }
}