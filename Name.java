class Name extends Expr implements TI {
  String id;
  public Name(String i)
  {
    id = i;
  }

  public String typeCheck() throws LangException {
  	return symbolTable.get(id);
  }

  public String toString(int t)
  {
  	return(id);
  }
}
