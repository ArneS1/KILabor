package ki;

import ki.cathedral.Color;
import ki.cathedral.Placement;

public class SavedTurn {
    private Color[][] board;
    private Placement turn;

    public SavedTurn(Color[][] board, Placement turn){
        this.board = board;
        this.turn = turn;
    }


}
