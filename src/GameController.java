import ki.CoolAI;
import ki.cathedral.Game;

public class GameController {

    private Game game;

    public GameController(){
        game = new Game();
    }

    public void aiTurn(CoolAI coolAI) {
        try {
            if (game.takeTurn(coolAI.takeTurn(game))) {
                System.out.println(game.lastTurn().getBoard());
            }
        } catch (Exception ignored) {
        }
    }

    // Getter / Setter
    public Game getGame(){
        return game;
    }
}
