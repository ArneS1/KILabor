package de.fhkiel.ki.cathedral;

import com.sun.javafx.collections.ImmutableObservableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Building {
  private final int id;
  private final String name;
  private final Color color;
  private final Turnable turnable;
  private final List<Position> form;

  public Building(int id, String name, Color color, Turnable turnable,
                  int[]... form) {
    this.id = id;
    this.name = name;
    this.color = color;
    this.turnable = turnable;
    this.form = Collections.unmodifiableList(Arrays.stream(form).map(ints -> new Position(ints[0], ints[1])).collect(Collectors.toList()));
  }

  public List<Position> turn(Direction direction){
    if (turnable != Turnable.No){
      List<Position> turnedForm = new ArrayList<>();
      form.forEach(position -> {
        Position turnedPosition = position;
        for(int turns = 0; turns < direction.getNumber()%turnable.getNumber(); ++turns ){
          turnedPosition = new Position(-turnedPosition.y(), turnedPosition.x());
        }
        turnedForm.add(turnedPosition);
      });
      return turnedForm;
    }
    return form;
  }

  public int score(){
    return form.size();
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Color getColor() {
    return color;
  }

  public Turnable getTurnable() {
    return turnable;
  }

  public List<Position> getForm() {
    return form;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Building building = (Building) o;
    return id == building.id &&
        Objects.equals(name, building.name) &&
        color == building.color &&
        turnable == building.turnable &&
        Objects.equals(form, building.form);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, color, turnable, form);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Building.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("name='" + name + "'")
        .add("color=" + color)
        .add("turnable=" + turnable)
        .add("form=" + form)
        .toString();
  }
}
