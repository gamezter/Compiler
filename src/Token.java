
public class Token {
	
	static enum Type {
		ID, INUM, FNUM, EQ, NEQ, LESSTHAN, MORETHAN, LESSTHANEQ, MORETHANEQ, SEMICOLON, COMMA, DOT,
		PLUS, MINUS, STAR, FSLASH, ASSIGN, AND, NOT, OR, OPAREN, CPAREN, OSQUIG, CSQUIG,
		OBRACK, CBRACK, CMT, IF, THEN, ELSE, FOR, CLASS, INT, FLOAT, GET, PUT, RETURN, PROGRAM, EOF, ERROR;
		
		public static Type fromInt(int x){
			switch(x){
				case 0: return ID;
				case 1: return INUM;
				case 2: return FNUM;
				case 3: return EQ;
				case 4: return NEQ;
				case 5: return LESSTHAN;
				case 6: return MORETHAN;
				case 7: return LESSTHANEQ;
				case 8: return MORETHANEQ;
				case 9: return SEMICOLON;
				case 10: return COMMA;
				case 11: return DOT;
				case 12: return PLUS;
				case 13: return MINUS;
				case 14: return STAR;
				case 15: return FSLASH;
				case 16: return ASSIGN;
				case 17: return AND;
				case 18: return NOT;
				case 19: return OR;
				case 20: return OPAREN;
				case 21: return CPAREN;
				case 22: return OSQUIG;
				case 23: return CSQUIG;
				case 24: return OBRACK;
				case 25: return CBRACK;
				case 26: return CMT;
				case 27: return IF;
				case 28: return THEN;
				case 29: return ELSE;
				case 30: return FOR;
				case 31: return CLASS;
				case 32: return INT;
				case 33: return FLOAT;
				case 34: return GET;
				case 35: return PUT;
				case 36: return RETURN;
				case 37: return PROGRAM;
				case 38: return EOF;
				default: return ERROR;
			}
		}
	};
	
	Type type;
	String lexeme;
	int index;
	int line;
	
	Token(Type type, String lexeme, int index, int line){
		this.type = type;
		this.lexeme = lexeme;
		this.index = index;
		this.line = line;
	}

}
