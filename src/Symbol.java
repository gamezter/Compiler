import java.util.ArrayList;

public class Symbol {
	public String identifier;//identifier
	public Kind kind;
	public ArrayList<Type> type;
	public SymbolTable link;
	public SymbolTable parentTable;
	public int size;
	public String label;
	
	enum Kind {CLASS, FUNCTION, VARIABLE, PARAMETER};
	
	public Boolean isTable(){
		if(link != null)return true;
		else return false;
	}
	
	Symbol(String name, Kind kind){
		this.identifier = name;
		this.kind = kind;
		this.type = new ArrayList<Type>();
	}
}
