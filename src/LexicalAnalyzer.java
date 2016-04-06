import java.io.*;

public class LexicalAnalyzer {

	BufferedInputStream input;
	BufferedWriter error;
	
	int[][] stateTransition = {	{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},//error row so that all row indices start at 1
						/*1*/	{ 1, 1, 2, 4, 5,-3,12,16,22,26,27,28,29,30,31,32,38,39,40,41,42,43,-4,-1, 0},
						/*2*/	{ 3, 3, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,-1, 0},
						/*3*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},//yes(ID) == 0
						/*4*/	{10,10,10, 4, 4,10,10,10,10,10,10, 6,10,10,10,10,10,10,10,10,10,10,10,-1, 0},
						/*5*/	{10,10,10,10,10,10,10,10,10,10,10, 6,10,10,10,10,10,10,10,10,10,10,10,-1, 0},
						/*6*/	{-1,-1,-1, 7, 8,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, 0},
						/*7*/	{11,11,11, 7, 9,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,-1, 0},
						/*8*/	{11,11,11, 7, 9,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,-1, 0},
						/*9*/	{-2,-2,11, 7, 9,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-1, 0},
						/*10*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},//yes(INUM) == 1
						/*11*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1},//yes(FNUM) == 2
						/*12*/	{14,14,14,14,14,14,13,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,-1, 0},
						/*13*/	{15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,-1, 0},
						/*14*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,16, 1},//yes(ASSIGN) == 16
						/*15*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1},//yes(EQ) == 3
						/*16*/	{19,19,19,19,19,19,17,19,18,19,19,19,19,19,19,19,19,19,19,19,19,19,19,-1, 0},
						/*17*/	{20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,-1, 0},
						/*18*/	{21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,-1, 0},
						/*19*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 1},//yes(LESSTHAN) == 5
						/*20*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 7, 1},//yes(LESSTHANEQ) == 7
						/*21*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1},//yes(NEQ) == 4
						/*22*/	{24,24,24,24,24,24,23,24,24,24,24,24,24,24,24,24,24,24,24,24,24,24,24,-1, 0},
						/*23*/	{25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,-1, 0},
						/*24*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 6, 1},//yes(MORETHAN) == 6
						/*25*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1},//yes(MORETHANEQ) == 8
						/*26*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9, 0},//yes(SEMICOLON) == 9
						/*27*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,10, 0},//yes(COMMA) == 10
						/*28*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,11, 0},//yes(DOT) == 11
						/*29*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,12, 0},//yes(PLUS) == 12
						/*30*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,13, 0},//yes(MINUS) == 13
						/*31*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,14, 0},//yes(STAR) == 14
						/*32*/	{36,36,36,36,36,36,36,36,36,36,36,36,36,36,33,35,36,36,36,36,36,36,36,-1, 0},
						/*33*/	{33,33,33,33,33,33,33,33,33,33,33,33,33,33,34,33,33,33,33,33,33,33,33,-1, 0},
						/*34*/	{33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,37,33,33,33,33,33,33,33,-1, 0},
						/*35*/	{35,37,35,35,35,35,35,35,35,35,35,35,35,35,35,35,35,35,35,35,35,35,35,-1, 0},
						/*36*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,15, 1},//yes(FSLASH) == 15
						/*37*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,26, 0},//yes(CMT) == 26
						/*38*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,20, 0},//yes(OPAREN) == 20
						/*39*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,21, 0},//yes(CPAREN) == 21
						/*40*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,22, 0},//yes(OSQUIG) == 22
						/*41*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,23, 0},//yes(CSQUIG) == 23
						/*42*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,24, 0},//yes(OBRACK) == 24
						/*43*/	{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,25, 0}};//yes(CBRACK) == 25
	
	int index = 0;
	int line = 1;
	
	
	LexicalAnalyzer(String fileName, BufferedWriter error) throws IOException{
		File in = new File(fileName);
		input = new BufferedInputStream(new FileInputStream(in));
		
		this.error = error;
	}
	
	LexicalAnalyzer(File file, BufferedWriter error) throws IOException{
		input = new BufferedInputStream(new FileInputStream(file));
		
		this.error = error;
	}
	
	void close() throws IOException{
		input.close();
	}
	Boolean hasNext() throws IOException{
		Boolean hasNext = true;
		input.mark(1);
		if(input.read() == -1) hasNext = false;
		input.reset();
		return hasNext;
	}
	
