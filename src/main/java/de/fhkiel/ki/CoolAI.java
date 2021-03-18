package de.fhkiel.ki;

import de.fhkiel.ki.cathedral.Direction;
import de.fhkiel.ki.cathedral.Game;
import de.fhkiel.ki.cathedral.Placement;
import java.util.Random;

public class CoolAI {
  public Placement takeTurn(Game game) {
    Random rand = new Random();
    return new Placement(
        rand.nextInt(10),
        rand.nextInt(10),
        Direction.values()[rand.nextInt(Direction.values().length)],
        game.getPlacableBuildings().get(rand.nextInt(game.getPlacableBuildings().size())));
  }
}
