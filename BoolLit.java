class BoolLit extends TypeLit implements TI {
	String bool;
  public BoolLit(String b)
  {
    bool = b;
  }

  public String typeCheck() throws LangException {
  	return "bool";
  }

  public String toString(int t)
  {
  	return("" + bool);
  }
}
