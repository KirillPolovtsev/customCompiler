class PrintStmt extends NonWhileStmt implements TI {
	PrintList printList;
  public PrintStmt(PrintList p)
  {
    printList = p;
  }

  public String typeCheck() throws LangException {
  	printList.typeCheck();
  	return "";
  }

  public String toString(int t)
  {
  	return(T(t) + "print(" + printList.toString(t) + ");\n");
  }
}

