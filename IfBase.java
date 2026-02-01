class IfBase extends Token implements TI {
  Expr expression;
  public IfBase(Expr e)
  {
    expression = e;
  }

  public String typeCheck() throws LangException {
  	String exprType = expression.typeCheck();
  	
  	// Condition must be bool or coercible to bool
  	if (!exprType.equals("bool") && !exprType.equals("int")) {
  		throw new LangException("Error: If condition must be bool or int, got " + exprType);
  	}
  	
  	return "bool";
  }

  public String toString(int t)
  {
  	return ("if (" + expression.toString(t) + ")\n");
  }
}

