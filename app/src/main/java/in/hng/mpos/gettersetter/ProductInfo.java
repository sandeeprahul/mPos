package in.hng.mpos.gettersetter;

public class ProductInfo {

    public String code;
    public String Name;
    public  Boolean IsChecked;

    public ProductInfo(String code, String name, Boolean isChecked) {
        this.code = code;
        Name = name;
        IsChecked = isChecked;
    }

    public ProductInfo() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Boolean getChecked() {
        return IsChecked;
    }

    public void setChecked(Boolean checked) {
        IsChecked = checked;
    }

    @Override
    public String toString() {
        return "ProductInfo{" +
                "code='" + code + '\'' +
                ", Name='" + Name + '\'' +
                ", IsChecked=" + IsChecked +
                '}';
    }
}
