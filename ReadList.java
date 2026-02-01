class ReadList extends Token implements TI {
	Name name;
  ReadList readList;
  public ReadList(Name n)
  {
    name = n;
    readList = null;
  }

  public ReadList(Name n, ReadList r)
  {
    name = n;
    readList = r;
  }

  public String typeCheck() throws LangException {
  	name.typeCheck();
  	if(readList != null) {
  		return readList.typeCheck();
  	}
  	return "";
  }

  public String toString(int t)
  {
  	return(name.toString(t) + (readList != null ? ", " + readList.toString(t) : ""));
  }
}
