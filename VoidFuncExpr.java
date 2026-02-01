class VoidFuncExpr extends FuncExpr implements TI {
  public VoidFuncExpr(String i)
  {
    super(i);
  }

  public String typeCheck() throws LangException {
  	try {
  		String funcType = symbolTable.get(id);
  		if (!funcType.startsWith(":")) {
  			throw new LangException("Error: " + id + " is not a function");
  		}
  		return "void";
  	} catch (LangException e) {
  		throw new LangException("Error: function " + id + " called but not declared");
  	}
  }

  public String toString(int t)
  {
  	return(id + "()");
  }
}

