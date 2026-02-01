class ReadStmt extends NonWhileStmt implements TI {
	ReadList readList;
  public ReadStmt(ReadList r)
  {
    readList = r;
  }

  public String typeCheck() throws LangException {
  	readList.typeCheck();
  	return "";
  }

  public String toString(int t)
  {
  	return(T(t) + "read(" + readList.toString(t) + ");\n");
  }
}

