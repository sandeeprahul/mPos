package in.hng.mpos.ResponseModel;



import java.util.Arrays;

import in.hng.mpos.gettersetter.Service;
import in.hng.mpos.gettersetter.Specialist;

public class ProductServiceMaster {
    private Specialist[] specialist;

    private Service[] service;

    private String ststusCode;

    private String status;
    private String experts_limit;

    public String getExperts_limit() {
        return experts_limit;
    }

    public void setExperts_limit(String experts_limit) {
        this.experts_limit = experts_limit;
    }

    public Specialist[] getSpecialist ()
    {
        return specialist;
    }

    public void setSpecialist (Specialist[] specialist)
    {
        this.specialist = specialist;
    }

    public Service[] getService ()
    {
        return service;
    }

    public void setService (Service[] service)
    {
        this.service = service;
    }

    public String getStstusCode ()
    {
        return ststusCode;
    }

    public void setStstusCode (String ststusCode)
    {
        this.ststusCode = ststusCode;
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
    public String toString() {
        return "ProductServiceMaster{" +
                "specialist=" + Arrays.toString(specialist) +
                ", service=" + Arrays.toString(service) +
                ", ststusCode='" + ststusCode + '\'' +
                ", status='" + status + '\'' +
                ", experts_limit='" + experts_limit + '\'' +
                '}';
    }
}
