abstract class InOut extends Token {
  abstract String getType();
  abstract String typeCheck() throws LangException;
}

class ConcreteInOut extends InOut {
  private String type;
  
  public ConcreteInOut(String type) {
    this.type = type;
  }
  
  public String getType() {
    return type;
  }
  
  public String typeCheck() throws LangException {
    return type;
  }
  
  public String toString(int t) {
    return type;
  }
}
