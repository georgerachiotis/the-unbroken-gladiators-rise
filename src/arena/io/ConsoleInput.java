package arena.io;

import java.util.Scanner;

/**
 * Small wrapper around Scanner that keeps menu input safe and consistent.
 */
public class ConsoleInput {

    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine() {
        return scanner.nextLine();
    }

    public int readMenuChoice() {
        while (!scanner.hasNextInt()) {
            scanner.nextLine();
            System.out.print("Enter a number: ");
        }

        int choice = scanner.nextInt();
        scanner.nextLine();
        return choice;
    }
}
