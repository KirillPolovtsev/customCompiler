class ArgFuncExpr extends FuncExpr implements TI {
	Args arguments;
  public ArgFuncExpr(String i, Args a)
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
		int lastColon = funcType.lastIndexOf(":");
		if (lastColon >= 0 && lastColon < funcType.length() - 1) {
			String returnType = funcType.substring(lastColon + 1);
			return returnType;
		}
		if (funcType.length() > 1) {
			return funcType.substring(1);
		}
		return "void";
  	} catch (LangException e) {
  		throw new LangException("Error: function " + id + " called but not declared");
  	}
  }

  public String toString(int t)
  {
  	return(id + "(" + arguments.toString(t) + ")");
  }
}

