package ki;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import ki.cathedral.*;

import java.util.*;
import java.util.stream.Collectors;

public class CoolAI {

    private List<Placement> possibleTurns;
    public Boolean isDone;
    public AiMode aiMode;
    private Timer timer;
    private int timePerMoveInSeconds;

    /*
    Es folgen die wichtigkeiten bestimmter einflussfaktoren auf den nächsten Zug.
    Jeder zur wird anhand der Faktoren evaluiert und der höchstbewertetste wird ausgeführt.
     */
    private double EVALUTION_MoveDifference = 1;
    private double EVALUTION_CloseToPiece = 2;
    private double EVALUTION_CloseToWall = 2;
    private double EVALUATION_EnclosionPerField = .2;
    private double EVALUATION_EnclosionPerCapturedField = .5;

    public CoolAI() {
        possibleTurns = new ArrayList<>();
        isDone = false;
        aiMode = AiMode.Initial;
        timer = new Timer();
        timePerMoveInSeconds = 30;
    }

    public Placement takeTurn(Game game) {

        possibleTurns = getPossibleTurns(game);

        //Falls kein Zug mehr möglich ist
        if (possibleTurns.isEmpty()) {
            endGame();
            System.out.println("ICH KANN NICHT MEHR SETZEN! ");
            return null; // und der gegner vielleicht noch ziehen kann
        }

        setAiMode(game);
        return getTurn(game);
    }

