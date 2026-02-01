class FieldDecls extends Token implements TI {
	FieldDecl fieldDeclaration;
	FieldDecls fieldDeclarations;

  public FieldDecls(FieldDecl f, FieldDecls s)
  {
  	fieldDeclaration = f;
  	fieldDeclarations = s;
  }

  public FieldDecls(FieldDecl f)
  {
    fieldDeclaration = f;
    fieldDeclarations = null;
  }

  public String typeCheck() throws LangException {
  	fieldDeclaration.typeCheck();
  	if(fieldDeclarations != null) {
  		return fieldDeclarations.typeCheck();
  	}
  	return "";
  }

  public String toString(int t)
  {
  	return(fieldDeclaration.toString(t) + (fieldDeclarations != null ? fieldDeclarations.toString(t) : "") );
  }
}

