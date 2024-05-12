import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CLIApp implements Observer {
    private final INumberleModel model;
    private final NumberleController controller;
    private final Scanner scanner;
    private List<String> trueStringList;
    private List<String> poStringList;
    private List<String> errStringList;
    private boolean flag = false;
    public static void main(String[] args) {
        new CLIApp();
    }
    public CLIApp() {
        model = new NumberleModel();
        ((NumberleModel) this.model).addObserver(this);
        controller = new NumberleController(model);
        scanner = new Scanner(System.in);
        trueStringList = new ArrayList<>();
        poStringList = new ArrayList<>();
        errStringList = new ArrayList<>();
        startNewGame();
    }

    public void startNewGame() {
        // Start game
        controller.startNewGame();
        //Game loop
        Scanner scanner = new Scanner(System.in);
        while (!controller.isGameOver()) {
            this.removeDuplicates();
            System.out.print( "\n");
            System.out.print("Attempts remaining: "+ (controller.getRemainingAttempts()) + "\n");
            System.out.print("Completely correct are: " + trueStringList.toString() + "\n");
            System.out.print("Incorrect location: " + poStringList.toString() + "\n");
            System.out.print("Wrong: " + errStringList.toString() + "\n");
            System.out.print("Enter your guess: ");
            String guess = scanner.nextLine();
            controller.processInput(guess);
            if (flag) {
                System.out.println();
                flag = false;
            }
        }
        // Game end
        handleGameOver();
    }

    // Use streams to de-duplicate and assign values to the corresponding lists
    public void removeDuplicates() {
        trueStringList = trueStringList.stream().distinct().collect(Collectors.toList());
        poStringList = poStringList.stream().distinct().collect(Collectors.toList());
        errStringList = errStringList.stream().distinct().collect(Collectors.toList());
    }

    private void handleGameOver() {
        if (controller.isGameWon()) {
            System.out.println("Congratulations! You won!");
        } else {
            System.out.println("Sorry, you lost. The answer was: " + model.getTargetNumber());
        }
        boolean validInput = false;
        while (!validInput) {
            System.out.print("Play again. (yes/no): ");
            String answer = scanner.nextLine().toLowerCase();
            if ("yes".equals(answer)) {
                validInput = true;
                new CLIApp();
            } else if ("no".equals(answer)) {
                validInput = true;
                System.out.println("The game is over. Goodbye!");
                scanner.close();
            } else {
                System.out.println("Please input 'yes' or 'no'!");
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof IllegalArgumentException) {
            IllegalArgumentException exception = (IllegalArgumentException) arg;
            flag = true;
            System.out.println(exception.getMessage());
        } else if (arg instanceof HashMap) {
            HashMap<String, String> notification = (HashMap<String, String>) arg;
            if (notification.containsKey("true")) {
                // Handling of characters in the correct position
                String character = notification.get("true");
                trueStringList.add(character);


            } else if (notification.containsKey("po")) {

                String character = notification.get("po");
                poStringList.add(character);


            } else if (notification.containsKey("false")) {
                String character = notification.get("false");
                errStringList.add(character);


            }

        }
    }
}