    Placement getTurn(Game game) {
        double rating = 0;
        Random r = new Random();
        List<Placement> bestTurns = new ArrayList<>(possibleTurns);
        final Boolean[] abortEvaluation = {false};

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                abortEvaluation[0] = true;
            }
        }, timePerMoveInSeconds * 1000);

        for(Placement turn : possibleTurns){
            if(abortEvaluation[0]) {
                System.out.println("Aborting evaluation...");
                return bestTurns.get(r.nextInt(bestTurns.size()));
            }
            double currentRating = evaluateTurn(turn, game);
            if(currentRating == rating){
                bestTurns.add(turn);
            } else if(currentRating > rating){
                System.out.println("last best: " + rating);
                System.out.println("Better Rating found: " + currentRating);
                rating = currentRating;
                bestTurns.clear();
                bestTurns.add(turn);
            }
        }

        return bestTurns.get(r.nextInt(bestTurns.size()));
    }

    private void removeImpossibleTurns(Game game) {
        List<Placement> deleteTurns = new ArrayList<>();
        for (Placement turn : possibleTurns) {
            if (!game.copy().takeTurn(turn)) {
                deleteTurns.add(turn);
            }
        }
        possibleTurns.removeAll(deleteTurns);
    }

    public List<Placement> getPossibleTurns(Game game) {
        List<Placement> allPlacements = new ArrayList<>();

        List<Building> buildings = game.getPlacableBuildings().stream()
                .filter(building -> building.getColor() == game.getCurrentPlayer())
                .collect(Collectors.toList());

        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                for (Building building : buildings) {
                    List<Placement> testPlacements = new ArrayList<>();

                    testPlacements.add(new Placement(x, y, Direction._0, building));

                    if (building.getTurnable() == Turnable.Half) {
                        testPlacements.add(new Placement(x, y, Direction._90, building));
                    }

                    if (building.getTurnable() == Turnable.Full) {
                        testPlacements.add(new Placement(x, y, Direction._180, building));
                        testPlacements.add(new Placement(x, y, Direction._270, building));
                        testPlacements.add(new Placement(x, y, Direction._90, building));
                    }

                    for (Placement testPlacement : testPlacements) {
                        if (game.copy().takeTurn(testPlacement)) {
                            allPlacements.add(testPlacement);
                        }
                    }
                }
            }
        }
        return allPlacements;
    }

    void endGame() {
        isDone = true;
    }

    private void setAiMode(Game game){
        aiMode = AiMode.Early;
        if(game.getTurnsSize() == 0) aiMode = AiMode.Initial;
        if(possibleTurns.size() < 1000) aiMode = AiMode.Mid;
        if(possibleTurns.size() < 100) aiMode = AiMode.Late;
    }

    private double evaluateTurn(Placement turn, Game game){
        double rating = 0;
        Game testGame = game.copy();
        CoolAI testAi = new CoolAI();
        Color me = Color.Black;
        Color opponent = Color.Black;

        switch (game.getCurrentPlayer()){
            case Black:
                me = Color.Black;
                opponent = Color.White;
                break;
            case White:
                me = Color.White;
                opponent = Color.Black;
                break;
        }

        //Evaluation Methods
        switch (aiMode){
            case Initial:
                //TODO: Cathedral Placement
                break;
            case Early:
                rating += evaluateEnclosion(turn, testGame, me);
                break;
            case Mid:
                rating += evaluateEnclosion(turn, testGame, me);
                break;
            case Late:
                rating += evaluateEnclosion(turn, testGame, me);
                rating += evaluateMovesDifference(turn, testGame, testAi);
                break;
        }

        /*
        + weniger züge für den gegner möglich
        + viele eigene Züge möglich
            -> verhältnis ergibt wert
        + berührt eigenen stein
        + berührt wand
        + schließt bereich an (++ mit gegnergebäude drin)
         */

        return rating;
    }

    private double evaluateEnclosion(Placement turn, Game testGame, Color currentPlayer){
        double addToRating = 0;
        Color enclosedAreaColor = Color.White_Owned;
        Color opponent = Color.Black;
        int enclosedFields = 0;
        int capturedBuildingFields = 0;

        //saving board before and after the turn
        Color[][] currentBoard = testGame.getBoard().getBoardAsColorArray();
        testGame.takeTurn(turn);
        Color[][] afterMoveBoard = testGame.getBoard().getBoardAsColorArray();

        // getting the correct player color
        if(currentPlayer == Color.Black){
            enclosedAreaColor = Color.Black_Owned;
            opponent = Color.White;
        }

        //checking each field before and after move to see if its color changed to being enclosed or captured
        Color currentField = Color.None;
        Color afterMoveField = Color.None;
        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                currentField = currentBoard[x][y];
                afterMoveField = afterMoveBoard[x][y];

                if(currentField != enclosedAreaColor && afterMoveField == enclosedAreaColor){
                    enclosedFields++;
                } else if(currentField == opponent && afterMoveField == enclosedAreaColor){
                    capturedBuildingFields++;
                }
            }
        }

        // adding the Evaluation Value per enclosed Field
        addToRating += enclosedFields * EVALUATION_EnclosionPerField;
        // adding the value of the Enclosion with capturing per captured Field
        addToRating += capturedBuildingFields * EVALUATION_EnclosionPerCapturedField;

        System.out.println("EVALUATING ENCLOSION");
        System.out.println("ENCLOSED FIELDS: " + enclosedFields);
        System.out.println("CAPTURED FIELDS: " + capturedBuildingFields);

        return addToRating;
    }

    private double evaluatePieceDistance(Placement turn, Game testGame){
        double addToRating = 0;

        /*
        Position holen
        Building Beschaffenheit holen (Länge breite direction)

        Abstand von Kante des Buildings zu nächstem farbigen feld in X+, X-, Y+, Y-
        Farbe des feld checken
        farbe = eigene -> Rating+
        wiederholen für nächste richtung
         */

        Position position = turn.getPosition();
        Building building = turn.getBuilding();

        //X+ richtung
        int distance = 0;

        return addToRating;
    }

    private double evaluateMovesDifference(Placement turn, Game testGame, CoolAI testAi) {
        double addToRating = 0;
        Game testGame2 = testGame.copy();

        testGame.undoLastTurn();
        float enemyPossibleMovesBeforeMove  = testAi.getPossibleTurns(testGame).size();
        System.out.println("before: " + enemyPossibleMovesBeforeMove);

        testGame2.takeTurn(turn);
        float enemyPossibleMovesAfterMove = testAi.getPossibleTurns(testGame2).size();
        System.out.println("after: " + enemyPossibleMovesAfterMove);

        addToRating = (1-(enemyPossibleMovesAfterMove / enemyPossibleMovesBeforeMove)) * EVALUTION_MoveDifference;
        System.out.println("Ergebnis: "+ addToRating);

        return addToRating;
    }

}
