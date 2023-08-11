package nz.ac.auckland.se206.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.text.Text;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.UiUtils;
import nz.ac.auckland.se206.speech.TextToSpeech;

public class MemoryGameController implements Initializable {

  private ArrayList<String> buttonNames =
      new ArrayList<>(
          Arrays.asList(
              "button0", "button1", "button2", "button3", "button4", "button5", "button6",
              "button7", "button8"));

  private ArrayList<Button> gameButtons = new ArrayList<>();

  private ArrayList<String> patternSequence = new ArrayList<>();

  private int patternIndex = 0;

  private Random randomGenerator = new Random();

  private int clickCount = 0;
  private int turnCount = 1;
  private TextToSpeech exitSpeech = new TextToSpeech();

  private boolean hasStarted = false;

  @FXML private Button button0;

  @FXML private Button button1;

  @FXML private Button button2;

  @FXML private Button button3;

  @FXML private Button button4;

  @FXML private Button button5;

  @FXML private Button button6;

  @FXML private Button button7;

  @FXML private Button button8;

  @FXML private Text displayText;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    gameButtons.addAll(
        Arrays.asList(
            button0, button1, button2, button3, button4, button5, button6, button7, button8));
  }

  @FXML
  public void handleButtonClick(ActionEvent event) {
    if (!hasStarted) {
      UiUtils.showDialog("Muffin", "Hey there!", "Press Start to begin the game.");
    } else {
      if (((Control) event.getSource()).getId().equals(patternSequence.get(clickCount))) {
        displayText.setText("Correct " + clickCount);
        Button clickedButton = gameButtons.get(getButtonIndex(event));
        changeButtonAppearance(clickedButton, "-fx-base: lightgreen");
        clickCount++;

        if (clickCount == 4) {
          UiUtils.showDialog("Congratulations", "You've won!", "See you next time!");

          exitGameSpeech();
          App.setScene(AppUi.EXIT);
        }
      } else {
        Button clickedButton = gameButtons.get(getButtonIndex(event));
        changeButtonAppearance(clickedButton, "-fx-base: red");
        displayText.setText("Wrong");
        return;
      }

      if (clickCount == turnCount) {
        nextTurn();
      }
    }
  }

  private void exitGameSpeech() {
    Task<Void> exitSpeechTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            exitSpeech.speak("Goodbye, see you soon!");
            return null;
          }
        };

    Thread exitSpeechThread = new Thread(exitSpeechTask);
    exitSpeechThread.start();
  }

  @FXML
  public void handleStart(ActionEvent event) {
    hasStarted = true;
    patternSequence.clear();

    patternSequence.add(buttonNames.get(randomGenerator.nextInt(buttonNames.size())));
    displayPattern();

    clickCount = 0;
    turnCount = 1;
  }

  private void nextTurn() {
    clickCount = 0;
    turnCount++;

    patternSequence.add(buttonNames.get(randomGenerator.nextInt(buttonNames.size())));
    displayPattern();
  }

  private int getButtonIndex(ActionEvent event) {
    String buttonId = ((Control) event.getSource()).getId();
    return Integer.parseInt(buttonId.substring(buttonId.length() - 1));
  }

  private int getButtonIndex(String buttonName) {
    return Integer.parseInt(buttonName.substring(buttonName.length() - 1));
  }

  private void displayPattern() {
    PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
    pause.setOnFinished(
        e -> {
          Timeline timeline =
              new Timeline(
                  new KeyFrame(
                      Duration.seconds(1),
                      event -> {
                        displayNext();
                      }));
          timeline.setCycleCount(patternSequence.size());
          timeline.play();
        });
    pause.play();
  }

  private void displayNext() {
    Button button = gameButtons.get(getButtonIndex(patternSequence.get(patternIndex)));
    changeButtonAppearance(button, "-fx-base: gray");
    patternIndex++;

    if (patternIndex == turnCount) {
      patternIndex = 0;
    }
  }

  private void changeButtonAppearance(Button button, String style) {
    button.setStyle(style);
    PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
    pause.setOnFinished(
        e -> {
          button.setStyle(null);
        });
    pause.play();
  }
}
