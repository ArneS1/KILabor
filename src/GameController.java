import ki.Thomas;
import ki.cathedral.Game;

public class GameController {

    private Game game;

    public GameController(){
        game = new Game();
    }

    public void aiTurn(Thomas coolAI) {
        try {
            if (game.takeTurn(coolAI.takeTurn(game))) {
                System.out.println(game.lastTurn().getBoard());
            }
        } catch (Exception ignored) {
        }
    }

    public void resetGame(){
        game = new Game();
    }

    // Getter / Setter
    public Game getGame(){
        return game;
    }
}
