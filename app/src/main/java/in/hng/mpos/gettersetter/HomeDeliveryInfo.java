package in.hng.mpos.gettersetter;

import java.util.ArrayList;

public class HomeDeliveryInfo {

    private String name;
    private ArrayList<AddressInfo> list = new ArrayList<AddressInfo>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<AddressInfo> getAddressList() {
        return list;
    }

    public void setAddressList(ArrayList<AddressInfo> productList) {
        this.list = productList;
    }
}
