package ki;
import ki.cathedral.*;
import java.util.*;
import java.util.stream.Collectors;

public class CoolAI {

    private List<Placement> possibleTurns;
    public Boolean isDone;
    private AiMode aiMode;
    private Timer timer;
    private int timePerMoveInSeconds;
    public double rating;

    /*
    Es folgen die wichtigkeiten bestimmter einflussfaktoren auf den nächsten Zug.
    Jeder zur wird anhand der Faktoren evaluiert und der höchstbewertetste wird ausgeführt.
     */
    private double EVALUTION_MoveDifference = 4;
    private double EVALUTION_CloseToOwnPiece = 2;
    private double EVALUTION_CloseToEnemyPiece = 2;
    private double EVALUTION_CloseToWall = .5;
    private double EVALUATION_EnclosionPerField = .3;
    private double EVALUATION_EnclosionPerCapturedField = 5;
    private double EVALUATION_PlacedOnOwnedArea = -3;
    private double EVALUATION_ScoreImproved = 2;

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

        return getTurn(game);
    }

    Placement getTurn(Game game) {
        rating = 0;
        Random r = new Random();
        List<Placement> bestTurns = new ArrayList<>(possibleTurns);
        final Boolean[] abortEvaluation = {false};

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                abortEvaluation[0] = true;
            }
        }, timePerMoveInSeconds * 1000);

        for (Placement turn : possibleTurns) {
            System.out.println("Evaluating: " + turn.getBuilding().getName() + " X= " + turn.x() + " Y= " + turn.y());
            if (abortEvaluation[0]) {
                System.out.println("Aborting evaluation...");
                return bestTurns.get(r.nextInt(bestTurns.size()));
            }
            double currentRating = evaluateTurn(turn, game);
            System.out.println("rating received: " + currentRating);
            if (currentRating == rating) {
                bestTurns.add(turn);
            } else if (currentRating > rating) {
                System.out.println("last best: " + rating);
                System.out.println("Better Rating found: " + currentRating);
                rating = currentRating;
                bestTurns.clear();
                bestTurns.add(turn);
            }
        }
        int returnNumber = r.nextInt(bestTurns.size());
        System.out.println("Returning Move: " + bestTurns.get(returnNumber).getBuilding().getName() + " rated: " + rating);
        return bestTurns.get(returnNumber);
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

    private void setAiMode(Game game) {
        aiMode = AiMode.Early;
        if (game.getTurnsSize() == 1) aiMode = AiMode.Initial;
        if (possibleTurns.size() < 700 && aiMode != AiMode.Initial) aiMode = AiMode.Mid;
        if (possibleTurns.size() < 100 && aiMode != AiMode.Initial) aiMode = AiMode.Late;
        System.out.println("AI MODE: " + aiMode.toString());
    }

    private double evaluateTurn(Placement turn, Game game) {
        setAiMode(game);
        double rating = 0;
        Game testGame = game.copy();
        CoolAI testAi = new CoolAI();
        Color me = Color.Black;
        Color opponent = Color.Black;

        switch (game.getCurrentPlayer()) {
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
        switch (aiMode) {
            case Initial:
                //TODO: Cathedral Placement
                break;
            case Early:
                rating += evaluateEnclosion(turn, testGame, me);
                rating += evaluatePieceDistance(turn, testGame, me, opponent);
                rating += evaluateScoreImprovement(turn, testGame, me, opponent);
                break;
            case Mid:
                rating += evaluateEnclosion(turn, testGame, me);
                rating += evaluatePieceDistance(turn, testGame, me, opponent);
                break;
            case Late:
                rating += evaluateEnclosion(turn, testGame, me);
                rating += evaluateMovesDifference(turn, testGame, testAi);
                rating += evaluateScoreImprovement(turn, testGame, me, opponent);
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

    private double evaluateEnclosion(Placement turn, Game testGame, Color currentPlayer) {
        Game enclosionGame = testGame.copy();
        double addToRating = 0;
        Color enclosedAreaColor = Color.White_Owned;
        Color opponent = Color.Black;
        int enclosedFields = 0;
        int capturedBuildingFields = 0;

        //saving board before and after the turn
        Color[][] currentBoard = enclosionGame.getBoard().getBoardAsColorArray();
        enclosionGame.takeTurn(turn);
        Color[][] afterMoveBoard = enclosionGame.getBoard().getBoardAsColorArray();

        // getting the correct player color
        if (currentPlayer == Color.Black) {
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

                if (currentField != enclosedAreaColor && afterMoveField == enclosedAreaColor) {
                    enclosedFields++;
                } else if (currentField == opponent && afterMoveField == enclosedAreaColor) {
                    capturedBuildingFields++;
                }
                if(currentField == enclosedAreaColor && afterMoveField == currentPlayer){
                    addToRating += EVALUATION_PlacedOnOwnedArea;
                }
            }
        }

        // adding the Evaluation Value per enclosed Field
        addToRating += enclosedFields * EVALUATION_EnclosionPerField;
        // adding the value of the Enclosion with capturing per captured Field
        addToRating += capturedBuildingFields * EVALUATION_EnclosionPerCapturedField;

        System.out.println("ENCLOSION: " + addToRating);
        return addToRating;
    }

    private double evaluatePieceDistance(Placement turn, Game testGame, Color me, Color opponent) {
        double addToRating = 0;
        Game pieceDistanceGame = testGame.copy();

        Position position = turn.getPosition();
        int x = position.x();
        int y = position.y();

        Color[][] board = pieceDistanceGame.getBoard().getBoardAsColorArray();

        //Distance to Wall
        if (x > 6) {
            addToRating += ((9.0-(9.0 - x)) / 9) * (EVALUTION_CloseToWall / 2);
        }
        if (x < 4) {
            addToRating += ((9.0 - x)/9.0) * (EVALUTION_CloseToWall / 2);
        }
        if (y > 6) {
            addToRating += ((9.0-(9.0 - y)) / 9) * (EVALUTION_CloseToWall / 2);
        }
        if (y < 4) {
            addToRating += ((9.0 - y)/9.0) * (EVALUTION_CloseToWall / 2);
        }

        for (int i = 1; i <= 3; i++) {
            double initialRating = 0;
            initialRating += addToRating;

            x += i;
            if (isOnBoard(x,y) && !belongsToBuilding(turn, pieceDistanceGame, x, y, me)) {
                if (board[x][y] == me) {
                    addToRating += EVALUTION_CloseToOwnPiece / i / 4;
                } else if (board[x][y] == opponent) {
                    addToRating += EVALUTION_CloseToEnemyPiece / i / 4;
                }
            }
            x -= i;
            y += i;
            if (isOnBoard(x,y) && !belongsToBuilding(turn, pieceDistanceGame, x, y, me)) {
                if (board[x][y] == me) {
                    addToRating += EVALUTION_CloseToOwnPiece / i / 4;
                } else if (board[x][y] == opponent) {
                    addToRating += EVALUTION_CloseToEnemyPiece / i / 4;
                }
            }
            x -= i;
            y -= i;
            if (isOnBoard(x,y) && !belongsToBuilding(turn, pieceDistanceGame, x, y, me)) {
                if (board[x][y] == me) {
                    addToRating += EVALUTION_CloseToOwnPiece / i / 4;
                } else if (board[x][y] == opponent) {
                    addToRating += EVALUTION_CloseToEnemyPiece / i / 4;
                }
            }
            x += i;
            y -= i;
            if (isOnBoard(x,y) && !belongsToBuilding(turn, pieceDistanceGame, x, y, me)) {
                if (board[x][y] == me) {
                    addToRating += EVALUTION_CloseToOwnPiece / i / 4;
                } else if (board[x][y] == opponent) {
                    addToRating += EVALUTION_CloseToEnemyPiece / i / 4;
                }
            }
            if(addToRating > initialRating){
                break;
            }
        }
        System.out.println("PIECE DISTANCE: " + addToRating);
        return addToRating;
    }

    private boolean isOnBoard(int x, int y){
        return x <= 9 && y <= 9 && x >= 0 && y >= 0;
    }

    private boolean belongsToBuilding(Placement turn, Game testGame, int x, int y, Color me){
        Game belongsToBuildingGame = testGame.copy();
        Color[][] before = belongsToBuildingGame.getBoard().getBoardAsColorArray();
        belongsToBuildingGame.takeTurn(turn);
        Color[][] after = belongsToBuildingGame.getBoard().getBoardAsColorArray();
        if(before[x][y] != after[x][y] && after[x][y] == me){
            return true;
        }
        return false;
    }

    private double evaluateMovesDifference(Placement turn, Game testGame, CoolAI testAi) {
        double addToRating = 0;
        Game movesDiffGame = testGame.copy();

        float enemyPossibleMovesBeforeMove = testAi.getPossibleTurns(movesDiffGame).size();
        movesDiffGame.takeTurn(turn);
        movesDiffGame.takeTurn(testAi.getPossibleTurns(movesDiffGame).get(0));
        float enemyPossibleMovesAfterMove = testAi.getPossibleTurns(movesDiffGame).size();

        addToRating = (1 - (enemyPossibleMovesAfterMove / enemyPossibleMovesBeforeMove)) * EVALUTION_MoveDifference;

        System.out.println("MOVES DIFFERENCE: " + addToRating);
        return addToRating;
    }

    private double evaluateScoreImprovement(Placement turn, Game testGame, Color me, Color opponent){
        double addToRating = 0;

        if(testGame.score().get(me) - testGame.score().get(opponent) >= 0 ){
            //me verliert oder hat gleichstand, differenz ist positiv oder 0
            //addToRating = ((-afterScoreDiff) + beforeScoreDiff) /10.0 * EVALUATION_ScoreImproved;
            int before = testGame.score().get(me);
            testGame.takeTurn(turn);
            int after = testGame.score().get(me);
            addToRating += (before - after) * EVALUATION_ScoreImproved / 10;
        } else {
            //me gewinnt, differenz ist negativ
            //addToRating = (afterScoreDiff - beforeScoreDiff) /10.0 * EVALUATION_ScoreImproved;
            int before = testGame.score().get(me);
            testGame.takeTurn(turn);
            int after = testGame.score().get(me);
            addToRating += (before - after) * EVALUATION_ScoreImproved / 20;
        }

        System.out.println("SCORE IMPROVEMENT: " + addToRating);
        return addToRating;
    }

    public double rateLastTurn(Game game){
        Game testGame = game.copy();
        Placement placement = testGame.lastTurn().getAction();
        testGame.undoLastTurn();
        possibleTurns = getPossibleTurns(testGame);
        return evaluateTurn(placement,testGame);
    }

}
