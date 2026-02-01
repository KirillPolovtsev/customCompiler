class VoidFuncStmt extends FuncStmt implements TI {
  public VoidFuncStmt(String i)
  {
    super(i);
  }

  public String typeCheck() throws LangException {
  	try {
  		String funcType = symbolTable.get(id);
  		if (!funcType.startsWith(":")) {
  			throw new LangException("Error: " + id + " is not a function");
  		}
  	} catch (LangException e) {
  		throw new LangException("Error: function " + id + " called but not declared");
  	}
  	return "";
  }

  public String toString(int t)
  {
  	return(T(t) + id + "();\n");
  }
}

