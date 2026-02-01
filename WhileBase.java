class WhileBase extends Token implements TI {
  Expr expression;
  public WhileBase(Expr e)
  {
    expression = e;
  }

  public String typeCheck() throws LangException {
  	String exprType = expression.typeCheck();
  	
  	// Condition must be bool or coercible to bool
  	if (!exprType.equals("bool") && !exprType.equals("int")) {
  		throw new LangException("Error: While condition must be bool or int, got " + exprType);
  	}
  	
  	return "bool";
  }

  public String toString(int t)
  {
  	return(T(t) + "while(" + expression.toString(t) + ")\n");
  }
}

