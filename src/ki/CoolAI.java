package ki;

import ki.cathedral.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoolAI {

    private List<Placement> possibleTurns;
    public Boolean isDone = false;

    /*
    Es folgen die wichtigkeiten bestimmter einflussfaktoren auf den nächsten Zug.
    Jeder zur wird anhand der Faktoren evaluiert und der höchstbewertetste wird ausgeführt.
     */
    private float EVALUTION_MoveDifference = 1;
    private float EVALUTION_CloseToWall = 2;

    public CoolAI() {
        possibleTurns = new ArrayList<>();
    }

    public Placement takeTurn(Game game) {

        possibleTurns = getPossibleTurns(game, game.getCurrentPlayer());

        //Falls kein Zug mehr möglich ist
        if (possibleTurns.isEmpty()) {
            endGame();
            System.out.println("ICH KANN NICHT MEHR SETZEN! ");
            return null; // und der gegner vielleicht noch ziehen kann
        }

        return getTurn(game);
    }

    Placement getTurn(Game game) {
        float rating = 0;
        Placement bestTurn = possibleTurns.get(0);

        for(Placement turn : possibleTurns){
            float currentRating = evaluateTurn(turn, game);
            if(currentRating > rating){
                rating = currentRating;
                bestTurn = turn;
            }
        }
        return bestTurn;
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

    public List<Placement> getPossibleTurns(Game game, Color player) {
        List<Placement> allPlacements = new ArrayList<>();

        List<Building> buildings = game.getPlacableBuildings().stream()
                .filter(building -> building.getColor() == player)
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
        //System.out.println(game.lastTurn());
        //System.out.println(game.getCurrentPlayer() + " hat keinen möglichen Zug gefunden und das Spiel beendet!");
        //System.out.println("Score: " + game.score() + "\n");
        //System.out.println("Leftover buildings " + game.getPlacableBuildings());
        isDone = true;
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
        //TODO: start-game / mid-game / end-game mode

        if(possibleTurns.size() < 100){
            rating += evaluateMovesDifference(turn, testGame, testAi, opponent);
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

    private float evaluateMovesDifference(Placement turn, Game testGame, CoolAI testAi, Color opponent) {
        float addToRating = 0;
        Game testGame2 = testGame.copy();

        testGame.undoLastTurn();
        float enemyPossibleMovesBeforeMove  = testAi.getPossibleTurns(testGame, opponent).size();
        System.out.println("before: " + enemyPossibleMovesBeforeMove);

        testGame2.takeTurn(turn);
        float enemyPossibleMovesAfterMove = testAi.getPossibleTurns(testGame2, opponent).size();
        System.out.println("after: " + enemyPossibleMovesAfterMove);

        addToRating = (1-(enemyPossibleMovesAfterMove / enemyPossibleMovesBeforeMove)) * EVALUTION_MoveDifference;
        System.out.println("Ergebnis: "+ addToRating);

        return addToRating;
    }

}
