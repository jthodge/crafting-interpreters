package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
  // We use the hadError flag to ensure we don't execute code with a known error.
  // hadError also allows us to exit with a non-zero code
  static boolean hadError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      // Using exit codes defined in the sysexits.h header:
      // https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  // We're supporting two ways to execute code directly from source:
  // 1. Start jlox from the CLI and give it a file path. jlox reads the file and
  // executes it.
  // 2. Start jlox from the CLI w/o any arguments. jlox opens a REPL for
  // interactivity.

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));

    // Surface an error in the exit code.
    if (hadError)
      System.exit(65);
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null)
        break;
      run(line);

      // Reset hadError flag so that we don't kill the entire session if a user makes
      // a mistake.
      hadError = false;
    }
  }

  // The file runner and prompt are each thin wrappers for run.
  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    // Just print the tokens for now.
    for (Token token : tokens) {
      System.out.println(token);
    }
  }

  // Error Handling
  // We're pulling error reporting out of the individual interpreter phases, and
  // placing
  // it into the general framework to separate code the _generates_ errors from
  // the code that _reports_ errors.

  // Each phase of the front end will be detecting errors, and its not each
  // phase's
  // concern to know how to present those errors to the user.
  // Especially because in a more evolved language implementation there will be
  // multiple output targets to display errors (e.g. stderr, IDE error windows, log
  // files).
  // We don't want that error reporting code smeared across phases of our front
  // end.

  // Ideally this will be abstracted out to a more general ErrorReporter interface
  // later.
  // That way we can pass the interface to the scanner and parser to swap out
  // different,
  // more appropriate reporting strategies.
  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
}