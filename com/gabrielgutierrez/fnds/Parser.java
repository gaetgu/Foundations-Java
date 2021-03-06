package com.gabrielgutierrez.fnds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gabrielgutierrez.fnds.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    private int loopLevel = 0;

    private boolean allowExpression;
    private boolean foundExpression = false;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Object parseRepl() {
        allowExpression = true;
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            List<Stmt> stmts = declarations();
            if(stmts != null)
                statements.addAll(stmts);

            if (foundExpression) {
                Stmt last = statements.get(statements.size() - 1);
                return ((Stmt.Expression) last).expression;
            }

            allowExpression = false;
        }

        return statements;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            List<Stmt> declarations = declarations();
            if (declarations != null)
                statements.addAll(declarations);
        }

        return statements;
    }

    private List<Stmt> declarations() {
        List<Stmt> lst = new ArrayList<>();
        try {
            if (match(CLASS)) lst.add(classDeclaration());
            else if (check(METHOD) && checkNext(IDENTIFIER)) {
                consume(METHOD, null);
                lst.add(function("function"));
            }
            else if (match(LET)) lst.addAll(letDeclarations());
            else lst.add(statement());

            return lst;
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");

        Expr superclass = null;
        if (match(LESS)) {
            consume(IDENTIFIER, "Expect superclass name.");
            superclass = new Expr.Variable(previous());
        }

        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt.Function> classMethods = new ArrayList<>();
        consume(LEFT_BRACE, "Expect '{' before class body.");

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            boolean isClassMethod = match(CLASS);
            (isClassMethod ? classMethods : methods).add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return new Stmt.Class(name, superclass, methods, classMethods);
    }

    private List<Stmt> letDeclarations() {
        List<Stmt> lst = new ArrayList<>();
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = assignment();
        }
        lst.add(new Stmt.Var(name, initializer));
        while (match(COMMA)) {
            name = consume(IDENTIFIER, "Expect variable name.");
            consume(EQUAL, "Expect assignment in multiple variable declaration.");
            initializer = assignment();
            lst.add(new Stmt.Var(name, initializer));
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return lst;
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        return new Stmt.Function(name, functionBody(kind));
    }

    private Expr.Function functionBody(String kind) {
        List<Token> parameters = null;

        // Allow omitting the parameter list entirely in method getters.
        if (!kind.equals("method") || check(LEFT_PAREN)) {
            consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
            parameters = new ArrayList<>();
            if (!check(RIGHT_PAREN)) {
                do {
                    if (parameters.size() >= 8) {
                        error(peek(), "Cannot have more than 8 parameters.");
                    }

                    parameters.add(consume(IDENTIFIER, "Expect parameter name."));
                } while (match(COMMA));
            }
            consume(RIGHT_PAREN, "Expect ')' after parameters.");
        }

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Expr.Function(parameters, body);
    }

    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(DO)) return doWhileStatement();
        if (match(WHILE)) return whileStatement();
        if (match(BREAK)) return breakStatement();
        if (match(CONTINUE)) return continueStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        if (allowExpression && isAtEnd()) {
            foundExpression = true;
        } else {
            consume(SEMICOLON, "Expect ';' after expression.");
        }
        return new Stmt.Expression(expr);
    }

    //using desugaring technique
    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        List<Stmt> initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(LET)) {
            initializer = letDeclarations();
        } else {
            initializer = new ArrayList<>();
            initializer.add(expressionStatement());
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        try {
            loopLevel++;
            Stmt body = statement();

            if (increment != null) {
                body = new Stmt.Block(Arrays.asList(
                        body,
                        new Stmt.Expression(increment)));
            }

            if (condition == null) condition = new Expr.Literal(true);
            body = new Stmt.While(condition, body);

            if (initializer != null) {
                List<Stmt> stmts = new ArrayList<>();
                stmts.addAll(initializer);
                stmts.add(body);
                body = new Stmt.Block(stmts);
            }

            return body;
        } finally {
            loopLevel--;
        }
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt doWhileStatement() {
        try {
            loopLevel++;
            Stmt body = statement();
            consume(WHILE, "Expect 'while' in a do-while loop.");
            consume(LEFT_PAREN, "Expect '(' after 'while'.");
            Expr condition = expression();
            consume(RIGHT_PAREN, "Expect ')' after condition.");
            consume(SEMICOLON, "Expect ';' after do-while statement.");

            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.While(condition, body)));

            return body;
        } finally {
            loopLevel--;
        }
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");

        try {
            loopLevel++;
            Stmt body = statement();

            return new Stmt.While(condition, body);
        } finally {
            loopLevel--;
        }
    }

    private Stmt breakStatement() {
        if (!(loopLevel > 0)) throw error(previous(), "Break statement must be inside a loop.");
        consume(SEMICOLON, "Expect ';' after 'break' statement.");
        return new Stmt.Break();
    }

    private Stmt continueStatement() {
        if (!(loopLevel > 0)) throw error(previous(), "Continue statement must be inside a loop.");
        consume(SEMICOLON, "Expect ';' after 'continue' statement.");
        return new Stmt.Continue();
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            List<Stmt> declarations = declarations();
            if (declarations != null) {
                statements.addAll(declarations);
            }
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    // --exprs:

    private Expr expression() {
        return comma();
    }

    private Expr comma() {
        Expr expr = assignment();

        while (match(COMMA)) {
            Token comma = previous();
            Expr right = assignment();
            expr = new Expr.Binary(expr, comma, right);
        }

        return expr;
    }

    private Expr assignment() {
        Expr expr = ternary();

        if (match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, STAR_STAR_EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value, equals);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr ternary() {
        Expr expr = or();

        if (match(QUESTION_MARK)) {
            Expr thenBranch = expression();
            consume(COLON, "Expect ':' after then branch of ternary expression.");
            Expr elseBranch = ternary();
            expr = new Expr.Ternary(expr, thenBranch, elseBranch);
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right, false);
        }

        return exponent();
    }

    private Expr exponent() {
        Expr expr = prefix();

        if (match(STAR_STAR)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr prefix() {
        if (match(PLUS_PLUS, MINUS_MINUS)) {
            Token operator = previous();
            Expr right = primary();
            return new Expr.Unary(operator, right, false);
        }

        return postfix();
    }

    private Expr postfix() {
        if (matchNext(PLUS_PLUS, MINUS_MINUS)) {
            Token operator = peek();
            current--;
            Expr left = primary();
            advance();
            return new Expr.Unary(operator, left, true);
        }

        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER,
                        "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 8) {
                    error(peek(), "Cannot have more than 8 arguments.");
                }
                arguments.add(assignment());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(NONE)) return new Expr.Literal(null);
        if (match(THIS)) return new Expr.This(previous());

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        // Lambda
        if (check(METHOD) && !checkNext(IDENTIFIER)) {
            advance();
            return functionBody("function");
        }

        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expect '.' after 'super'.");
            Token method = consume(IDENTIFIER,
                    "Expect superclass method name.");
            return new Expr.Super(keyword, method);
        }

        // Error productions.
        if (match(QUESTION_MARK)) {
            throw error(previous(), "Missing left-hand condition of ternary operator.");
        }
        if (match(BANG_EQUAL, EQUAL_EQUAL)) {
            throw error(previous(), "Missing left-hand operand.");
        }
        if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            throw error(previous(), "Missing left-hand operand.");
        }
        if (match(PLUS)) {
            throw error(previous(), "Missing left-hand operand.");
        }
        if (match(SLASH, STAR, STAR_STAR)) {
            throw error(previous(), "Missing left-hand operand.");
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean matchNext(TokenType... types) {
        for (TokenType type : types) {
            if (checkNext(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType tokenType) {
        if (isAtEnd()) return false;
        return peek().type == tokenType;
    }

    private boolean checkNext(TokenType tokenType) {
        if (isAtEnd()) return false;
        if (tokens.get(current + 1).type == EOF) return false;
        return tokens.get(current + 1).type == tokenType;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Foundations.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case METHOD:
                case LET:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                case BREAK:
                case CONTINUE:
                    return;
            }

            advance();
        }
    }
}
