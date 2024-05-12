import org.junit.Before;
import static org.junit.Assert.*;
public class Test {
    private NumberleModel numberleModel;
    private NumberleController numberleController;

    @Before
    public void setUp() {
        numberleModel = new NumberleModel();
        numberleController = new NumberleController(numberleModel);
        numberleModel.initialize();
    }

    @org.junit.Test
    public void testGameWin() {
        String targetNumber = numberleModel.getTargetNumber();
        assertTrue("Target number is not null or empty", targetNumber != null && !targetNumber.isEmpty());

        // Provide a correct answer
        numberleController.processInput(targetNumber);

        assertTrue("Game is over", numberleController.isGameOver());
        assertTrue("Game is won", numberleController.isGameWon());
    }

    @org.junit.Test
    public void testGameLose() {
        String targetNumber = numberleModel.getTargetNumber();
        assertTrue( "Target number is not null or empty",targetNumber != null && !targetNumber.isEmpty());

        // Provide a wrong answer
        String wrongAnswer = "1+1+1=3"; // Let's say this is the wrong answer
        for(int i=0;i<7;i++) {
            numberleController.processInput(wrongAnswer);
        }
        assertTrue( "Game is over",numberleController.isGameOver());
        assertFalse( "Game is lose",numberleController.isGameWon());
    }
    @org.junit.Test
    public void testInputNoEqualSign() {
        // NO "="
        assertFalse(numberleController.processInput("1234567"));
        assertEquals(numberleModel.getRemainingAttempts(), NumberleModel.MAX_ATTEMPTS);
    }
}
