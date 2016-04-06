import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class SemanticActions {
	
	BufferedWriter symbolTables;
	BufferedWriter error;
	
	SymbolTable global;
	SymbolTable current;
	ArrayList<Integer> arraySizeList = new ArrayList<Integer>();
	
	Stack<Item> s = new Stack<Item>();
	Stack<SymbolTable> scope = new Stack<SymbolTable>();
	int depth = 0;
	
	SemanticActions(String fileName, BufferedWriter error) throws IOException{
		File fSA = new File("symbolTables-" + fileName);
		symbolTables = new BufferedWriter(new FileWriter(fSA));
		this.error = error;
	}
	
	private class Item{
		String type;// type or id
		String lexeme;
		int shallowDimensions;
		int line;
		
		Item(String type, String lexeme, int line){
			this.type = type;
			this.lexeme = lexeme;
			this.line = line;
		}
	}
	
	public void action(String action, Token token) throws IOException{
		Item id, type;
		
		switch(action){
		case "#newGlobal":
			global = new SymbolTable(null);
			current = global;
			scope.push(global);
			break;
		case "#newClass":
			id = s.pop();
			if(global.Search(id.lexeme) != null){
				error.write("SEMANTIC ERROR: CLASS " + id.lexeme + " ON LINE " + id.line + " HAS ALREADY BEEN DECLARED.");error.newLine();
				break;
			}else{
				Symbol c = current.insert(new Symbol(id.lexeme, Symbol.Kind.CLASS));
				c.link = new SymbolTable(c);
				c.parentTable = current;
				current = c.link;
				scope.push(current);
				break;
			}
		case "#newFunc":
			id = s.pop();
			type = s.pop();
			if(current.Search(id.lexeme) != null){
				error.write("SEMANTIC ERROR: FUNCTION " + id.lexeme + " ON LINE " + id.line + " HAS ALREADY BEEN DECLARED");error.newLine();
				break;
			}else if((!type.lexeme.equals("int") && !type.lexeme.equals("float") && current.Search(type.lexeme) == null)){
				error.write("SEMANTIC ERROR: RETURN TYPE " + type.lexeme + " ON LINE " + id.line + " HAS NOT BEEN DECLARED");error.newLine();
				break;
			}else{
				Symbol f = current.insert(new Symbol(id.lexeme, Symbol.Kind.FUNCTION));
				f.link = new SymbolTable(f);
				f.parentTable = current;
				f.type.add(new Type(type.lexeme, new ArrayList<Integer>()));
				current = f.link;
				scope.push(current);
				break;
			}
		case "#newProg":
			if(global.Search("program") != null){
				error.write("SEMANTIC ERROR: program FUNCTION REDECLARED");error.newLine();
				break;
			}else{
				Symbol p = current.insert(new Symbol("program", Symbol.Kind.FUNCTION));
			p.link = new SymbolTable(p);
			p.parentTable = current;
			p.type.add(new Type("void", new ArrayList<Integer>()));
			current = p.link;
			scope.push(current);
			break;
			}
		case "#newVar":
			id = s.pop();
			type = s.pop();
			if(current.Search(id.lexeme) != null){
				error.write("SEMANTIC ERROR: VARIABLE " + id.lexeme + " ON LINE " + id.line + " HAS ALREADY BEEN DECLARED");error.newLine();
				break;
			}else if((!type.lexeme.equals("int") && !type.lexeme.equals("float") && current.Search(type.lexeme) == null) || current.getParentSymbol().equals(type.lexeme)){
				error.write("SEMANTIC ERROR: VARIABLE TYPE " + type.lexeme + " ON LINE " + id.line + " IS UNDECLARED");error.newLine();
				break;
			}else{
				Symbol v = current.insert(new Symbol(id.lexeme, Symbol.Kind.VARIABLE));
				v.type.add(new Type(type.lexeme, new ArrayList<Integer>(arraySizeList)));
				arraySizeList.clear();
				break;
			}
		case "#changeToParam":
			Symbol vp = current.getSymbolAt(current.getNumberOfSymbols() - 1);
			vp.kind = Symbol.Kind.PARAMETER;
			current.getParentSymbol().type.addAll(vp.type);
			break;
		case "#addDimension":
			arraySizeList.add(Integer.valueOf(token.lexeme));
			break;
		case "#pushId":
			s.push(new Item("ID", token.lexeme, token.line));
			break;
		case "#pushType":
			s.push(new Item("TYPE", token.lexeme, token.line));
			break;
		case "#checkId":
			id = s.pop();
			Symbol symbol = scope.peek().Search(id.lexeme);
			if(symbol == null){
				error.write("SEMANTIC ERROR: ID " + id.lexeme + " ON LINE " + id.line + " IS UNDECLARED");error.newLine();
				break;
			}else if(symbol.type.get(0).dimensions.size() < id.shallowDimensions){
				error.write("SEMANTIC ERROR: ID " + id.lexeme + " ON LINE " + id.line + " HAS WRONG ARRAY SIZE");error.newLine();
				break;
			}else{
				Boolean temp = symbol.type.get(0).type.equals("int");
				Boolean temp2 = symbol.type.get(0).type.equals("float");
				if(temp || temp2){
				}else{
					scope.push(scope.peek().Search(symbol.type.get(0).type).link);
					depth++;
				}
			}
			break;
		case "#checkFunc":
			id = s.pop();
			Symbol func = scope.peek().Search(id.lexeme);
			if(func == null){
				error.write("SEMANTIC ERROR: FUNCTION " + id.lexeme + " ON LINE " + id.line + " IS UNDEFINED");error.newLine();
				break;
			}else if((func.type.size() - 1) != id.shallowDimensions){
				error.write("SEMANTIC ERROR: FUNCTION " + id.lexeme + " ON LINE " + id.line + " HAS WRONG NUMBER OF PARAMETERS");error.newLine();
				break;
			}else break;
		case "#addShallowDimension":
			s.peek().shallowDimensions++;
			break;
		case "#addShallowParam":
			s.peek().shallowDimensions++;
			break;
		case "#currentScope":
			scope.push(current);
			depth++;
			break;
		case "#exitScope":
			for(int i = 0; i < depth; i++){
				scope.pop();
			}
			depth = 0;
			break;
		case "#exit":
			current = current.getParentTable();
			scope.pop();
			break;
		}
	}

	public void print(SymbolTable st) throws IOException{
		st.print(symbolTables);
		for(int i = 0; i < st.getNumberOfSymbols(); i++){
			if(st.getSymbolAt(i).isTable()){
				print(st.getSymbolAt(i).link);
			}
		}
		
	}
	public void close() throws IOException{
		symbolTables.close();
	}
}
