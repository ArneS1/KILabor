package de.fhkiel.ki.cathedral;

import java.util.StringJoiner;

public enum Color {
  None,
  Blue,
  Black,
  Black_Owned,
  White,
  White_Owned;

  public static Color getOwned(Color color) {
    switch(color){
      case Black:
        return Black_Owned;
      case White:
        return White_Owned;
      default:
        return None;
    }
  }

  @Override
  public String toString() {
    return ordinal() + "";
  }
}
