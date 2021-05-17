package de.fhkiel.ki;

import de.fhkiel.ki.cathedral.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CoolAI {

    private List<Placement> possibleTurns;
    private Board savedGameBoard;

    public CoolAI() {
        possibleTurns = new ArrayList<>();
    }

    public Placement takeTurn(Game game) {
        if (possibleTurns.isEmpty() || possibleTurns.get(0).getBuilding().getId() == 23) {
            possibleTurns = getPossibleTurns(game);
        }
        if (!possibleTurns.isEmpty()) {
            removeImpossibleTurns(game);
        }

        //Falls kein Zug mehr möglich ist
        if (possibleTurns.isEmpty()) {
            if (game.getBoard() == savedGameBoard) { // und der gegner auch nicht mehr zieht
              endGame(game);
            } else {
              savedGameBoard = game.getBoard();
              System.out.println("ICH KANN NICHT MEHR SETZEN! ");
              return null; // und der gegner vielleicht noch ziehen kann
            }
        }

        Placement nextTurn = getTurn();
        Game g = game.copy();
        g.takeTurn(nextTurn);
        savedGameBoard = g.getBoard();
        return nextTurn;
    }

    Placement getTurn() {
        Random rand = new Random();
        return possibleTurns.get(rand.nextInt(possibleTurns.size()));
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

    List<Placement> getPossibleTurns(Game game) {
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

        System.out.println("Mögliche Züge: " + allPlacements.size());

        return allPlacements;
    }

    void endGame(Game game) {
        //System.out.println(game.lastTurn());
        System.out.println(game.getCurrentPlayer() + " hat keinen möglichen Zug gefunden und das Spiel beendet!");
        System.out.println("Score: " + game.score() + "\n");
        System.out.println("Leftover buildings " + game.getPlacableBuildings());

        System.exit(0);
    }
}
