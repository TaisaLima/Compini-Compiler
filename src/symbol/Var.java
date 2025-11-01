package symbol;

public class Var {
    protected String name;
    protected Type type;
    
    public Var(String name, Type type) {
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
