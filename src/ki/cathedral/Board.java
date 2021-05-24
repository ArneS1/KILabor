package ki.cathedral;

import java.util.*;
import java.util.stream.Collectors;

public class Board {
  private final Map<Building, Integer> freeBuildings = new HashMap<>();
  private final List<Placement> placedBuildings = new ArrayList<>();

  private Color[][] board = new Color[10][10];

  public Board(Map<Building, Integer> freeBuildings) {
    freeBuildings.keySet()
        .forEach(building -> this.freeBuildings.put(building, freeBuildings.getOrDefault(building, 0)));

    for(int y = 0; y < 10; ++y){
      for(int x = 0; x < 10; ++x){
        board[y][x] = Color.None;
      }
    }
  }

  public Color[][] getBoardAsColorArray(){
    return board.clone();
  }

  public Board copy(){
    Board newBoard = new Board(freeBuildings);

    newBoard.placedBuildings.addAll(placedBuildings);

    for(int y = 0; y < 10; ++y){
      System.arraycopy(board[y], 0, newBoard.board[y], 0, 10);
    }
    return newBoard;
  }

  public boolean placeBuilding(Placement placement){
    if(freeBuildings.getOrDefault(placement.getBuilding(), 0) <= 0){
      return false;
    }
    if(isNotPlaceable(placement)){
      return false;
    }

    placeColor(placement.getForm(), placement.getBuilding().getColor(), placement.x(), placement.y());
    freeBuildings.put(placement.getBuilding(), freeBuildings.getOrDefault(placement.getBuilding(), 0) - 1);
    placedBuildings.add(placement);

    buildRegions();
    buildRegions();

    return true;
  }

  public Map<Color, Integer> score(){
    Map<Color, Integer> score = new HashMap<>();
    score.put(Color.Black, 0);
    score.put(Color.White, 0);

    freeBuildings.keySet().stream().filter(building -> score.containsKey(building.getColor()))
        .forEach(building -> score.put(building.getColor(), score.get(building.getColor()) + building.score() * freeBuildings.get(building)));

    return score;
  }

  public int getNumberOfFreeBuildings(Building building){
    return freeBuildings.getOrDefault(building, 0);
  }

  public Set<Building> getBuildings(){
    return freeBuildings.keySet();
  }

  private void buildRegions() {
    Arrays.stream(new Color[] {Color.Black, Color.White}).forEach(color -> {
      Stack<Position> freeFields = new Stack<>();
      for(int y = 0; y < 10; ++y){
        for(int x = 0; x < 10; ++x){
          if(this.board[y][x] != color){
            freeFields.add(new Position(x,y));
          }
        }
      }
      while(!freeFields.isEmpty()){
        Stack<Position> freeFieldsToLookAt = new Stack<>();
        freeFieldsToLookAt.push(freeFields.pop());

        List<Position> region = new ArrayList<>();
        Set<Direction> borders = new HashSet<>();

        while(!freeFieldsToLookAt.isEmpty()){
          Position currentField = freeFieldsToLookAt.pop();

          region.add(currentField);

          for(int y = -1; y < 2; ++y){
            final int realY = currentField.y() + y;
            if(realY < 0){
              borders.add(Direction._0);
              continue;
            }
            if(realY > 9){
              borders.add(Direction._180);
              continue;
            }
            for(int x = -1; x < 2; ++x){
              final int realX = currentField.x() + x;
              if(realX < 0){
                borders.add(Direction._270);
                continue;
              }
              if(realX > 9){
                borders.add(Direction._90);
                continue;
              }
              Optional<Position> fieldToLookAt = freeFields.stream().filter(position -> position.equals(new Position(realX, realY))).findAny();
              if(fieldToLookAt.isPresent()){
                freeFieldsToLookAt.push(fieldToLookAt.get());
                freeFields.remove(fieldToLookAt.get());
              }
            }
          }
        }

        if(borders.size() < 3 && isNotAlreadyOwned(region, color)){
          List<Placement> enemyBuildingsInRegion = getAllEnemyBuildingsInRegion(region, color);
          if(enemyBuildingsInRegion.size() < 2){
            enemyBuildingsInRegion.forEach(this::removePlacement);
            placeColor(region, Color.getOwned(color), 0, 0);
          }
        }
      }
    });
  }

  private boolean isNotAlreadyOwned(List<Position> region, Color color) {
    return region.stream().anyMatch(position -> board[position.y()][position.x()] != Color.getOwned(color));
  }

  private void removePlacement(Placement placement) {
    placedBuildings.remove(placement);
    placeColor(placement.getForm(), Color.None, placement.x(), placement.y());
  }

  private List<Placement> getAllEnemyBuildingsInRegion(List<Position> region, Color color) {
    return placedBuildings.stream()
        .filter(placement -> region.contains(placement.getPosition()))
        .filter(placement -> placement.getBuilding().getColor() != color)
        .collect(Collectors.toList());
  }

  private void placeColor(List<Position> form, Color color, int x, int y) {
    form.forEach(position -> board[position.y() + y][position.x() + x] = color);
  }

  private boolean isNotPlaceable(Placement placement) {
    return placement.getForm().stream().anyMatch(position -> {
      int realX = position.x() + placement.x();
      int realY = position.y() + placement.y();

      return outOfBounds(realX) || outOfBounds(realY) || !colorIsCompatible(board[realY][realX], placement.getBuilding().getColor());
    });
  }

  private boolean colorIsCompatible(Color onPosition, Color toPlace) {
    return onPosition == Color.None || onPosition == Color.Black_Owned && toPlace == Color.Black || onPosition == Color.White_Owned && toPlace == Color.White;
  }

  private boolean outOfBounds(int number){
    return !(number >= 0 && number < 10);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Board board1 = (Board) o;
    return Objects.equals(freeBuildings, board1.freeBuildings) &&
        Objects.equals(placedBuildings, board1.placedBuildings) &&
        Arrays.equals(board, board1.board);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(freeBuildings, placedBuildings);
    result = 31 * result + Arrays.hashCode(board);
    return result;
  }

  @Override
  public String toString() {
    StringJoiner boardAsString = new StringJoiner(", ", Board.class.getSimpleName() + "[", "]")
        .add("\nfreeBuildings=" + freeBuildings)
        .add("\nplacedBuildings=" + placedBuildings)
        .add("\nboard=\n");
    for(int y = 0; y < 10; ++y){
      boardAsString.add(Arrays.toString(board[y]) + "\n");
    }
    return boardAsString.toString();
  }
}
