package com.gabrielgutierrez.fnds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
//import java.util.Scanner;
import java.util.List;

import static com.gabrielgutierrez.fnds.Scanner.*;
import static com.gabrielgutierrez.fnds.TokenType.*;

public class Foundations {

    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /*
     * Takes in a filename as an argument, reads the contents of the file, and passes the result to the run() function
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error
        if (hadError) System.exit(65);
    }

    /*
     * Creates a new interactive prompt, reads the line of input, and runs that line
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.println(">_ ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);

            // If the user makes a mistake, it shouldn’t kill their entire session
            hadError = false;
        }
    }

    /*
     * The run function - what everything else has been pointing to
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    /*
     * An extremely important part of the program - error catching/handling
     */
    static void error(int line, String message) {
        report(line, "", message);
    }
    private static void report(int line, String where, String message) {
        System.err.println("[line" + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
