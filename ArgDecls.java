class ArgDecls extends Token implements TI {
	ArgDecl argumentDeclaration;
	ArgDecls argumentDeclarations;
  public ArgDecls(ArgDecl a, ArgDecls s)
  {
  	argumentDeclaration = a;
  	argumentDeclarations = s;
  }

    public ArgDecls(ArgDecl a)
  {
    argumentDeclaration = a;
    argumentDeclarations = null;
  }


  public String typeCheck() throws LangException {
  	argumentDeclaration.typeCheck();
  	if(argumentDeclarations != null) {
  		return argumentDeclarations.typeCheck();
  	}
  	return "";
  }

  public String toString(int t)
  {
  	return(argumentDeclaration.toString(t) + (argumentDeclarations != null ? ", " + 
      argumentDeclarations.toString(t) : "") );
  }
}

