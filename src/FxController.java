import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ki.Thomas;
import ki.cathedral.*;

import java.util.ArrayList;
import java.util.List;

public class FxController {
    //Scene
    private Text scenetitle;
    private Text score;
    private GridPane gridPane;
    private Stage stage;

    //Buttons
    private Button button_aiOne;
    private Button button_aiTwo;
    private Button buttonReset;
    private Button buttonUndoLastTurn;

    //Game Logic
    private List<Button> buildingButtons;
    private Boolean isHumanTurn;
    private GameController gameController;
    private Thomas aiOne;
    private Thomas aiTwo;


    FxController(
            Thomas aiOne,
            Thomas aiTwo,
            Stage stage,
            GameController gameController
    ){
        this.stage = stage;
        this.gameController = gameController;
        this.aiOne = aiOne;
        this.aiTwo = aiTwo;
        isHumanTurn = false;
        buildingButtons = new ArrayList<>();
    }

    public void start(){
        gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        scenetitle = new Text("Drücke einen Knopf um das spiel zu starten");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        gridPane.add(scenetitle, 0, 0, 2, 1);

        score = new Text("Score:");
        score.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
        gridPane.add(score, 0, 13, 2, 1);

        button_aiOne = new Button("AI GREEN");
        button_aiTwo = new Button("AI BLACK");
        button_aiTwo.setDisable(true);

        button_aiOne.setOnMouseClicked(mouseEvent -> {
            aiButtonOnClick(aiOne, button_aiOne);
        });

        button_aiTwo.setOnMouseClicked(mouseEvent -> {
            aiButtonOnClick(aiTwo, button_aiTwo);
        });

        gridPane.add(button_aiOne, 0, 1);
        gridPane.add(button_aiTwo, 1, 1);

        buttonReset = new Button("New Game");
        buttonReset.setOnMouseClicked(mouseEvent -> {
            gameController.resetGame();
            aiOne = new Thomas();
            aiTwo = new Thomas();
            button_aiOne.setText("AI GREEN");
            button_aiOne.setDisable(false);
            button_aiTwo.setText("AI BLACK");
            button_aiTwo.setDisable(true);
            updateSceneTitle(gameController.getGame());
            updateUI(gameController.getGame());
        });
        gridPane.add(buttonReset, 0, 15);

        buttonUndoLastTurn = new Button("UNDO");
        buttonUndoLastTurn.setOnMouseClicked(mouseEvent -> {
            if(gameController.getGame().getTurnsSize() > 0){
                gameController.getGame().undoLastTurn();
                switchAIButtons();
                updateUI(gameController.getGame());
            }
        });
        gridPane.add(buttonUndoLastTurn, 0, 2);

        updateUI(gameController.getGame());

        stage.setTitle("Hello Eike");
        stage.setScene(new Scene(gridPane, 1000, 700));
        stage.show();
    }

    private void aiButtonOnClick(Thomas ai, Button buttonMe) {
        if (ai.isDone) {
            buttonMe.setText("Done");
            switchAIButtons();
        } else {
            isHumanTurn = false;
            gameController.aiTurn(ai);
            updateUI(gameController.getGame());
            switchAIButtons();
        }
    }

    private void updateUI(Game game) {

        updateSceneTitle(game);

        renderBoard(game.lastTurn().getBoard().getBoardAsColorArray());

        if (!isHumanTurn) {
            createBuildingButtons();
        }

        score.setText(getCurrentScore(game));
        stage.show();
    }

    private void updateSceneTitle(Game game){
        if (game.getCurrentPlayer() == Color.Black) {
            scenetitle.setText("Schwarz ist dran!");
        } else {
            scenetitle.setText("Grün ist dran!");
        }
    }

    private void renderBoard(Color[][] board){
        for (int y = 0; y < 10; ++y) {
            for (int x = 0; x < 10; ++x) {
                gridPane.add(
                        getBoardFieldRect(x, y, board[x][y]),
                        x + 2,
                        y + 2);
            }
        }
    }


    private String getCurrentScore(Game game) {
        String rScore;
        rScore = "Score: \n";
        rScore += "Spieler Schwarz: ";
        rScore += game.score().get(Color.Black);
        rScore += "\n";
        rScore += "Spieler Grün: ";
        rScore += game.score().get(Color.White);
        rScore += "\n";
        if(game.getTurnsSize() > 1){
            rScore += "Last Move: \n";
            rScore += game.lastTurn().getAction().getBuilding().getName() +"\n";
            rScore += "At X = " + game.lastTurn().getAction().x() + " and Y = " + game.lastTurn().getAction().y() +"\n";
            if(!isHumanTurn){
                try{
                    rScore += "rated: " + aiOne.rateLastTurn(game);
                } catch (Exception e){
                    System.out.println("could not rate last Move because: \n" + e);
                }
            }
        }
        return rScore;
    }

    private Rectangle getBoardFieldRect(int x, int y, Color color) {
        Rectangle rect = new Rectangle();
        rect.setWidth(30);
        rect.setHeight(30);
        switch (color) {
            case None:
                rect.setFill(javafx.scene.paint.Color.LIGHTPINK);
                break;
            case Black:
                rect.setFill(javafx.scene.paint.Color.BLACK);
                break;
            case Black_Owned:
                rect.setFill(javafx.scene.paint.Color.DARKGRAY);
                break;
            case White:
                rect.setFill(javafx.scene.paint.Color.GREEN);
                break;
            case White_Owned:
                rect.setFill(javafx.scene.paint.Color.LIGHTGREEN);
                break;
            case Blue:
                rect.setFill(javafx.scene.paint.Color.BLUE);
                break;
        }
        return rect;
    }

