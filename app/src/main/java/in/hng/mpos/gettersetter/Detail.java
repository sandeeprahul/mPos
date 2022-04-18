package in.hng.mpos.gettersetter;

public class Detail {
    private String value_name;

    private String Value_code;

    public Detail() {
    }

    public Detail(String value_name, String value_code) {
        this.value_name = value_name;
        Value_code = value_code;
    }

    public String getValue_name() {
        return value_name;
    }

    public void setValue_name(String value_name) {
        this.value_name = value_name;
    }

    public String getValue_code() {
        return Value_code;
    }

    public void setValue_code(String value_code) {
        Value_code = value_code;
    }
}
