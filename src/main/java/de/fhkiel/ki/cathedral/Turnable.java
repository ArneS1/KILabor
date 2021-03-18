package de.fhkiel.ki.cathedral;

public enum Turnable {
  No(1),
  Half(2),
  Full(4);

  private final int number;

  Turnable(int number) {
    this.number = number;
  }

  public int getNumber() {
    return number;
  }
}
