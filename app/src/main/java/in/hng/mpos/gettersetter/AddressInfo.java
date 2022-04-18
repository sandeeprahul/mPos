package in.hng.mpos.gettersetter;

public class AddressInfo {
    private String addrID = "";
    private String addr = "";
    private String addrType="";
    private String isDefault="";

    public String getAddrID() {
        return addrID;
    }

    public void setAddrID(String addrID) {
        this.addrID = addrID;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getAddrType() {
        return addrType;
    }

    public void setAddrType(String addrType) {
        this.addrType = addrType;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }
}
