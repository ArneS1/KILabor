import javafx.application.Application;
import javafx.stage.Stage;
import ki.CoolAI;
import ki.cathedral.*;

public class Main extends Application {
    CoolAI aiOne;
    CoolAI aiTwo;
    Boolean buttonsCreated = false;

    @Override
    public void start(Stage primaryStage) throws Exception {

        aiOne = new CoolAI();
        aiTwo = new CoolAI();
        GameController gameController = new GameController();
        FxController fxController = new FxController(aiOne, aiTwo, primaryStage, gameController);
        fxController.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
