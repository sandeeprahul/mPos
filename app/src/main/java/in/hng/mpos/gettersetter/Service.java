package in.hng.mpos.gettersetter;

public class Service {
    private String code;

    private String name;

    private String sel;

    public String getCode ()
    {
        return code;
    }

    public void setCode (String code)
    {
        this.code = code;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getSel ()
    {
        return sel;
    }

    public void setSel (String sel)
    {
        this.sel = sel;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [code = "+code+", name = "+name+", sel = "+sel+"]";
    }
}
