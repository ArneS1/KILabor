package de.fhkiel.ki.cathedral;

public enum Direction {
  _0(0),
  _90(1),
  _180(2),
  _270(3);

  private final int number;

  Direction(int number) {
    this.number = number;
  }

  public int getNumber() {
    return number;
  }
}
