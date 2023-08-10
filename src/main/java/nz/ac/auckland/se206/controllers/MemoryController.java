package nz.ac.auckland.se206.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class MemoryController implements Initializable {

  ArrayList<String> possibleButtons =
      new ArrayList<>(
          Arrays.asList(
              "button1", "button2", "button3", "button4", "button5", "button6", "button7",
              "button8", "button9"));

  ArrayList<Button> buttons = new ArrayList<>();
  ArrayList<String> pattern = new ArrayList<>();

  int patternOrder = 0;

  Random random = new Random();
  int counter = 0;
  int turn = 1;
  private Text text;

  @FXML private Button button1;
  @FXML private Button button2;
  @FXML private Button button3;
  @FXML private Button button4;
  @FXML private Button button5;
  @FXML private Button button6;
  @FXML private Button button7;
  @FXML private Button button8;
  @FXML private Button button9;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    buttons.addAll(
        Arrays.asList(
            button1, button2, button3, button4, button5, button6, button7, button8, button9));
  }
}
