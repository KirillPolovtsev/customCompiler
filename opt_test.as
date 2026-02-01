class OptimizationTest {

int globalVar = 10;

void main()
{
    int a = 5;
    int b = 3;
    int c;
    int d;
    int e;
    
    c = 10 + 20;
    
    d = a * 1;
    
    e = b + 0;
    
    if (true) {
        print(c);
    }
    
    if (false) {
        print(999);
    }
    
    c = 2 * 3 + 4;
    
    print(c, d, e);
    printline();
}

int add(int x, int y)
{
    return x + y;
}

int factorial(int n)
{
    int result = 1;
    while (n > 1) {
        result = result * n;
        n--;
    }
    return result;
}

}
