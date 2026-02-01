class ArgDeclArray extends ArgDecl implements TI {
  
  public ArgDeclArray(Type t, String i)
  {
    super(t, i);
  }

  public String typeCheck() throws LangException {
  	String arrayType = type.toString(0) + "[]";
  	symbolTable.addVar(id, arrayType);
  	return arrayType;
  }

  public String toString(int t)
  {
  	return (super.toString(t) + "[]");
  }
}

