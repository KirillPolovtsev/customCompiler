class NameArray extends Name implements TI {
	Expr expression;
  public NameArray(String i, Expr e)
  {
    super(i);
    expression = e;
  }

  public String typeCheck() throws LangException {
  	String indexType = expression.typeCheck();
  	String arrayType = symbolTable.get(id);
  	
  	// Index must be int
  	if (!indexType.equals("int")) {
  		throw new LangException("Error: Array index must be int, got " + indexType);
  	}
  	
  	// Array type should end with [] - remove it to get element type
  	if (arrayType.endsWith("[]")) {
  		return arrayType.substring(0, arrayType.length() - 2);
  	}
  	
  	throw new LangException("Error: Cannot index non-array type " + arrayType);
  }

  public String toString(int t)
  {
  	return(id + "[" + expression.toString(t) + "]");
  }
}
