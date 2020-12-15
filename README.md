# Foundations-Java

An implementation of the Foundations programming language, written in Java.
Inspired by the [craftinginterpreters](http://www.craftinginterpreters.com/contents.html) book.

### Examples
```javascript
// variables...
let hello = "Hello, Foundations!";

// print statements...
print(hello);
print "You don't need parenthesis!";

// Control flow
let go = true;
let no_go = false;
if (go and !no_go) {
    print("Mission is go!");
} 
// no elif yet
if (!go) {
    print("Mission is no go!");
}

let a = 1;
while (a < 10) {
  print(a);
  a = a + 1;
}

for (let a = 1; a < 10; a = a + 1) {
  print a;
}

// methods (functions)...
method print_user(name) {
    print("USERNAME: " + name);
}
print_user("Gabriel Gutierrez");

// Closures
method addPair(a, b) {
  return a + b;
}
method identity(a) {
  return a;
}
print identity(addPair)(1, 2); // Prints "3".

// classes (!!!)...
class Programming {
    
    init(operating_system, age) {
        this.os = operating_system;
        this.age = age;
    }
    
    type() {
        print("clickity-clackety");
    }
    
    compile(language) {
        print("Compiling " + language + " program on target " + this.os + "...");
    }
}

// Inheritance
class Java < Programming {
    init(operating_system, age, ide) {
        super.init(operating_system, age);
        this.ide = ide;
    }
    
    print_ide() {
        print("You use the " + this.ide + " IDE!");
    }
}

let program = Java("macOS", "16", "IntelliJ"); 

program.type();
program.compile("Java");
program.print_ide();
```
