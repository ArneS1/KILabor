package de.fhkiel.ki;

import de.fhkiel.ki.cathedral.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CoolAI {

  public Placement takeTurn(Game game) {
    Random rand = new Random();
    List<Placement> placements = getTurns(game);
    if(placements.isEmpty()){
      //Kein zug mehr möglich.
      System.out.println(game.lastTurn());
      System.out.println(game.getCurrentPlayer() + " hat keinen möglichen Zug gefunden und das Spiel beendet!");
      System.out.println("Score: " + game.score() + "\n");
      System.out.println("Leftover buildings " +game.getPlacableBuildings());

      System.exit(0);
    }
    return placements.get(rand.nextInt(placements.size()));
  }

  List<Placement> getTurns(Game game){
    List<Placement> allPlacements = new ArrayList<>();

    Random rand = new Random();

    List<Building> buildings = game.getPlacableBuildings().stream()
            .filter(building -> building.getColor() == game.getCurrentPlayer())
            .collect(Collectors.toList());

    for(int x = 0; x < 10; ++x){
      for(int y = 0; y <10; ++y){
        for(Building building: buildings){
          List<Placement> testPlacements = new ArrayList<>();

          testPlacements.add(new Placement(x, y, Direction._0, building));

          if(building.getTurnable() == Turnable.Half){
            testPlacements.add(new Placement(x, y, Direction._90, building));
          }

          if(building.getTurnable() == Turnable.Full){
            testPlacements.add(new Placement(x, y, Direction._180, building));
            testPlacements.add(new Placement(x, y, Direction._270, building));
            testPlacements.add(new Placement(x, y, Direction._90, building));
          }

          for (Placement testPlacement: testPlacements) {
            if(game.copy().takeTurn(testPlacement)){
              allPlacements.add(testPlacement);
            }
          }
        }
      }
    }

    System.out.println("Mögliche Züge: " + allPlacements.size());

    return allPlacements;
  }
}