	Token nextToken() throws Exception{
		if(hasNext()){
			int initialIndex = index;
			int state = 1;
			StringBuffer lexeme = new StringBuffer();
			Token token = null;
			
			do{
				input.mark(1);
				char next = (char)input.read();
				if(next != -1){									// if not end of file
					
					index++;									// increment index
					
					int col = findType(next);					// find type of next character
					if(col == 1) line++;						// increment line number
					
					lexeme.append(next);						//
					if(state == 1 && (col == 0 || col == 1)){	// append character to lexeme
						lexeme.deleteCharAt(lexeme.length() - 1);// remove whitespace
						initialIndex++;							//
					}											//
					
					state = stateTransition[state][col];		// get next state
					
					if(state < 0){								// if is error
						handleError(state, next);				// handle error
						lexeme.setLength(0);					// empty string buffer
						state = 1;								// return to initial state
						initialIndex++;							// push the initial index forward by 1
					}
					else if(stateTransition[state][23] > -1){	// if is final
												
						if(stateTransition[state][24] == 1){	// if backtrack is required
							input.reset();						// bring back input stream index
							index--;							// bring back index
							lexeme.deleteCharAt(lexeme.length()-1);//remove last added character
						}
						
						Token.Type type = Token.Type.fromInt(stateTransition[state][23]);
						
						String lexemeS = lexeme.toString();
						
						if(type == Token.Type.ID){
							type = isReservedWord(lexemeS);
						}
						token = new Token(type, lexemeS, initialIndex, line);//create token
					}
				}
			}while(token == null);
			if(token.type == Token.Type.CMT) return nextToken();
			else return token;
		}else{
			Token token = new Token(Token.Type.EOF, "$", index, line);
			return token;
		}
		
	}
	
	int findType(char c) throws Exception{
		if(c == ' ' || c == '\r' || c== '\t')						return 0;
		else if(c == '\n')											return 1;
		else if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))	return 2;
		else if(c >= '1' && c <= '9')								return 3;
		else if(c == '0')											return 4;
		else if(c == '_')											return 5; 
		else if(c == '=')											return 6;
		else if(c == '<')											return 7;
		else if(c == '>')											return 8;
		else if(c == ';')											return 9;
		else if(c == ',')											return 10;
		else if(c == '.')											return 11;
		else if(c == '+')											return 12;
		else if(c == '-')											return 13;
		else if(c == '*')											return 14;
		else if(c == '/')											return 15;
		else if(c == '(')											return 16;
		else if(c == ')')											return 17;
		else if(c == '{')											return 18;
		else if(c == '}')											return 19;
		else if(c == '[')											return 20;
		else if(c == ']')											return 21;
		else 														return 22;
	}
	
	Token.Type isReservedWord(String lexeme){
		switch(lexeme){
		case "and":
			return Token.Type.AND;
		case "not":
			return Token.Type.NOT;
		case "or":
			return Token.Type.OR;
		case "if":
			return Token.Type.IF;
		case "then":
			return Token.Type.THEN;
		case "else":
			return Token.Type.ELSE;
		case "for":
			return Token.Type.FOR;
		case "class":
			return Token.Type.CLASS;
		case "int":
			return Token.Type.INT;
		case "float":
			return Token.Type.FLOAT;
		case "get":
			return Token.Type.GET;
		case "put":
			return Token.Type.PUT;
		case "return":
			return Token.Type.RETURN;
		case"program":
			return Token.Type.PROGRAM;
		default:
			return Token.Type.ID;
		}
	}
	
	void handleError(int e, char c) throws IOException{
		switch(e){
		case -1:
			error.append("LEXICAL ERROR: MUST END FLOAT WITH NUMBER, REMOVE CHARACTER " + c + " at line: " + line);
			error.newLine();
			input.reset();
			break;
		case -2:
			error.append("LEXICAL ERROR: CANNOT END FLOAT WITH TRAILING ZEROS at line: " + line);
			error.newLine();
			input.reset();
			break;
		case -3:
			error.append("LEXICAL ERROR: CANNOT START IDENTIFIER WITH UNDERSCORE at line: " + line);
			error.newLine();
			break;
		case -4:
			error.append("LEXICAL ERROR: INVALID CHARACTER " + "\'" + c + "\'" + " at line: " + line);
			error.newLine();
			break;
		}
	}
}
