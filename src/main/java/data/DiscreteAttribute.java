package data;

public class DiscreteAttribute extends Attribute {
    private String[] values;

    public DiscreteAttribute(String name, int index, String values[]){
        super(name, index);
        this.values = values;
    }

    public int getNumberOfDistinctValues(){
        return this.values.length;
    }

    public String getValue(int i) {
        return this.values[i];
    }
}
