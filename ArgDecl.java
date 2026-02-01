abstract class ArgDecl extends Token implements TI {
	Type type;
  String id;
  public ArgDecl(Type t, String i)
  {
    type = t;
    id = i;
  }

  public String typeCheck() throws LangException {
  	symbolTable.addVar(id, type.toString(0));
  	return type.toString(0);
  }

  public String toString(int t)
  {
  	return( type.toString(t) + " " + id);
  }
}

