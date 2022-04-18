package in.hng.mpos.gettersetter.BillPrintDetailsPojo;

public class Billheaders {
    private String pincode;

    private String city;

    private String Address4;

    private String Address2;

    private String ph;

    private String Address3;

    private String Address1;

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress4() {
        return Address4;
    }

    public void setAddress4(String address4) {
        Address4 = address4;
    }

    public String getAddress2() {
        return Address2;
    }

    public void setAddress2(String address2) {
        Address2 = address2;
    }

    public String getPh() {
        return ph;
    }

    public void setPh(String ph) {
        this.ph = ph;
    }

    public String getAddress3() {
        return Address3;
    }

    public void setAddress3(String address3) {
        Address3 = address3;
    }

    public String getAddress1() {
        return Address1;
    }

    public void setAddress1(String address1) {
        Address1 = address1;
    }

    public Billheaders() {
    }

    public Billheaders(String pincode, String city, String address4, String address2, String ph, String address3, String address1) {
        this.pincode = pincode;
        this.city = city;
        Address4 = address4;
        Address2 = address2;
        this.ph = ph;
        Address3 = address3;
        Address1 = address1;
    }
}