    private void createBuildingButtons() {

        int x = 12;
        int y = 1;
        Game game = gameController.getGame();
        for (Button btn : buildingButtons
        ) {
            gridPane.getChildren().remove(btn);
        }

        buildingButtons.clear();

        for (Building building : game.getPlacableBuildings()) {
            if (building.getColor() == game.getCurrentPlayer()) {
                Button btn = new Button(building.getName());
                btn.setOnMouseClicked(mouseEvent -> {
                    for (Button butn : buildingButtons) {
                        gridPane.getChildren().remove(butn);
                    }
                    isHumanTurn = true;
                    humanPlayerTurn(game.getCurrentPlayer(), building);
                });
                btn.setMinWidth(50);
                gridPane.add(btn, x, y);
                y++;
                buildingButtons.add(btn);
            }
        }
    }

    private void humanPlayerTurn(Color player, Building building) {

        final int[] x = {0};
        final int[] y = {0};
        final Direction[] direction = {Direction._0};

        Button xpBtn = new Button("X++");
        Text xText = new Text("X: " + x[0]);
        Button xnBtn = new Button("X--");

        Button ypBtn = new Button("Y++");
        Text yText = new Text("Y: " + y[0]);
        Button ynBtn = new Button("Y--");

        Button rotateBtn = new Button("rotate");
        Button confirmBtn = new Button("confirm");
        Button backBtn = new Button("chose other");

        xpBtn.setOnMouseClicked(mouseEvent -> {
            x[0]++;
            testPlace(new Placement(x[0], y[0], direction[0], building));
            xText.setText("X: " + x[0]);
        });
        xnBtn.setOnMouseClicked(mouseEvent -> {
            x[0]--;
            testPlace(new Placement(x[0], y[0], direction[0], building));
            xText.setText("X: " + x[0]);
        });
        ypBtn.setOnMouseClicked(mouseEvent -> {
            y[0]++;
            testPlace(new Placement(x[0], y[0], direction[0], building));
            yText.setText("Y: " + y[0]);
        });
        ynBtn.setOnMouseClicked(mouseEvent -> {
            y[0]--;
            testPlace(new Placement(x[0], y[0], direction[0], building));
            yText.setText("Y: " + y[0]);
        });
        rotateBtn.setOnMouseClicked(mouseEvent -> {
            switch (direction[0]) {
                case _0:
                    direction[0] = Direction._90;
                    break;
                case _90:
                    direction[0] = Direction._180;
                    break;
                case _180:
                    direction[0] = Direction._270;
                    break;
                case _270:
                    direction[0] = Direction._0;
                    break;
            }
            testPlace(new Placement(x[0], y[0], direction[0], building));
        });

        confirmBtn.setOnMouseClicked(mouseEvent -> {
            gameController.getGame().takeTurn(new Placement(x[0], y[0], direction[0], building));
            gridPane.getChildren().remove(xpBtn);
            gridPane.getChildren().remove(xText);
            gridPane.getChildren().remove(xnBtn);
            gridPane.getChildren().remove(ypBtn);
            gridPane.getChildren().remove(yText);
            gridPane.getChildren().remove(ynBtn);
            gridPane.getChildren().remove(rotateBtn);
            gridPane.getChildren().remove(confirmBtn);
            gridPane.getChildren().remove(backBtn);
            isHumanTurn = false;
            switchAIButtons();
            updateUI(gameController.getGame());
            System.out.println("Züge: " + gameController.getGame().getTurnsSize());
        });

        backBtn.setOnMouseClicked(mouseEvent -> {
            gridPane.getChildren().remove(xpBtn);
            gridPane.getChildren().remove(xText);
            gridPane.getChildren().remove(xnBtn);
            gridPane.getChildren().remove(ypBtn);
            gridPane.getChildren().remove(yText);
            gridPane.getChildren().remove(ynBtn);
            gridPane.getChildren().remove(rotateBtn);
            gridPane.getChildren().remove(confirmBtn);
            gridPane.getChildren().remove(backBtn);
            isHumanTurn = false;
            updateUI(gameController.getGame());
        });

        gridPane.add(xpBtn, 15, 1);
        gridPane.add(xText, 14, 1);
        gridPane.add(xnBtn, 13, 1);

        gridPane.add(ypBtn, 15, 3);
        gridPane.add(yText, 14, 3);
        gridPane.add(ynBtn, 13, 3);

        gridPane.add(rotateBtn, 13, 5);
        gridPane.add(confirmBtn, 13, 7);
        gridPane.add(backBtn, 13, 9);
    }

    private void switchAIButtons(){
        if(button_aiOne.isDisabled()){
            button_aiOne.setDisable(false);
            button_aiTwo.setDisable(true);
        } else {
            button_aiOne.setDisable(true);
            button_aiTwo.setDisable(false);
        }
    }

    private void testPlace(Placement placement) {
        Game testGame = gameController.getGame().copy();
        testGame.takeTurn(placement);
        updateUI(testGame);
    }
}
