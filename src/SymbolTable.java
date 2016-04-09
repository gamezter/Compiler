import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SymbolTable {
	private ArrayList<Symbol> symbols;
	private Symbol parentSymbol;
	
	SymbolTable(Symbol parent){
		symbols = new ArrayList<Symbol>();
		this.parentSymbol = parent;
	}
	
	public Symbol Search(String name){
		for(int i = 0; i < symbols.size(); i++){
			if(name.equals(symbols.get(i).identifier)){
				return symbols.get(i);
			}
		}
		if(parentSymbol != null){
			return parentSymbol.parentTable.Search(name);
		}else{
			return null;
		}
	}
	public Symbol Search(String name, int parameters){
		for(int i = 0; i < symbols.size(); i++){
			Symbol temp = symbols.get(i);
			if(name.equals(temp.identifier) && (temp.type.size() - 1) == parameters){
				return symbols.get(i);
			}
		}
		if(parentSymbol != null){
			return parentSymbol.parentTable.Search(name, parameters);
		}else{
			return null;
		}
	}
	
	public Symbol insert(Symbol e){
		symbols.add(e);
		return e;
	}
	public Symbol getParentSymbol(){
		return parentSymbol;
	}
	public SymbolTable getParentTable(){
		return parentSymbol.parentTable;
	}
	
	public void delete(){
		parentSymbol.parentTable.symbols.remove(parentSymbol);
		parentSymbol.link = null;
	}
	
	public int getNumberOfSymbols(){
		return symbols.size();
	}
	
	public Symbol getSymbolAt(int i){
		return symbols.get(i);
	}
	
	public void print(BufferedWriter bW) throws IOException{
		bW.newLine();
		if(parentSymbol != null)bW.write("Symbol Table: " + parentSymbol.identifier);
		else 					bW.write("Symbol Table: global");
		bW.newLine();
		
		bW.write("                Name|                Kind|                         Type|                Link|");bW.newLine();
		bW.write("--------------------|--------------------|-----------------------------|--------------------|");bW.newLine();
		for(int i = 0; i < symbols.size(); i++){
			bW.write(String.format("%1$20s", symbols.get(i).identifier) + "|" + String.format("%1$20s", symbols.get(i).kind.toString()) + "|");
			
			StringBuilder types = new StringBuilder();
			int j = 0;
			if(symbols.get(i).kind == Symbol.Kind.FUNCTION){
				types.append(symbols.get(i).type.get(0).type);
				if(!symbols.get(i).type.get(0).dimensions.isEmpty()){
					for(int k = 0; k < symbols.get(i).type.get(0).dimensions.size(); k++){
						types.append("[" + symbols.get(i).type.get(0).dimensions.get(k) + "]");
					}
				}
				types.append("|");
				j++;
			}
			for(; j < symbols.get(i).type.size();j++){
				types.append(symbols.get(i).type.get(j).type);
				if(!symbols.get(i).type.get(j).dimensions.isEmpty()){
					for(int k = 0; k < symbols.get(i).type.get(j).dimensions.size(); k++){
						types.append("[" + symbols.get(i).type.get(j).dimensions.get(k) + "]");
					}
				}
				if(j != symbols.get(i).type.size() - 1){
					types.append(", ");
				}
			}
			types.append("|");
			if(symbols.get(i).link != null){
				bW.write(String.format("%1$30s", types.toString()) + String.format("%1$20s", symbols.get(i).link.parentSymbol.identifier) + "|");
			}else{
				bW.write(String.format("%1$30s", types.toString()) + String.format("%1$20s", "") + "|");
			}
			bW.newLine();
		}
	}
}
