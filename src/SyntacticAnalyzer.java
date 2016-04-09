import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Stack;

public class SyntacticAnalyzer{
	
	BufferedWriter output;
	BufferedWriter error;
	BufferedWriter code;
	
	LexicalAnalyzer la;
	SemanticActions sa;
	Stack<String> stack = new Stack<String>();
	String[][] stateTransition = {
	/*prog*/			{"","","","","","","","","","","","","","","","","","","","","","#newGlobal classDeclList progBody","","","","","","","","","","","","#newGlobal classDeclList progBody","","","","pop"},		
	/*classDeclList*/	{"","","","","","","","","","","","","","","","","","","","","","classDecl classDeclList","","","","","","","","","","","","EPSILON","","","","pop"},
	/*classDecl*/		{"","","","","","","","","","","","","","","","","","","","","","class #pushId %pushId id #newClass %enter { classBody } ; #exit %exit","","","","","","","","","","","","pop","","","","pop"},
	/*classBody*/		{"","","","","","","","","","","","EPSILON","","","","","","","","","","","","#pushType type #pushId %pushId id varOrFunc","","","","#pushType type #pushId %pushId id varOrFunc","","#pushType type #pushId %pushId id varOrFunc","","","","","","","","pop"},
	/*varOrFunc*/		{"","#newFunc %enter ( fParams ) funcBody ; #exit %exit funcDefList","","","","","","%pop arraySizeList #newVar ; classBody","%pop arraySizeList #newVar ; classBody","","","pop","","","","","","","","","","","","","","","","","","","","","","","","","","pop"},
	/*funcDefList*/		{"","","","","","","","","","","","EPSILON","","","","","","","","","","","","funcDef funcDefList","","","","funcDef funcDefList","","funcDef funcDefList","","","","","","","","EPSILON"},
	/*progBody*/		{"","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","%pushId program #newProg %enter funcBody ; #exit %exit funcDefList","","","","pop"},
	/*funcHead*/		{"","","","","","","","","","","pop","","","","","","","","","","","","","#pushType type #pushId %pushId id #newFunc %enter ( fParams )","","","","#pushType type #pushId %pushId id #newFunc %enter ( fParams )","","#pushType type #pushId %pushId id #newFunc %enter ( fParams )","","","","","","","","pop"},
	/*funcDef*/			{"","","","","","","","","","","","pop","","","","","","","","","","","","funcHead funcBody ; #exit %exit","","","","funcHead funcBody ; #exit %exit","","funcHead funcBody ; #exit %exit","","","","","","","","pop"},
	/*funcBody*/		{"","","","","","","","pop","","","{ funcBlock }","pop","","","","","","","","","","","","pop","","","","pop","","pop","","","","","","","","pop"},
	/*funcBlock*/		{"","","","","","","","","","","","EPSILON","","","","","","","","","","","","#pushType float #pushId id arraySizeList #newVar ; funcBlock","","for ( #pushType type #pushId %pushId id #newVar %pushDummy %checkId assignOp expr %ass ; relExpr ; assignStat ) statBlock ; statementList","get ( variable ) ; statementList","#pushType %pushId id varOrStatement","if ( expr ) then statBlock else statBlock ; statementList","#pushType int #pushId id arraySizeList #newVar ; funcBlock","","","","","put ( expr ) ; statementList","return ( expr ) ; statementList","","pop"},
	/*varOrStatement*/	{"","","","","","#pop indiceList %pushDummy %checkId variable1 assignOp expr %ass ; statementList","","","#pop indiceList %pushDummy %checkId variable1 assignOp expr %ass ; statementList","","","pop","","","","","#pop indiceList %pushDummy %checkId variable1 assignOp expr %ass ; statementList","","","","","","","","","","","#pushId id arraySizeList #newVar ; funcBlock","","","","","","","","","","pop"},
	/*statementList*/	{"","","","","","","","","","","","EPSILON","","","","","","","","","","","","","","statement statementList","statement statementList","statement statementList","statement statementList","","","","","","statement statementList","statement statementList","","pop"},
	/*arraySizeList*/	{"","","EPSILON","","EPSILON","","","EPSILON","arraySize arraySizeList","","","","","","","","","","","","","","","","","","","","","","","","","","","","","pop"},
	/*statement*/		{"","","","","","","","pop","","","","pop","","","","","","","","","","","pop","","","for ( #pushType type #pushId %pushId id #newVar %pushDummy %checkId assignOp expr ; relExpr ; assignStat ) statBlock ;","get ( variable ) ;","assignStat ;","if ( expr ) then statBlock else statBlock ;","","","","","","put ( expr ) ;","return ( expr ) ;","","pop"},
	/*assignStat*/		{"","","pop","","","","","pop","","","","","","","","","","","","","","","","","","","","variable assignOp expr %ass","","","","","","","","","","pop"},
	/*statBlock*/		{"","","","","","","","EPSILON","","","{ statementList }","","","","","","","","","","","","EPSILON","","","statement","statement","statement","statement","","","","","","statement","statement","","pop"},
	/*expr*/			{"arithExpr expr1","arithExpr expr1","pop","","pop","","","pop","","","","","arithExpr expr1","","","","","","","","","","","","arithExpr expr1","","","arithExpr expr1","","","arithExpr expr1","arithExpr expr1","","","","","","pop"},
	/*expr1*/			{"","","EPSILON","","EPSILON","","","EPSILON","","","","","","relOp arithExpr %rel","relOp arithExpr %rel","relOp arithExpr %rel","","relOp arithExpr %rel","relOp arithExpr %rel","relOp arithExpr %rel","","","","","","","","","","","","","","","","","","pop"},
	/*relExpr*/			{"arithExpr relOp arithExpr %rel","arithExpr relOp arithExpr %rel","","","","","","pop","","","","","arithExpr relOp arithExpr %rel","","","","","","","","","","","","arithExpr relOp arithExpr %rel","","","arithExpr relOp arithExpr %rel","","","arithExpr relOp arithExpr %rel","arithExpr relOp arithExpr %rel","","","","","","pop"},
	/*arithExpr*/		{"term arithExpr1","term arithExpr1","pop","","pop","","","pop","","pop","","","term arithExpr1","pop","pop","pop","","pop","pop","pop","","","","","term arithExpr1","","","term arithExpr1","","","term arithExpr1","term arithExpr1","","","","","","pop"},
	/*arithExpr1*/		{"addOp term %add arithExpr1","","EPSILON","","EPSILON","","","EPSILON","","EPSILON","","","addOp term %add arithExpr1","EPSILON","EPSILON","EPSILON","","EPSILON","EPSILON","EPSILON","","","","","","","","","","","","","addOp term %add arithExpr1","","","","","pop"},
	/*sign*/			{"-","pop","","","","","","","","","","","+","","","","","","","","","","","","pop","","","pop","","","pop","pop","","","","","","pop"},
	/*term*/			{"factor term1","factor term1","pop","","pop","","","pop","","pop","","","factor term1","pop","pop","pop","","pop","pop","pop","","","","","factor term1","","","factor term1","","","factor term1","factor term1","pop","","","","","pop"},
	/*term1*/			{"EPSILON","","EPSILON","multOp factor %mul term1","EPSILON","","multOp factor %mul term1","EPSILON","","EPSILON","","","EPSILON","EPSILON","EPSILON","EPSILON","","EPSILON","EPSILON","EPSILON","multOp factor %mul term1","","","","","","","","","","","","EPSILON","","","","","pop"},
	/*factor*/			{"sign factor","( arithExpr )","pop","pop","pop","","pop","pop","","pop","","","sign factor","pop","pop","pop","","pop","pop","pop","pop","","","","fnum %factorF","","","%pushId id factor1","","","inum %factorI","not factor","pop","","","","","pop"},
	/*factor1*/			{"indiceList %pushDummy %checkId factor2","( %currentScope aParams %exitScope ) %checkFunc","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","","","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","indiceList %pushDummy %checkId factor2","","","","","","","","","","","","indiceList %pushDummy %checkId factor2","","","","","pop"},
	/*factor2*/			{"EPSILON %exitScope %currentScope","","EPSILON %exitScope %currentScope","EPSILON %exitScope %currentScope","EPSILON %exitScope %currentScope",". %pushId id factor1","EPSILON %exitScope %currentScope","EPSILON %exitScope %currentScope","","EPSILON %exitScope %currentScope","","","EPSILON %exitScope %currentScope","EPSILON %exitScope %currentScope","EPSILON %exitScope %currentScope","EPSILON %exitScope %currentScope","","EPSILON %exitScope %currentScope","EPSILON %exitScope %currentScope","EPSILON %exitScope %currentScope","EPSILON %exitScope %currentScope","","","","","","","","","","","","EPSILON %exitScope %currentScope","","","","","pop"},
	/*variable*/		{"","","pop","","","","","","","","","","","","","","pop","","","","","","","","","","","%pushId id indiceList %pushDummy %checkId variable1","","","","","","","","","","pop"},
	/*variable1*/		{"","","EPSILON %exitScope %currentScope","","",". %pushId id indiceList %checkId variable1","","","","","","","","","","","EPSILON %exitScope %currentScope","","","","","","","","","","","","","","","","","","","","","pop"},
	/*indiceList*/		{"EPSILON","","EPSILON","EPSILON","EPSILON","EPSILON","EPSILON","EPSILON","indice indiceList","EPSILON","","","EPSILON","EPSILON","EPSILON","EPSILON","EPSILON","EPSILON","EPSILON","EPSILON","EPSILON","","","","","","","","","","","","EPSILON","","","","","pop"},
	/*indice*/			{"pop","","pop","pop","pop","pop","pop","pop","[ arithExpr %addIndex ]","pop","","","pop","pop","pop","pop","pop","pop","pop","pop","pop","","","","","","","","","","","","pop","","","","","pop"},
	/*arraySize*/		{"","","pop","","pop","","","pop","[ #addDimension inum ]","","","","","","","","","","","","","","","","","","","","","","","","","","","","","pop"},
	/*type*/			{"","","","","","","","","","","","","","","","","","","","","","","","float","","","","id","","int","","","","","","","","pop"},
	/*fParams*/			{"","","EPSILON","","","","","","","","","","","","","","","","","","","","","#pushType type #pushId id arraySizeList #newVar #changeToParam fParamsTailList","","","","#pushType type #pushId id arraySizeList #newVar #changeToParam fParamsTailList","","#pushType type #pushId id arraySizeList #newVar #changeToParam fParamsTailList","","","","","","","","pop"},
	/*fParamsTailList*/	{"","","EPSILON","","fParamsTail fParamsTailList","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","pop"},
	/*aParams*/			{"expr %addParam aParamsTailList","expr %addParam aParamsTailList","EPSILON","","","","","","","","","","expr %addParam aParamsTailList","","","","","","","","","","","","expr %addParam aParamsTailList","","","expr %addParam aParamsTailList","","","expr %addParam aParamsTailList","expr %addParam aParamsTailList","","","","","","pop"},
	/*aParamsTailList*/	{"","","EPSILON","","aParamsTail aParamsTailList","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","pop"},
	/*fParamsTail*/		{"","","pop","",", #pushType type #pushId id arraySizeList #newVar #changeToParam","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","pop"},
	/*aParamsTail*/		{"","","pop","",", expr %addParam","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","pop"},
	/*assignOp*/		{"pop","pop","","","","","","","","","","","pop","","","","=","","","","","","","","pop","","","pop","","","pop","pop","","","","","","pop"},
	/*relOp*/			{"pop","pop","","","","","","","","","","","pop","<","<=","<>","","==",">",">=","","","","","pop","","","pop","","","pop","pop","","","","","","pop"},
	/*addOp*/			{"-","pop","","","","","","","","","","","+","","","","","","","","","","","","pop","","","pop","","","pop","pop","or","","","","","pop"},
	/*multOp*/			{"pop","pop","","*","","","/","","","","","","pop","","","","","","","","and","","","","pop","","","pop","","","pop","pop","","","","","","pop"}};
	
