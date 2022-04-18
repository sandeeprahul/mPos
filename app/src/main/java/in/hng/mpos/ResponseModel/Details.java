package in.hng.mpos.ResponseModel;

public class Details {
    private String specialist_id;

    private String bill_no;

    private String type_code;

    public String getSpecialist_id ()
    {
        return specialist_id;
    }

    public void setSpecialist_id (String specialist_id)
    {
        this.specialist_id = specialist_id;
    }

    public String getBill_no ()
    {
        return bill_no;
    }

    public void setBill_no (String bill_no)
    {
        this.bill_no = bill_no;
    }

    public String getType_code ()
    {
        return type_code;
    }

    public void setType_code (String type_code)
    {
        this.type_code = type_code;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [specialist_id = "+specialist_id+", bill_no = "+bill_no+", type_code = "+type_code+"]";
    }
}
