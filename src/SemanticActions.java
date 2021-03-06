import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class SemanticActions {
	
	BufferedWriter symbolTables;
	BufferedWriter error;
	BufferedWriter code;
	
	Boolean entry = false;
	SymbolTable global;
	SymbolTable current;
	ArrayList<Integer> arraySizeList = new ArrayList<Integer>();
	
	Stack<Item> s = new Stack<Item>();
	Stack<SymbolTable> scope = new Stack<SymbolTable>();
	Stack<Type> factors = new Stack<Type>();
	
	SemanticActions(String fileName, BufferedWriter error, BufferedWriter code) throws IOException{
		File fSA = new File("symbolTables-" + fileName);
		symbolTables = new BufferedWriter(new FileWriter(fSA));
		this.error = error;
		this.code = code;
	}
	
	private class Item{
		//String type;// type or id
		String lexeme;
		ArrayList<Type> dimensions = new ArrayList<Type>();
		int line;
		
		Item(String lexeme, int line){
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
				if(id.lexeme.length() < 4)c.label = id.lexeme + "_C_" + token.line;
				else c.label = id.lexeme.substring(0,4) + "_C_" + token.line;
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
				String parent;
				if(!current.equals(global)){
					if(current.getParentSymbol().identifier.length() < 4) parent =  current.getParentSymbol().identifier;
				else parent = current.getParentSymbol().identifier.substring(0,4);
				if(id.lexeme.length() < 4)f.label = parent + "_" + id.lexeme + "_F_" + token.line;
				else f.label = parent + "_" + id.lexeme.substring(0,4) + "_F_" + token.line;
				}else{
					f.label = "glob_" + id.lexeme.substring(0,4) + "_F_" + token.line;
				}
				
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
			p.label = "prog" + "_F_" + token.line;
			current = p.link;
			scope.push(current);
			break;
			}
		case "#newVar":
			id = s.pop();
			type = s.pop();
			Symbol existing = current.Search(id.lexeme);
			if(existing != null && existing.parentTable != current){
				error.write("SEMANTIC ERROR: VARIABLE " + id.lexeme + " ON LINE " + id.line + " HAS ALREADY BEEN DECLARED");error.newLine();
				break;
			}else if((!type.lexeme.equals("int") && !type.lexeme.equals("float") && current.Search(type.lexeme) == null) || current.getParentSymbol().equals(type.lexeme)){
				error.write("SEMANTIC ERROR: VARIABLE TYPE " + type.lexeme + " ON LINE " + id.line + " IS UNDECLARED");error.newLine();
				break;
			}else{
				Symbol v = current.insert(new Symbol(id.lexeme, Symbol.Kind.VARIABLE));
				v.type.add(new Type(type.lexeme, new ArrayList<Integer>(arraySizeList)));
				if(id.lexeme.length() < 4)v.label = id.lexeme + "_V_" + token.line;
				else v.label = id.lexeme.substring(0,4) + "_V_" + token.line;
				v.parentTable = current;
				arraySizeList.clear();
				if(v.type.get(0).type.equals("int") || v.type.get(0).type.equals("float")){
					if(v.type.get(0).dimensions == null || v.type.get(0).dimensions.isEmpty()){
						v.size = 1;
					}else{
						ArrayList<Integer> dim = v.type.get(0).dimensions;
						int size = 0;
						int multiplyer = 1;
						for(int i = dim.size() - 1; i > -1; i--){
							size += v.type.get(0).dimensions.get(i) * multiplyer;
							multiplyer *= v.type.get(0).dimensions.get(i);
						}
						v.size = size;
					}
				}else{
					if(v.type.get(0).dimensions == null || v.type.get(0).dimensions.isEmpty()){
						v.size = current.Search(v.type.get(0).type).size;
					}else{
						ArrayList<Integer> dim = v.type.get(0).dimensions;
						int size = 0;
						int multiplyer = current.Search(v.type.get(0).type).size;
						for(int i = dim.size() - 1; i > -1; i--){
							size += v.type.get(0).dimensions.get(i) * multiplyer;
							multiplyer *= v.type.get(0).dimensions.get(i);
						}
						v.size = size;
					}
				}
				break;
			}
		case "#changeToParam":
			Symbol vp = current.getSymbolAt(current.getNumberOfSymbols() - 1);
			vp.kind = Symbol.Kind.PARAMETER;
			vp.label = vp.label.replace("_V_", "_P_");
			current.getParentSymbol().type.addAll(vp.type);
			break;
		case "#addDimension":
			arraySizeList.add(Integer.valueOf(token.lexeme));
			break;
		case "#pushId":
		case "#pushType":
			s.push(new Item(token.lexeme, token.line));
			break;
		case "#pop":
			s.pop();
			break;
		case "#exit":
			int size = 0;
			for(int i = 0; i < current.getNumberOfSymbols(); i++){
				size += current.getSymbolAt(i).size;
			}
			current.getParentSymbol().size = size;
			current = current.getParentTable();
			scope.pop();
			break;
		}
	}

	public void checking(String action, Token token) throws IOException{
		Item id;
		Type one, two;
		
		switch(action){
		case "%pushId":
			s.push(new Item(token.lexeme, token.line));
			break;
		case "%enter":
			current = current.Search(s.pop().lexeme).link;
			scope.push(current);
			break;
		case "%exit":
			current = current.getParentTable();
			scope.pop();
			break;
		case "%pop":
			s.pop();
			break;
		case "%checkId":
			id = s.pop();
			Symbol symbol = scope.peek().Search(id.lexeme);
			if(symbol == null){
				error.write("SEMANTIC ERROR: ID " + id.lexeme + " ON LINE " + id.line + " IS UNDECLARED");error.newLine();
				break;
			}else if(symbol.type.get(0).dimensions.size() < id.dimensions.size()){
				error.write("SEMANTIC ERROR: ID " + id.lexeme + " ON LINE " + id.line + " HAS WRONG ARRAY SIZE");error.newLine();
				break;
			}else{
				Boolean temp = symbol.type.get(0).type.equals("int");
				Boolean temp2 = symbol.type.get(0).type.equals("float");
				if(temp || temp2){
					scope.pop();
					scope.push(symbol.parentTable);
				}else{
					scope.push(scope.pop().Search(symbol.type.get(0).type).link);
				}
				
				if(symbol.type.get(0).dimensions.size() == id.dimensions.size()){
					factors.pop();
					factors.push(new Type(symbol.type.get(0).type, null));
					factors.peek().value = symbol.label;
					factors.peek().symbol = symbol;
					if(!id.dimensions.isEmpty()){
						factors.peek().index = new ArrayList<Type>(id.dimensions);
					}
				}else{
					factors.pop();
					factors.push(new Type(symbol.type.get(0).type, new ArrayList<Integer>()));
					factors.peek().value = symbol.label;
					int difference = symbol.type.get(0).dimensions.size() - id.dimensions.size();
					for(int i = difference; i > 0; i--){
						factors.peek().dimensions.add(symbol.type.get(0).dimensions.get(i - 1));
					}
					factors.peek().index = new ArrayList<Type>(id.dimensions);
					factors.peek().symbol = symbol;
				}
			}
			break;
		case "%add":
			two = factors.pop();
			one = factors.pop();
			
			if(one.dimensions != null && !one.dimensions.isEmpty() || two.dimensions != null && !two.dimensions.isEmpty()){
				error.write("SEMANTIC ERROR: CANNOT ADD WHOLE ARRAY ON LINE " + token.line);error.newLine();
				break;
			}
			if(two.type.equals(one.type) && (two.type.equals("float") || two.type.equals("int"))){
				factors.push(new Type(one.type, null));
			}else if(two.type.equals("int") && one.type.equals("float") || two.type.equals("float") && one.type.equals("int")){
				error.write("SEMANTIC WARNING: ADDING INT TO FLOAT ON LINE " + token.line);error.newLine();
				factors.push(new Type("float", null));
			}else{
				error.write("SEMANTIC WARNING: ADDING INVALID TYPES ON LINE " + token.line);error.newLine();
			}
			break;
		case "%rel":
			two = factors.pop();
			one = factors.pop();
			
			if(one.dimensions != null && !one.dimensions.isEmpty() || two.dimensions != null && !two.dimensions.isEmpty()){
				error.write("SEMANTIC ERROR: CANNOT COMPARE WHOLE ARRAY ON LINE " + token.line);error.newLine();
				break;
			}
			if(two.type.equals(one.type) && (two.type.equals("float") || two.type.equals("int"))){
				factors.push(new Type("bool", null));
			}else if(two.type.equals("int") && one.type.equals("float") || two.type.equals("float") && one.type.equals("int")){
				error.write("SEMANTIC WARNING: COMPARING INT TO FLOAT ON LINE " + token.line);error.newLine();
				factors.push(new Type("bool", null));
			}else{
				error.write("SEMANTIC WARNING: INVALID TYPES ON LINE " + token.line);error.newLine();
			}
			break;
		case "%mul":
			two = factors.pop();
			one = factors.pop();
			
			if(one.dimensions != null && !one.dimensions.isEmpty() || two.dimensions != null && !two.dimensions.isEmpty()){
				error.write("SEMANTIC ERROR: CANNOT MULTIPLY WHOLE ARRAY ON LINE " + token.line);error.newLine();
				break;
			}
			if(two.type.equals(one.type)){
				factors.push(new Type(one.type, null));
			}else if(two.type.equals("int") && one.type.equals("float") || two.type.equals("float") && one.type.equals("int")){
				error.write("SEMANTIC WARNING: MULTIPLYING INT TO FLOAT ON LINE " + token.line);error.newLine();
				factors.push(new Type("float", null));
			}else{
				error.write("SEMANTIC WARNING: MULTIPLYING INVALID TYPES ON LINE " + token.line);error.newLine();
			}
			break;
		case "%ass":
			two = factors.pop();
			one = factors.pop();
			
			if(one.dimensions != null && !one.dimensions.isEmpty() || two.dimensions != null && !two.dimensions.isEmpty()){
				error.write("SEMANTIC ERROR: CANNOT ASSIGN WHOLE ARRAY ON LINE " + token.line);error.newLine();
				break;
			}
			if(two.type.equals(one.type)){
				if(entry)	asmAss(one, two);
			}else if(two.type.equals("int") && one.type.equals("float")){
				error.write("SEMANTIC WARNING: CASTING INT TO FLOAT ON LINE " + token.line);error.newLine();
			}else{
				error.write("SEMANTIC ERROR: LEFT SIDE TYPE: " + one.type + " DOESN'T MATCH RIGHT SIDE TYPE: " + two.type + " ON LINE " + token.line);error.newLine();
			}
			break;
		case "%factorF":
			factors.push(new Type("float", null));
			factors.peek().value = token.lexeme;
			break;
		case "%factorI":
			factors.push(new Type("int", null));
			factors.peek().value = token.lexeme;
			break;
		case "%pushDummy":
			factors.push(new Type(null, null));
			break;
		case "%checkFunc":
			id = s.pop();
			Symbol func = scope.peek().Search(id.lexeme, id.dimensions.size());
			if(func == null){
				error.write("SEMANTIC ERROR: FUNCTION " + id.lexeme + " ON LINE " + id.line + " IS UNDEFINED");error.newLine();
				break;
			}else{
				Boolean same = true;
				for(int i = 0; i < func.type.size() - 1; i++){
					if(func.type.get(i+1).type.equals(id.dimensions.get(i).type)){
						for(int j = 0; j < func.type.get(i+1).dimensions.size(); j++){
							if(func.type.get(i+1).dimensions.get(j) != id.dimensions.get(i).dimensions.get(j)){
								same = false;
								error.write("SEMANTIC ERROR: FUNCTION PARAMETER " + id.dimensions.get(i).type + " DIMENSIONS DOESNT MATCH FUNCTION SIGNATURE PARAMETER DIMENSIONS " + func.type.get(i+1).dimensions.get(j) + " ON LINE " + id.line);error.newLine();
							}
						}
					}else{
						same = false;
						error.write("SEMANTIC ERROR: FUNCTION PARAMETER " + id.dimensions.get(i).type + " DOESNT MATCH FUNCTION SIGNATURE " + func.type.get(i+1).type + " ON LINE " + id.line);error.newLine();
					}
				}if(same){
					if(!scope.peek().equals(current)) factors.pop();
					factors.push(func.type.get(0));//push return type
				}
				scope.pop();
				scope.push(current);
			} break;
		case "%addIndex":
			Type dimension = factors.pop();
			if(!dimension.type.equals("int")){
				error.write("SEMANTIC ERROR: VARIABLE INDEX ON LINE " + token.line + " IS NOT AN INTEGER");error.newLine();
			}else{
				s.peek().dimensions.add(dimension);
			}
			break;
		case "%addParam":
			s.peek().dimensions.add(factors.pop());
			break;
		case "%doneExpr":
			factors.pop();
			break;
		case "%currentScope":
			scope.push(current);
			break;
		case "%exitScope":
			scope.pop();
			break;
		}
	}

	public void asm(String action) throws IOException{
		if(action.equals("&enter")) entry = true;
		if(entry){
			switch(action){
			case "&enter":
				entry = true;
				code.write("                    entry");code.newLine();
				asmDeclare(global.Search("program").link);
				break;
			case "&exit":
				entry = false;
				code.write("					hlt");code.newLine();
				break;
			}
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
		code.close();
	}
	public void asmDeclare(SymbolTable table)throws IOException{
		for(int i = 0; i < table.getNumberOfSymbols(); i++){
			Symbol s = table.getSymbolAt(i);
			code.write(String.format("%1$-20s",s.label));
			if(s.size == 1){
				code.write("dw\t\t0");code.newLine();
			}else{
				code.write("res\t\t" + s.size);code.newLine();	
			}
		}
	}
	public void asmAss(Type one, Type two) throws IOException{
		if(one.index == null || one.index.isEmpty()){
			if(two.value != null && two.value.matches("-?\\d+(\\.\\d+)?")){//assign number
				code.write("\t\t\t\t\tlw\t\tr1," + one.value + "(r0)");code.newLine();
				code.write("\t\t\t\t\taddi\tr1,r1," + two.value);code.newLine();
				code.write("\t\t\t\t\tsw\t\t" + one.value + "(r0),r1");code.newLine();
			}else{
				//TODO assign function return value
			}
		}else{
			int offset = 0;
			int multiplyer = 1;
			for(int i = one.index.size() -1; i > -1;i--){
				offset += Integer.parseInt(one.index.get(i).value) * multiplyer;
				multiplyer *= one.symbol.type.get(0).dimensions.get(i);
			}
			code.write("\t\t\t\t\taddi\tr4,r0," + offset);code.newLine();
			code.write("\t\t\t\t\tlw\t\tr1," + one.value + "(r4)");code.newLine();
			code.write("\t\t\t\t\taddi\tr1,r1," + two.value);code.newLine();
			code.write("\t\t\t\t\tsw\t\t" + one.value + "(r0),r1");code.newLine();
			//TODO if the left side has an array value
		}
	}
}
