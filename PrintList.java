class PrintList extends Token implements TI {
	Expr expression;
  PrintList printList;
  public PrintList(Expr e)
  {
    expression = e;
    printList = null;
  }

  public PrintList(Expr e, PrintList p)
  {
    expression = e;
    printList = p;
  }

  public String typeCheck() throws LangException {
  	expression.typeCheck();
  	if(printList != null) {
  		return printList.typeCheck();
  	}
  	return "";
  }

  public String toString(int t)
  {
  	return(expression.toString(t) + (printList != null ? ", " + printList.toString(t) : ""));
  }
}