	SyntacticAnalyzer(String fileName, BufferedWriter output, BufferedWriter error, BufferedWriter code) throws IOException{
		la = new LexicalAnalyzer(fileName, error);
		sa = new SemanticActions(fileName, error);
		this.output = output;
		this.error = error;
		this.code = code;
	}
	
	Boolean parse() throws Exception{
		Boolean error = false;
		
		inverseRHSMultiplePush("$");output.newLine();
		inverseRHSMultiplePush("prog");output.newLine();
		
		Token a = la.nextToken();
		while(!stack.peek().equals("$")){
			String x = stack.peek();
			if(x.equals("EPSILON"))pop();
			else if(isTerminal(x)){
				if(matches(x,a)){
					output.append(pop() + " = " + a.lexeme);output.newLine();
					a = la.nextToken();
				}else{// if the top of the stack is a terminal but doesn't match the current token, should pop
					a = skipErrors(a, x, "pop");
					error = true;
				}
			}else if(x.startsWith("#")){//semantic actions
				sa.action(x, a);
				pop();
			}else if(x.startsWith("%")){//semantic checking, skip
				pop();
			}
			else{//non-terminal
				String rule = table(a.type, x);
				if(!rule.isEmpty()){
					output.append(pop() + " -> ");
					inverseRHSMultiplePush(rule);
					output.newLine();
					printStack();
				}else{// if the table returns an invalid rule
					a = skipErrors(a, x, null);
					error = true;
				}
			}
		}
		if(!a.type.equals(Token.Type.EOF) || error) return false;
		else{
			sa.print(sa.global);
			return true;
		}
	}
	Boolean check() throws Exception{
		Stack<String> cStack = new Stack<String>();
		Boolean error = false;
		
		cStack.push("$");
		cStack.push("prog");
		
		Token a = la.nextProcessedToken();
		while(!cStack.peek().equals("$")){
			String x = cStack.peek();
			if(x.equals("EPSILON"))cStack.pop();
			else if(isTerminal(x)){
				if(matches(x,a)){
					a = la.nextProcessedToken();
					cStack.pop();
				}else{// if the top of the stack is a terminal but doesn't match the current token, should pop
					cStack.pop();
					error = true;
				}
			}else if(x.startsWith("%")){//semantic checking
				sa.checking(x, a);
				cStack.pop();
			}else if(x.startsWith("#")){//semantic action, skip
				cStack.pop();
			}else{//non-terminal
				String rule = table(a.type, x);
				if(!rule.isEmpty()){
					cStack.pop();
					String[] rules = rule.split(" ");
					for(int i = rules.length -1; i > -1; i--){
						cStack.push(rules[i]);
					}
				}else{// if the table returns an invalid rule
					String func = table(a.type, x);
					if(func.equals("pop")){
						cStack.pop();
					}else{
						Token next = a;
						while(func.isEmpty()){// should always be empty
							next = la.nextProcessedToken();
							func = table(next.type, x);
						}
						a = next;
					}
					error = true;
				}
			}
		}
		if(!a.type.equals(Token.Type.EOF) || error) return false;
		else return true;
	}
	
