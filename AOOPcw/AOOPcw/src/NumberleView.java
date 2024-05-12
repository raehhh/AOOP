import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
public class NumberleView extends JFrame implements Observer {
    private final INumberleModel model;
    private final NumberleController controller;
    private final JTextField inputTextField = new JTextField(7);
    private final JLabel attemptsLabel = new JLabel("Attempts remaining: ");
    private JPanel mainPanel;
    private JButton[][] gridButtons;
    private JButton[] numberButtons;
    private JButton[] operatorButtons;
    String regex = "^(10|[0-9])$";
    String operatorRegex = "[+\\-*/=]";

    private Integer rowIndex = 0;

    public NumberleView(INumberleModel model, NumberleController controller) {
        this.controller = controller;
        this.model = model;
        this.controller.startNewGame();
        ((NumberleModel) this.model).addObserver(this);
        initializeFrame();
        this.controller.setView(this);
        update((NumberleModel) this.model, null);
    }

    public void initializeFrame() {
        setTitle("Numberle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 450);

        mainPanel = new JPanel(new GridLayout(6, 7));
        gridButtons = new JButton[6][7];

        // Create grid buttons
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                gridButtons[i][j] = new JButton("");
                gridButtons[i][j].setPreferredSize(new Dimension(50, 50));
                gridButtons[i][j].setBackground(new Color(238 ,224 ,229));
                gridButtons[i][j].setEnabled(false);

                Font boldFont = new Font("Transport", Font.BOLD, 20);
                gridButtons[i][j].setFont(boldFont);
                gridButtons[i][j].setForeground(Color.BLACK);
                mainPanel.add(gridButtons[i][j]);
            }
        }
        //numeric keypad
        JPanel numberPanel = new JPanel(new GridLayout(1, 10));
        numberButtons = new JButton[10];
        for (int i = 0; i < 10; i++) {
            numberButtons[i] = new JButton(String.valueOf(i));
            numberButtons[i].setPreferredSize(new Dimension(55, 55));
            numberButtons[i].addActionListener(new OperatorButtonListener());
            numberPanel.add(numberButtons[i]);
        }
        //Arithmetic keyboard
        JPanel operatorPanel = new JPanel(new GridLayout(1, 8));
        String[] operators = {"Del", "+", "-", "*", "/", "=", "New", "Enter"};
        operatorButtons = new JButton[8];
        for (int i = 0; i < 8; i++) {
            operatorButtons[i] = new JButton(operators[i]);
            operatorButtons[i].setPreferredSize(new Dimension(60, 60));
            operatorButtons[i].addActionListener(new OperatorButtonListener());
            operatorPanel.add(operatorButtons[i]);
        }
        add(mainPanel, BorderLayout.NORTH);
        add(numberPanel, BorderLayout.CENTER);
        add(operatorPanel, BorderLayout.SOUTH);
        setVisible(true);

    }

    class OperatorButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String operator = ((JButton) e.getSource()).getText();
            if (operator.matches(regex) || operator.matches(operatorRegex)) {
                inputTextField.setText(inputTextField.getText() + operator);
                if (inputTextField.getText().length() > 7) {
                    inputTextField.setText(inputTextField.getText().substring(0, inputTextField.getText().length() - 1));
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Please enter the correct equation");
                    return;
                }
                //Gets the current display location
                int index = inputTextField.getText().length() - 1;
                JButton jButton = gridButtons[rowIndex][index];
                jButton.setText(String.valueOf(inputTextField.getText().charAt(index)));
                jButton.setBackground(new Color(216, 191, 216));

            } else if (operator.equals("Del")) {
                if (!inputTextField.getText().isEmpty()) {
                    int index = inputTextField.getText().length() - 1;
                    JButton jButton = gridButtons[rowIndex][index];
                    jButton.setText("");
                    inputTextField.setText(inputTextField.getText().substring(0, inputTextField.getText().length() - 1));
                }
            } else if (operator.equals("Enter")) {
                controller.processInput(inputTextField.getText());
                if (!controller.isGameOver()) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Attempts remaining: " + (controller.getRemainingAttempts()));
                }
            } else if (operator.equals("New")) {
                dispose();
                INumberleModel model = new NumberleModel();
                NumberleController controller = new NumberleController(model);
                new NumberleView(model, controller);
            }
        }
    }

    @Override
    public void update(java.util.Observable o, Object arg) {
        attemptsLabel.setText("Attempts remaining: " + controller.getRemainingAttempts());
        if (arg instanceof IllegalArgumentException) {
            IllegalArgumentException exception = (IllegalArgumentException) arg;
            JOptionPane.showMessageDialog(this, exception.getMessage());
            return;
        } else if (arg instanceof HashMap) {
            HashMap<String, String> notification = (HashMap<String, String>) arg;
            Integer index = Integer.parseInt(notification.get("index"));
            if (notification.containsKey("true")) {
                // Handles cases where characters are in the correct position
                String character = notification.get("true");
                changeColor(character, Color.GREEN, index);
                // Handle accordingly
            } else if (notification.containsKey("po")) {
                // Handles situations where characters are present in the target number but are incorrectly positioned
                String character = notification.get("po");
                changeColor(character, Color.yellow, index);
            } else if (notification.containsKey("false")) {
                // Handles cases where the character does not exist in the target number
                String character = notification.get("false");
                changeColor(character, Color.GRAY, index);

            }
            if (index == 6) {
                this.rowIndex++;
                this.inputTextField.setText(null);
            }
        }
        if (controller.isGameOver()) {
            disable();
            if (controller.isGameWon()) {
                int option = JOptionPane.showConfirmDialog(this, "Congratulations! You won! " + controller.getTargetWord() + "\nDo you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    this.restart();
                } else {
                    System.out.println("End of game, Exiting the game.");
                }
            } else {
                int option = JOptionPane.showConfirmDialog(this, "Sorry, you lose. The answer was " + controller.getTargetWord() + "\nDo you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    this.restart();
                } else {
                    System.out.println("End of game, Exiting the game.");
                }
            }
        }
    }

    public void changeColor(String character, Color bg, Integer index) {

        JButton jButton = gridButtons[rowIndex][index];
        jButton.setBackground(bg);

        if (character.matches(regex)) {
            JButton numberButton = numberButtons[Integer.parseInt(character)];
            numberButton.setBackground(bg);

        } else {
            if (character.equals("+")) {
                JButton operatorButton = operatorButtons[1];
                operatorButton.setBackground(bg);

            } else if (character.equals("-")) {
                JButton operatorButton = operatorButtons[2];
                operatorButton.setBackground(bg);


            } else if (character.equals("*")) {
                JButton operatorButton = operatorButtons[3];
                operatorButton.setBackground(bg);


            } else if (character.equals("/")) {
                JButton operatorButton = operatorButtons[4];
                operatorButton.setBackground(bg);


            } else if (character.equals("=")) {
                JButton operatorButton = operatorButtons[5];
                operatorButton.setBackground(bg);


            }

        }
    }

    public void restart() {
        dispose();
        INumberleModel model = new NumberleModel();
        NumberleController controller = new NumberleController(model);
        new NumberleView(model, controller);
    }

    public void disable(){
        for(int i = 0 ;i<numberButtons.length;i++){
            numberButtons[i].setEnabled(false);
        }
        for(int i = 0 ;i<operatorButtons.length;i++){
            operatorButtons[i].setEnabled(false);
        }
    }
}
