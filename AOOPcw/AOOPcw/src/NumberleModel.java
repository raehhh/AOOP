import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
public class NumberleModel extends Observable implements INumberleModel {
    private String targetNumber;
    private StringBuilder currentGuess;
    private int remainingAttempts;
    private boolean gameWon;

    @Override
    public void initialize() {
        Random rand = new Random();
        List<String> read = read("equations.txt");
        int range = rand.nextInt(read.size());
        targetNumber = read.get(rand.nextInt(range));
        System.out.println(targetNumber);
        currentGuess = new StringBuilder("       ");
        remainingAttempts = MAX_ATTEMPTS;
        gameWon = false;
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean processInput(String input) {
        try {
            check(input);
        } catch (IllegalArgumentException e) {
            setChanged();
            notifyObservers(e);
            return false;
        }

        boolean[] matched = new boolean[input.length()]; // Used to mark whether each character entered has been matched
        // Check that each character is in the correct position or in the target number
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == targetNumber.charAt(i)) {
                matched[i] = true;
                HashMap<String, String> map = new HashMap<>();
                map.put("true", String.valueOf(c));
                map.put("index", i + "");
                setChanged();
                notifyObservers(map);
            }
            if (!matched[i] && targetNumber.indexOf(c) != -1) {
                HashMap<String, String> map = new HashMap<>();
                map.put("po", String.valueOf(c));
                map.put("index", i + "");
                setChanged();
                notifyObservers(map);
            }
            if (targetNumber.indexOf(c) == -1) {
                // If the character does not exist in the target number, it is processed accordingly
                HashMap<String, String> map = new HashMap<>();
                map.put("false", String.valueOf(c));
                map.put("index", i + "");
                setChanged();
                notifyObservers(map);
            }
        }
        if (input.equals(targetNumber)) {
            gameWon = true;
        }

        remainingAttempts--;
        setChanged();
        notifyObservers();

        return true;
    }

    public boolean check(String input) throws IllegalArgumentException {
        if (input.length() < 7) {
            throw new IllegalArgumentException("Please complete the equation!");
        }
        if (!input.contains("=")) {
            throw new IllegalArgumentException("lack '=' sign");
        }
        if (
            // Check whether it starts or ends with an operator
                "+-/*".indexOf(input.charAt(0)) != -1 ||
                        "+-/*".indexOf(input.charAt(input.length() - 1)) != -1 ||
                        // Use regular expressions to check if there are two or more consecutive operators
                        input.matches(".*[+\\-/*]{2,}.*")
        ) {
            throw new IllegalArgumentException("Invalid expression! Cannot start or end with an operator, or have multiple consecutive operators.");
        }
        if (!judge(input)) {
            throw new IllegalArgumentException("The left side is not equal to the right!");
        }
        return true;
    }


    public static boolean judge(String equation) {

        // Follow the "number + operator + number... = Number + operator + number..." The pattern splitting formula
        String[] parts = equation.split("=");

        // Extract and calculate the results on the left and right
        int leftResult = getValue(parts[0]);
        int rightResult =  getValue(parts[1]);
        boolean isEquationValid = leftResult == rightResult;
        return isEquationValid;
    }

    public static int getValue(String str) {
        return value(str.toCharArray(), 0)[0];
    }
    public static int[] value(char[] str, int i) {
        LinkedList<String> que = new LinkedList<String>();
        int pre = 0;
        int[] bra = null;
        while (i < str.length && str[i] != ')') {
            if (str[i] >= '0' && str[i] <= '9') {
                pre = pre * 10 + str[i++] - '0';
            } else if (str[i] != '(') {
                addNum(que, pre);
                que.addLast(String.valueOf(str[i++]));
                pre = 0;
            } else {
                bra = value(str, i + 1);
                pre = bra[0];
                i = bra[1] + 1;
            }
        }
        addNum(que, pre);
        return new int[]{getNum(que), i};
    }

    public static void addNum(LinkedList<String> que, int num) {
        if (!que.isEmpty()) {
            int cur = 0;
            String top = que.pollLast();
            if (top.equals("+") || top.equals("-")) {
                que.addLast(top);
            } else {
                cur = Integer.parseInt(que.pollLast());
                num = top.equals("*") ? (cur * num) : (cur / num);
            }
        }
        que.addLast(String.valueOf(num));
    }

    public static int getNum(LinkedList<String> que) {
        int res = 0;
        boolean add = true;
        String cur = null;
        int num = 0;
        while (!que.isEmpty()) {
            cur = que.pollFirst();
            if (cur.equals("+")) {
                add = true;
            } else if (cur.equals("-")) {
                add = false;
            } else {
                num = Integer.parseInt(cur);
                res += add ? num : -num;
            }
        }
        return res;
    }


    @Override
    public boolean isGameOver() {

        return remainingAttempts <= 0 || gameWon;
    }

    @Override
    public boolean isGameWon() {

        return gameWon;
    }

    @Override
    public String getTargetNumber() {

        return targetNumber;
    }

    @Override
    public StringBuilder getCurrentGuess() {

        return currentGuess;
    }

    @Override
    public int getRemainingAttempts() {

        return remainingAttempts;
    }

    @Override
    public void startNewGame() {

        initialize();
    }

    public static List<String> read(String fileName) {
        Path filePath = Paths.get(fileName);
        if (Files.exists(filePath)) {
            List<String> words = new ArrayList<>();
            try (BufferedReader br = Files.newBufferedReader(filePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    words.add(line);
                }
                return words;
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                return null;
            }
        } else {
            System.err.println("File does not exist: " + fileName);
            return null;
        }
    }
}
