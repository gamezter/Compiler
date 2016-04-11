import java.util.ArrayList;

public class Type {
	String type;
	ArrayList<Integer> dimensions;
	ArrayList<Type> index;
	String value;
	Symbol symbol;
	
	Type(String type, ArrayList<Integer> dimensions){
		this.type = type;
		this.dimensions = dimensions;
	}
}
