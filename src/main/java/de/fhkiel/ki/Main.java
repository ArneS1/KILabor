package de.fhkiel.ki;

import de.fhkiel.ki.cathedral.Game;

public class Main {

  public static void main(String[] args) {
    Game testGame = new Game();
    CoolAI ai_one = new CoolAI();
    CoolAI ai_two = new CoolAI();

    System.out.println(testGame.lastTurn());

    long startTime = System.currentTimeMillis();
    long counter = 0, fails = 0;
    while(System.currentTimeMillis() - startTime < 50000){
      ++counter;

      if(testGame.copy().isFirstPlayerTurn()){
        aiTurn(testGame, ai_one);
      } else {
        aiTurn(testGame, ai_two);
      }

    }

    System.out.println("Score: " + testGame.score() + "\n");
    System.out.println(counter + " tries with " + fails + " unusable turns");
    System.out.println("Leftover buildings " +testGame.getPlacableBuildings());
  }

  private static void aiTurn(Game game, CoolAI coolAI){
    if(game.takeTurn(coolAI.takeTurn(game))){
      System.out.println(game.lastTurn());
    }
  }
}
