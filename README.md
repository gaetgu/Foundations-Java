# Foundations-Java

An implementation of the Foundations programming language, written in Java.
Written with the [craftinginterpreters](http://www.craftinginterpreters.com/contents.html) book.

# Learn the Language

In Foundations, every line ends with a semicolon (`;`).

## Data Types
There are a few different kinds of data types in Foundations. While Foundations *is* a dynamically typed programming language, 
it is still useful to know what you are using and how you can use it.

There are two boolean values, and a literal for each one.
```javascript
true;   // Not false
false;  // Not not false
```
As of writing, Foundations does not (and probably will not) have any fancy numbers like hex, octo, etc. For now all we have is 
basic integers and floats (known as decimal numbers).
```javascript
2006;   // An integer
20.06;  // A decimal num
```
Currently, only double quotes (`"`) are allowed to define strings. If need be, they will be added but most likely single quotes (`'`)
will be a feature saved for Foundations Pro.
```javascript
"Foundations is the best!"; // A normal string
"";                         // An empty string
"2006";                     // This is a string, *not* a number
```
And lastly, there is `nil`, which is the same as Ruby's `nil` or javascript's `null` or Python's `None`.
I chose the name `nil` mostly because it helped to differentiate between Foundations `nil` and Java's `null`.

## Expressions

#### Arithmetic

Foundations uses the same arithmetic operators used by virtually every language.
```javascript
1 + 2;              // addition
4 - 1;              // subtraction
1.5 * 2;            // multiplication
9 / 3;              // division
1.73205080757 ** 2; // exponentials
```

The `-` operator can also be used to negate a number.
```javascript
let myNumber = 2;
-myNumber;        // expect: -2
```

All of these operators only work on numbers, with the exception of the `+` operator, which can be used to concatenate strings.

#### Comparison and Equality

Foundations uses the same, tried and true comparison operator as just about every other language.
```
less < than;
greater > than;
less <= orEqual;
greater >= orEqual;
```
You can test any values for equality or inequality
```javascript
20 == 06;       // false
"20" != "06";   // true
3.14 == "pi";   // false
2006 == "2006"; // false
```

#### Logical Operators

The `!` returns false if its operand is true, and vice versa.
```javascript
!true;  // false.
!false; // true.
```
`and` and `or` act like you'd expect.
```javascript
true and false; // false
true and true;  // true
false or false; // false
true or false;  // true
```

#### Precedence and Grouping

If you don't like the way that an expression is running or you want to run some math, use parenthesis to cause something to be calculated
before other things.
```javascript
1 + 3 * 3;    // 10
(1 + 3) * 3;  // 9
```

## Statements


#### Output
To print, you use the `print` keyword. You can have parenthesis around what it prints, or you can leave it out (think python2).
For example, these are both perfectly valid print statements:
```python
print "Hello, world!";
print("Hello, world!");
```

If you would like to add a bunch of statements where one is expected (for some reason, idk), wrap it in braces.
```javascript
{
  print "Hello, world!";
  print("Hello, world!");
}
```
#### Variables
Variables are an important part of every language. To define a variable in Foundations, use the `let` keyword.
```javascript
let variable_name = "variable value";
```

## Control Flow

#### If/Else
This is the one thing that something needs to truly be a language (in my opinion) - control flow.
```javascript
if (condition) {
  print "yes";
} else {
  print "no";
}
```
Unfortunatly, Foundations does not yet support `else if`, but for the time being you can use a hacked solution like this (or figure out your own):
```javascript
if (condition) {
  print "yes";
} else { 
  if (condition2) {
    print "else if!";
  } else {
    print "no";
  }
}
```

#### While
While loops are also extremely important.
```javascript
var i = 1;
while (i < 10) {
  print i;
  i = i + 1;
}
```

#### For
For loops look alot like c/c++/javascript for loops.
```javascript
for (let i = 1; i < 10; i = i + 1) {
  print i;
}
```

## Methods

NOTE: In most languages, they are called `functions`. However, Foundations uses `methods`. The main difference is the name.

#### Creating
Functions are created by writing `method` with the function name and its params after it.
```javascript
method doSomething(something) {
  print(something + "!!!");
  
  return something;
}
```

#### Calling
A function call looks the same as it does in c/c++/javascript
```javascript
doSomething(important);
```
Unlike languages like Ruby, calling a function without parenthesis is not allowed. If you leave them off, it doesn’t call 
the function, it just refers to it.

If the program reaches the end of the block without reaching `return`, it returns `nil`.













