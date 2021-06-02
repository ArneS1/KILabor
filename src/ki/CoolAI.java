package ki;

import javafx.geometry.Pos;
import ki.cathedral.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CoolAI {

    private List<Placement> possibleTurns;
    public Boolean isDone = false;
    public AiMode aiMode = AiMode.Initial;

    /*
    Es folgen die wichtigkeiten bestimmter einflussfaktoren auf den nächsten Zug.
    Jeder zur wird anhand der Faktoren evaluiert und der höchstbewertetste wird ausgeführt.
     */
    private float EVALUTION_MoveDifference = 1;
    private float EVALUTION_CloseToPiece = 2;
    private float EVALUTION_CloseToWall = 2;

    public CoolAI() {
        possibleTurns = new ArrayList<>();
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
        float rating = 0;
        List<Placement> bestTurns = new ArrayList<>(possibleTurns);

        for(Placement turn : possibleTurns){
            float currentRating = evaluateTurn(turn, game);
            if(currentRating == rating){
                bestTurns.add(turn);
            } else if(currentRating > rating){
                bestTurns.clear();
                bestTurns.add(turn);
            }
        }

        Random r = new Random();
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

        //System.out.println("Mögliche Züge: " + allPlacements.size());

        return allPlacements;
    }

    void endGame() {
        isDone = true;
    }

    private void setAiMode(Game game){
        aiMode = AiMode.Early;
        if(game.getTurnsSize() == 0) aiMode = AiMode.Initial;
        if(possibleTurns.size() < 1000) aiMode = AiMode.Mid;
        if(possibleTurns.size() < 150) aiMode = AiMode.Late;
    }

    private float evaluateTurn(Placement turn, Game game){
        float rating = 0;
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
                break;
            case Mid:
                break;
            case Late:
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

    private float evaluatePieceDistance(Placement turn, Game testGame){
        float addToRating = 0;

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

    private float evaluateMovesDifference(Placement turn, Game testGame, CoolAI testAi) {
        float addToRating = 0;
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
