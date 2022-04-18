package in.hng.mpos.ResponseModel;

public class ProductServiceInsert {
    private String location_code;

    private String ststusCode;

    private Details[] details;

    private String status;

    public String getLocation_code ()
    {
        return location_code;
    }

    public void setLocation_code (String location_code)
    {
        this.location_code = location_code;
    }

    public String getStstusCode ()
    {
        return ststusCode;
    }

    public void setStstusCode (String ststusCode)
    {
        this.ststusCode = ststusCode;
    }

    public Details[] getDetails ()
    {
        return details;
    }

    public void setDetails (Details[] details)
    {
        this.details = details;
    }

    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [location_code = "+location_code+", ststusCode = "+ststusCode+", details = "+details+", status = "+status+"]";
    }
}
