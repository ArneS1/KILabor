package ki.cathedral;

import java.util.Map;
import java.util.StringJoiner;

public class Turn {
  private final int turnNumber;
  private final Board board;

  private final Placement action;

  public Turn(int turnNumber, Board board, Placement action) {
    this.turnNumber = turnNumber;
    this.board = board;
    this.action = action;
  }

  public Turn copy(){
    return new Turn(turnNumber, board.copy(), action);
  }

  public Map<Color, Integer> score(){
    return board.score();
  }

  public int getTurnNumber() {
    return turnNumber;
  }

  public Board getBoard() {
    return board;
  }

  public Placement getAction() {
    return action;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Turn.class.getSimpleName() + "[", "]")
        .add("turnNumber=" + turnNumber)
        .add("action=" + action)
        .add("score=" + score().toString())
        .add("board=" + board)
        .toString();
  }
}
