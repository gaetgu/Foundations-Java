# Foundations-Java

An implementation of the Foundations programming language, written in Java.
Inspired by the [craftinginterpreters](http://www.craftinginterpreters.com/contents.html) book.

### Example Program
```javascript
// A simple program in Foundations

// variables...
let hello = "Hello, Foundations!";

// print statements...
print(hello);

// methods (functions)...
method print_user(name) {
    print("USERNAME: " + name);
}

print_user("Gabriel Gutierrez");

// classes (!!!)...
class Programming {
    type() {
        print("clickity-clackety");
    }
    
    compile(language) {
        print("Compiling " + language + " program...");
    }
}

// Closures
fun addPair(a, b) {
  return a + b;
}

fun identity(a) {
  return a;
}

print identity(addPair)(1, 2); // Prints "3".
```
