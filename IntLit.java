class IntLit extends TypeLit implements TI {
	int integer;
  public IntLit(int i)
  {
    integer = i;
  }

  public String typeCheck() throws LangException {
  	return "int";
  }

  public String toString(int t)
  {
  	return Integer.toString(integer);
  }
}
