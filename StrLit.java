class StrLit extends TypeLit implements TI {
	String string;
  public StrLit(String s)
  {
    string  = s;
  }

  public String typeCheck() throws LangException {
  	return "string";
  }

  public String toString(int t)
  {
  	return string;
  }
}