	public void close() throws IOException{
		la.close();
		sa.close();
		output.close();
		error.close();
	}
	Token skipErrors(Token terminal, String nTerminal, String func) throws Exception{
		if(func == null)func = table(terminal.type, nTerminal);
		if(func.equals("pop")){//follow of non-terminal or eof
			error.append("SYNTAX ERROR: MISSING TOKENS AT LINE: " + terminal.line + ", index:" + terminal.index);
			error.newLine();
			pop();
			return terminal;
		}
		else{
			Token next = terminal;
			while(func.isEmpty()){// should always be empty
				error.append("SYNTAX ERROR: SKIPPED TOKEN " + next.lexeme + " at line: " + next.line + ", index:" + next.index);
				error.newLine();
				next = la.nextToken();
				func = table(next.type, nTerminal);
			}
			return next;
		}
	}
	void inverseRHSMultiplePush(String s) throws IOException{
		String[] rules = s.split(" ");
		for(int i = rules.length -1; i > -1; i--){
			stack.push(rules[i]);
			output.append(rules[i] + " ");
		}
	}
	String pop(){
		return stack.pop();
	}
	Boolean matches(String s,Token t){
		switch(t.type){
		case AND: 		return s.equals("and");
		case ASSIGN: 	return s.equals("=");
		case CBRACK: 	return s.equals("]");
		case CLASS:		return s.equals("class");
		case COMMA:		return s.equals(",");
		case CPAREN:	return s.equals(")");
		case CSQUIG:	return s.equals("}");
		case DOT:		return s.equals(".");
		case ELSE:		return s.equals("else");
		case EOF:		return s.equals("$");
		case EQ:		return s.equals("==");
		case FLOAT:		return s.equals("float");
		case FNUM:		return s.equals("fnum");
		case FOR:		return s.equals("for");
		case FSLASH:	return s.equals("/");
		case GET:		return s.equals("get");
		case ID:		return s.equals("id");
		case IF:		return s.equals("if");
		case INT:		return s.equals("int");
		case INUM:		return s.equals("inum");
		case LESSTHAN:	return s.equals("<");
		case LESSTHANEQ:return s.equals("<=");
		case MINUS:		return s.equals("-");
		case MORETHAN:	return s.equals(">");
		case MORETHANEQ:return s.equals(">=");
		case NEQ:		return s.equals("<>");
		case NOT:		return s.equals("not");
		case OBRACK:	return s.equals("[");
		case OPAREN:	return s.equals("(");
		case OR:		return s.equals("or");
		case OSQUIG:	return s.equals("{");
		case PLUS:		return s.equals("+");
		case PROGRAM:	return s.equals("program");
		case PUT:		return s.equals("put");
		case RETURN:	return s.equals("return");
		case SEMICOLON:	return s.equals(";");
		case STAR:		return s.equals("*");
		case THEN:		return s.equals("then");
		default:			return false;
		}
	}
	Boolean isTerminal(String s){
		switch(s){
		case "-":case "(":case ")":case "*":case ",":
		case ".":case "/":case ";":case "[":case "]":
		case "{":case "}":case "+":case "<":case "<=":
		case "<>":case "=":case "==":case ">":case ">=":
		case "and":case "class":case "else":case "float":
		case "fnum":case "for":case "get":case "id":
		case "if":case "int":case "inum":case "not":
		case "or":case "program":case "put":
		case "return":case "then":case "$":
			return true;
		default: return false;
		}
	}
	String table(Token.Type terminal, String nTerminal){//terminal is the current token, nTerminal is what is on the stack
		int x, y;
		switch(terminal){
		case MINUS: 	x = 0; break;
		case OPAREN: 	x = 1; break;
		case CPAREN: 	x = 2; break;
		case STAR: 		x = 3; break;
		case COMMA:		x = 4; break;
		case DOT:		x = 5; break;
		case FSLASH:	x = 6; break;
		case SEMICOLON:	x = 7; break;
		case OBRACK:	x = 8; break;
		case CBRACK:	x = 9; break;
		case OSQUIG:	x = 10; break;
		case CSQUIG:	x = 11; break;
		case PLUS:		x = 12; break;
		case LESSTHAN:	x = 13; break;
		case LESSTHANEQ:x = 14; break;
		case NEQ:		x = 15; break;
		case ASSIGN:	x = 16; break;
		case EQ:		x = 17; break;
		case MORETHAN:	x = 18; break;
		case MORETHANEQ:x = 19; break;
		case AND:		x = 20; break;
		case CLASS:		x = 21; break;
		case ELSE:		x = 22; break;
		case FLOAT:		x = 23; break;
		case FNUM:		x = 24; break;
		case FOR:		x = 25; break;
		case GET:		x = 26; break;
		case ID:		x = 27; break;
		case IF:		x = 28; break;
		case INT:		x = 29; break;
		case INUM:		x = 30; break;
		case NOT:		x = 31; break;
		case OR:		x = 32; break;
		case PROGRAM:	x = 33; break;
		case PUT:		x = 34; break;
		case RETURN:	x = 35; break;
		case THEN:		x = 36; break;
		case EOF:		x = 37; break;
		default:		x = -1; break;
		}
		switch(nTerminal){
		case "prog":			y = 0; break;
		case "classDeclList":	y = 1; break;
		case "classDecl":		y = 2; break;
		case "classBody":		y = 3; break;
		case "varOrFunc":		y = 4; break;
		case "funcDefList":		y = 5; break;
		case "progBody":		y = 6; break;
		case "funcHead":		y = 7; break;
		case "funcDef":			y = 8; break;
		case "funcBody":		y = 9; break;
		case "funcBlock":		y = 10; break;
		case "varOrStatement":	y = 11; break;
		case "statementList":	y = 12; break;
		case "arraySizeList":	y = 13; break;
		case "statement":		y = 14; break;
		case "assignStat":		y = 15; break;
		case "statBlock":		y = 16; break;
		case "expr":			y = 17; break;
		case "expr1":			y = 18; break;
		case "relExpr":			y = 19; break;
		case "arithExpr":		y = 20; break;
		case "arithExpr1":		y = 21; break;
		case "sign":			y = 22; break;
		case "term":			y = 23; break;
		case "term1":			y = 24; break;
		case "factor":			y = 25; break;
		case "factor1":			y = 26; break;
		case "factor2":			y = 27; break;
		case "variable":		y = 28; break;
		case "variable1":		y = 29; break;
		case "indiceList":		y = 30; break;
		case "indice":			y = 31; break;
		case "arraySize":		y = 32; break;
		case "type":			y = 33; break;
		case "fParams":			y = 34; break;
		case "fParamsTailList":	y = 35; break;
		case "aParams":			y = 36; break;
		case "aParamsTailList":	y = 37; break;
		case "fParamsTail":		y = 38; break;
		case "aParamsTail":		y = 39; break;
		case "assignOp":		y = 40; break;
		case "relOp":			y = 41; break;
		case "addOp":			y = 42; break;
		case "multOp":			y = 43; break;
		default:				y = -1; break;
		}
		return stateTransition[y][x];
	}
	void printStack() throws IOException{
		output.append("Stack: ");
		Object[] sStack = stack.toArray();
		for(int i = 0; i < sStack.length; i++){
			output.append(sStack[i] + " ");
		}
		output.newLine();
	}
}
