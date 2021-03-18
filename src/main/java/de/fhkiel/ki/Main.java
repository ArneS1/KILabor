package de.fhkiel.ki;

import de.fhkiel.ki.cathedral.Game;

public class Main {

  public static void main(String[] args) {
    Game testGame = new Game();
    CoolAI ai = new CoolAI();

    System.out.println(testGame.lastTurn());

    long startTime = System.currentTimeMillis();
    long counter = 0, fails = 0;
    while(System.currentTimeMillis() - startTime < 5000){
      ++counter;
      if(testGame.takeTurn(ai.takeTurn(testGame))){
        System.out.println(testGame.lastTurn());
      } else {
        ++fails;
      }
    }

    System.out.println("Score: " + testGame.score() + "\n");
    System.out.println(counter + " tries with " + fails + " unusable turns");
    System.out.println("Leftover buildings " +testGame.getPlacableBuildings());
  }
}
