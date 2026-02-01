class BinaryExpr extends NonTypeCastExpr implements TI {
	Expr leftHandSide;
	String operator;
	Expr rightHandSide;

  public BinaryExpr(Expr l, String b, Expr r)
  {
    leftHandSide = l;
    operator = b;
    rightHandSide = r;
  }

  public String typeCheck() throws LangException {
  	String leftType = leftHandSide.typeCheck();
  	String rightType = rightHandSide.typeCheck();
  	
  	return getResultType(leftType, rightType, operator);
  }
  
  private String getResultType(String left, String right, String op) throws LangException {
  	if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/")) {
  		if (op.equals("+")) {
  			if (left.equals("string") || right.equals("string")) {
  				if (!left.contains("[]") && !right.contains("[]")) {
  					return "string";
  				}
  				throw new LangException("Error: Cannot concatenate arrays with strings");
  			}
  		}
  		if (left.equals("int") && right.equals("int")) return "int";
  		if ((left.equals("int") && right.equals("float")) || (left.equals("float") && right.equals("int"))) return "float";
  		if (left.equals("float") && right.equals("float")) return "float";
  		throw new LangException("Error: Invalid types for " + op + " operator: " + left + " and " + right);
  	}
  	
  	if (op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=") || op.equals("==") || op.equals("<>")) {
  		if ((left.equals("int") || left.equals("float")) && (right.equals("int") || right.equals("float"))) {
  			return "bool";
  		}
  		throw new LangException("Error: Invalid types for comparison " + op + ": " + left + " and " + right);
  	}
  	
  	if (op.equals("||") || op.equals("&&")) {
  		if (isBoolCompatible(left) && isBoolCompatible(right)) {
  			return "bool";
  		}
  		throw new LangException("Error: Invalid types for logical " + op + ": " + left + " and " + right);
  	}
  	
  	return "";
  }
  
  private boolean isBoolCompatible(String type) {
  	return type.equals("bool") || type.equals("int");
  }

  public String toString(int t)
  {
  	return ("(" + leftHandSide.toString(t) + " " + operator + " " + rightHandSide.toString(t) + ")");
  }
}

