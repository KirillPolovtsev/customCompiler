class ScopeContext {
    private static String currentClassName = "";
    private static String currentMethodName = "";
    
    public static void setClassName(String className) {
        currentClassName = className;
    }
    
    public static void setMethodName(String methodName) {
        currentMethodName = methodName;
    }
    
    public static void clearMethodName() {
        currentMethodName = "";
    }
    
    public static String getCurrentScope() {
        if (currentMethodName.isEmpty()) {
            return "class<" + currentClassName + ">";
        } else {
            return "class<" + currentClassName + ">:" + currentMethodName;
        }
    }
    
    public static String getClassName() {
        return currentClassName;
    }
    
    public static String getMethodName() {
        return currentMethodName;
    }
}
