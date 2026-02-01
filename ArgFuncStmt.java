class ArgFuncStmt extends FuncStmt implements TI {
	Args arguments;
  public ArgFuncStmt(String i, Args a)
  {
    super(i);
    arguments = a;
  }

  public String typeCheck() throws LangException {
  	arguments.typeCheck();
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
  	return(T(t) + id + "(" + arguments.toString(t) + ");\n");
  }
}

