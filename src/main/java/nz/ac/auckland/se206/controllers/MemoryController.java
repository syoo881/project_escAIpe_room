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

public class MemoryController implements Initializable {

  private ArrayList<String> possibleButtons =
      new ArrayList<>(
          Arrays.asList(
              "button0", "button1", "button2", "button3", "button4", "button5", "button6",
              "button7", "button8"));

  private ArrayList<Button> buttons = new ArrayList<>();

  private ArrayList<String> pattern = new ArrayList<>();

  private int patternOrder = 0;

  private Random random = new Random();

  private int counter = 0;
  private int turn = 1;
  private TextToSpeech exitTtsSpeech = new TextToSpeech();

  private boolean isStarted = false;

  @FXML private Button button0;

  @FXML private Button button1;

  @FXML private Button button2;

  @FXML private Button button3;

  @FXML private Button button4;

  @FXML private Button button5;

  @FXML private Button button6;

  @FXML private Button button7;

  @FXML private Button button8;

  @FXML private Text text;

  @Override
  // Initialize the code
  public void initialize(URL url, ResourceBundle resourceBundle) {
    buttons.addAll(
        Arrays.asList(
            button0, button1, button2, button3, button4, button5, button6, button7, button8));
  }

  @FXML
  public void onButtonClick(ActionEvent handlingButton) {

    if (isStarted = false) {
      UiUtils.showDialog("???", "Cmon!", "Press Start to begin!");
    } else {

      // When the button is clicked, we use the getIndexOfButton method to get the index of the
      // button
      if (((Control) handlingButton.getSource()).getId().equals(pattern.get(counter))) {
        text.setText("Correct " + counter);
        Button button = buttons.get(getIndexOfButton(handlingButton));
        changeButtonColor(button, "-fx-base: lightgreen");
        counter++;
        // if the counter is equal to 4, the game is over and the user has won
        if (counter == 4) {
          UiUtils.showDialog(
              "Muffins", "Congratulations, you won!", "I'll see you again Next Time!");

          exitSpeech();
          App.setScene(AppUi.EXIT);
        }

      } else {
        // if the user clicks the wrong button, user has to start again
        Button button = buttons.get(getIndexOfButton(handlingButton));
        changeButtonColor(button, "-fx-base: red");
        text.setText("Wrong");
        return;
      }

      if (counter == turn) {
        nextTurn();
      }
    }
  }

  private void exitSpeech() {
    // Speech for tts when exit happens
    Task<Void> exitTts =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {
            exitTtsSpeech.speak("See you next time");
            return null;
          }
        };
    Thread exitTtsThread = new Thread(exitTts);
    exitTtsThread.start();
  }

  @FXML
  public void onStart(ActionEvent handlingStart) {
    isStarted = true;
    pattern.clear();

    pattern.add(possibleButtons.get(random.nextInt(possibleButtons.size())));
    showPattern();

    counter = 0;
    turn = 1;
  }

  private void nextTurn() {
    counter = 0;
    turn++;

    pattern.add(possibleButtons.get(random.nextInt(possibleButtons.size())));
    showPattern();
  }

  private int getIndexOfButton(ActionEvent event) {
    String buttonId = ((Control) event.getSource()).getId();
    return Integer.parseInt(buttonId.substring(buttonId.length() - 1));
  }

  private int getIndexOfButton(String button) {
    return Integer.parseInt(button.substring(button.length() - 1));
  }

  private void showPattern() {
    // Show the pattern in a certain amount of time
    PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
    pause.setOnFinished(
        e -> {
          Timeline timeline =
              new Timeline(
                  new KeyFrame(
                      Duration.seconds(1),
                      event -> {
                        showNext();
                      }));
          timeline.setCycleCount(pattern.size());
          timeline.play();
        });
    pause.play();
  }

  private void showNext() {
    Button button = buttons.get(getIndexOfButton(pattern.get(patternOrder)));
    changeButtonColor(button, "-fx-base: gray");
    patternOrder++;

    if (patternOrder == turn) {
      patternOrder = 0;
    }
  }

  private void changeButtonColor(Button button, String color) {
    // Setting the pause in transition
    button.setStyle(color);
    PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
    pause.setOnFinished(
        e -> {
          button.setStyle(null);
        });
    pause.play();
  }
}