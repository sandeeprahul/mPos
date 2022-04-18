package in.hng.mpos.gettersetter;

public class OrderInfo {

    private String orderID = "";
    private String orderDate = "";
    private String custName="";
    private String custMobile="";
    private String delType="";

    public String getOrderID() {
        return orderID;
    }
    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getOrderDate() {
        return orderDate;
    }
    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }


    public String getCustName() {
        return custName;
    }
    public void setCustName(String custName) {
        this.custName = custName;
    }


    public String getCustMobile() {
        return custMobile;
    }
    public void setCustMobile(String custMobile) {
        this.custMobile = custMobile;
    }


    public String getDelType() {
        return delType;
    }
    public void setDelType(String delType) {
        this.delType = delType;
    }
}
