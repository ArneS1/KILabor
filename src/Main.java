import javafx.application.Application;
import javafx.stage.Stage;
import ki.Thomas;

public class Main extends Application {
    Thomas aiOne;
    Thomas aiTwo;
    Boolean buttonsCreated = false;

    @Override
    public void start(Stage primaryStage) throws Exception {

        aiOne = new Thomas();
        aiTwo = new Thomas();
        GameController gameController = new GameController();
        FxController fxController = new FxController(aiOne, aiTwo, primaryStage, gameController);
        fxController.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
