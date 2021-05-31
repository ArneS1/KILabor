import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ki.CoolAI;
import ki.cathedral.*;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application implements Runnable {

    private Game game;
    private Text scenetitle;
    private Text score;
    private Text possibleTurns;
    private List<Button> buildingButtons = new ArrayList<>();
    CoolAI ai_one;
    CoolAI ai_two;
    Boolean buttonsCreated = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        scenetitle = new Text("Drücke einen Knopf um das spiel zu starten");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        gridPane.add(scenetitle, 0, 0, 2, 1);

        score = new Text("Score:");
        score.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
        gridPane.add(score, 0, 13, 2, 1);

        game = new Game();
        ai_one = new CoolAI();
        ai_two = new CoolAI();

        Button button_aiOne = new Button("AI ONE");
        Button button_aiTwo = new Button("AI TWO");


        button_aiOne.setOnMouseClicked(mouseEvent -> {
            aiTurn(game, ai_one);
            updateUI(primaryStage, gridPane, game);
            button_aiOne.setDisable(true);
            button_aiTwo.setDisable(false);
        });

        button_aiTwo.setOnMouseClicked(mouseEvent -> {
            aiTurn(game, ai_two);
            updateUI(primaryStage, gridPane, game);
            button_aiOne.setDisable(false);
            button_aiTwo.setDisable(true);
        });

        gridPane.add(button_aiOne, 0, 1);
        gridPane.add(button_aiTwo, 1, 1);

        Button evaluateTurnsButton = new Button("evaluate turns");
        possibleTurns = new Text("Mögliche Züge: ");

        evaluateTurnsButton.setOnMouseClicked(mouseEvent -> {
            possibleTurns.setText("Mögliche Züge: " + ai_one.getPossibleTurns(game).size());
        });

        gridPane.add(evaluateTurnsButton, 0, 2);
        gridPane.add(possibleTurns, 0, 3);


        primaryStage.setTitle("Hello Eike");
        primaryStage.setScene(new Scene(gridPane, 1000, 700));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void aiTurn(Game game, CoolAI coolAI) {
        try {
            if (game.takeTurn(coolAI.takeTurn(game))) {
                System.out.println(game.lastTurn().getBoard());
            }
        } catch (Exception ignored) {
        }
    }

    private void updateUI(Stage ui, GridPane gridPane, Game game) {

        if (game.getCurrentPlayer() == Color.Black) {
            scenetitle.setText("Schwarz ist dran!");
        } else {
            scenetitle.setText("Grün ist dran!");
        }

        Color[][] currentBoard = game.lastTurn().getBoard().getBoardAsColorArray();
        for (int y = 0; y < 10; ++y) {
            for (int x = 0; x < 10; ++x) {
                gridPane.add(
                        getBoardFieldRect(x, y, currentBoard[x][y]),
                        x + 2,
                        y + 2);
            }
        }

        if(!buttonsCreated){
            createBuildingButtons(gridPane, ui);
            buttonsCreated = true;
        }

        score.setText(getCurrentScore(game));
        ui.show();
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

    private void createBuildingButtons(GridPane gridPane, Stage ui) {

        int x = 12;
        int y = 1;
        for (Button btn : buildingButtons
        ) {
            gridPane.getChildren().remove(btn);
        }

        buildingButtons.clear();

        for (Building building : game.getPlacableBuildings()
        ) {
            if(building.getColor() == game.getCurrentPlayer()){
                Button btn = new Button(building.getName());
                btn.setOnMouseClicked(mouseEvent -> {
                  humanPlayerTurn(game.getCurrentPlayer(), building, gridPane, ui);
                });
                btn.setMinWidth(50);
                gridPane.add(btn, x, y);
                y++;
                buildingButtons.add(btn);
            }
        }
    }

    private void humanPlayerTurn(Color player, Building building, GridPane gridPane, Stage ui){

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

        Building b = building;

        xpBtn.setOnMouseClicked(mouseEvent -> {
            x[0] = x[0] +1;
            testPlace(new Placement(x[0], y[0], direction[0], building), gridPane, ui);
            xText.setText("X: " + x[0]);
        });
        xnBtn.setOnMouseClicked(mouseEvent -> {
            x[0] = x[0] -1;
            testPlace(new Placement(x[0], y[0], direction[0], building), gridPane, ui);
            xText.setText("X: " + x[0]);
        });
        ypBtn.setOnMouseClicked(mouseEvent -> {
            y[0]++;
            testPlace(new Placement(x[0], y[0], direction[0], building), gridPane, ui);
            yText.setText("Y: " + y[0]);
        });
        ynBtn.setOnMouseClicked(mouseEvent -> {
            y[0] = y[0] -1;
            testPlace(new Placement(x[0], y[0], direction[0], building), gridPane, ui);
            yText.setText("Y: " + y[0]);
        });
        rotateBtn.setOnMouseClicked(mouseEvent -> {
            switch (direction[0]){
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
            testPlace(new Placement(x[0], y[0], direction[0], building), gridPane, ui);
        });

        confirmBtn.setOnMouseClicked(mouseEvent -> {
            game.takeTurn(new Placement(x[0], y[0], direction[0], building));
            gridPane.getChildren().remove(xpBtn);
            gridPane.getChildren().remove(xText);
            gridPane.getChildren().remove(xnBtn);
            gridPane.getChildren().remove(ypBtn);
            gridPane.getChildren().remove(yText);
            gridPane.getChildren().remove(ynBtn);
            gridPane.getChildren().remove(rotateBtn);
            gridPane.getChildren().remove(confirmBtn);
            buttonsCreated = false;
        });

        gridPane.add(xpBtn, 15, 1);
        gridPane.add(xText,14,1);
        gridPane.add(xnBtn, 13, 1);

        gridPane.add(ypBtn, 15, 3);
        gridPane.add(yText,14,3);
        gridPane.add(ynBtn, 13, 3);

        gridPane.add(rotateBtn, 13, 5);
        gridPane.add(confirmBtn, 13, 7);
    }

    private void testPlace(Placement placement, GridPane gridPane, Stage ui){
        Game testGame = game.copy();
        testGame.takeTurn(placement);
        updateUI(ui, gridPane, testGame);
    }


    @Override
    public void run() {
    }
}
