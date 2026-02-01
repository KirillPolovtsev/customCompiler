import java.util.List;
import java.util.LinkedList;

class InOutList extends Token {
  private List<InOut> params;

  public InOutList() {
    params = new LinkedList<InOut>();
  }

  public InOutList prepend(InOut i) {
    params.add(0, i);
    return this;
  }

  public InOutList join(InOutList i) {
    params.addAll(i.params);
    return this;
  }

  public String toString(int t) {
    String ret = "";
    for (InOut s : params) {
      ret += s.toString(0) + " ";
    }
    return ret;
  }

  public String typeCheck() throws LangException {
    for (InOut i : params) {
      String a = i.typeCheck();

    }
    return "";
  }

  public String getType() {
    String fullType = ":";
    String returnType = "void";
    
    // If we have params, the first one is the return type
    if (!params.isEmpty()) {
      returnType = params.get(0).getType();
      // Add parameter types (skip first which is return type)
      for (int i = 1; i < params.size(); i++) {
        fullType = fullType + params.get(i).getType() + ":";
      }
    }
    
    // Add return type at the end
    fullType = fullType + returnType;
    return fullType;
  }
}
