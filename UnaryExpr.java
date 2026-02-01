class UnaryExpr extends NonTypeCastExpr implements TI {
	String operator;
  Expr expression;
  public UnaryExpr(String o, Expr e)
  {
    operator = o;
    expression = e;
  }

  public String typeCheck() throws LangException {
  	String exprType = expression.typeCheck();
  	
  	if (operator.equals("+") || operator.equals("-")) {
  		if (exprType.equals("int") || exprType.equals("float")) {
  			return exprType;
  		}
  		throw new LangException("Error: Unary " + operator + " requires int or float, got " + exprType);
  	}
  	
  	if (operator.equals("~")) {
  		if (exprType.equals("bool") || exprType.equals("int")) {
  			return "bool";
  		}
  		throw new LangException("Error: Unary ~ requires bool or int, got " + exprType);
  	}
  	
  	return exprType;
  }

  public String toString(int t)
  {
  	return("(" + operator + expression.toString(t) + ")");
  }
}

