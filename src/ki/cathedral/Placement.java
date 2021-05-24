package ki.cathedral;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Placement {
  private final Position position;
  private final Direction direction;

  private final Building building;

  public Placement(int x, int y, Direction direction, Building building) {
    position = new Position(x, y);
    this.direction = direction;
    this.building = building;
  }

  public Placement copy(){
    return new Placement(position.x(), position.y(), direction, building);
  }

  public int x() {
    return position.x();
  }

  public int y() {
    return position.y();
  }

  public Position getPosition() {
    return position;
  }

  public Direction getDirection() {
    return direction;
  }

  public Building getBuilding() {
    return building;
  }

  public List<Position> getForm() {
    return building.turn(direction);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Placement placement = (Placement) o;
    return position.equals(placement.position) &&
        direction == placement.direction &&
        building.equals(placement.building);
  }

  @Override
  public int hashCode() {
    return Objects.hash(position, direction, building);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Placement.class.getSimpleName() + "[", "]")
        .add("position=" + position)
        .add("direction=" + direction)
        .add("building=" + building)
        .toString();
  }
}
