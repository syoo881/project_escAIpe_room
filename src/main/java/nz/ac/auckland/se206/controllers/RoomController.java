package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.UiUtils;

/** Controller class for the room view. */
public class RoomController {

  @FXML private Rectangle door;
  @FXML private Rectangle window;
  @FXML private Rectangle vase;

  @FXML private ImageView normalVase;
  Image monsterVaseImage = new Image(getClass().getResourceAsStream("/images/monstervase.png"));
  Image normalVaseImage = new Image(getClass().getResourceAsStream("/images/vase.png"));

  @FXML private Label timerLabel;
  private int remainingSeconds = COUNTDOWN_SECONDS;
  private static final int COUNTDOWN_SECONDS = 120;

  /** Initializes the room view, it is called when the room loads. */
  public void initialize() {
    // Initialization code goes here
    startTimer();
  }

  public void startTimer() {
    Thread countdownThread =
        new Thread(
            () -> {
              while (remainingSeconds > 0) {
                Platform.runLater(() -> timerLabel.setText(formatTime(remainingSeconds)));
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                remainingSeconds--;
              }
              Platform.runLater(() -> timerLabel.setText("Time's up!"));
            });

    countdownThread.setDaemon(true);
    countdownThread.start();
  }

  public void toMonsterVase() {
    normalVase.setImage(monsterVaseImage);
  }

  public void resetVase() {
    normalVase.setImage(normalVaseImage);
  }

  public String formatTime(int seconds) {
    int minutes = seconds / 60;
    int secs = seconds % 60;
    return String.format("%02d:%02d", minutes, secs);
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("key " + event.getCode() + " released");
  }

  // /**
  //  * Displays a dialog box with the given title, header text, and message.
  //  *
  //  * @param title the title of the dialog box
  //  * @param headerText the header text of the dialog box
  //  * @param message the message content of the dialog box
  //  */
  // private void showDialog(String title, String headerText, String message) {
  //   Alert alert = new Alert(Alert.AlertType.INFORMATION);
  //   alert.setTitle(title);
  //   alert.setHeaderText(headerText);
  //   alert.setContentText(message);
  //   alert.showAndWait();
  // }

  /**
   * Handles the click event on the door.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  public void clickMonsterVase(MouseEvent event) throws IOException {

    if (!GameState.isMonsterVaseRiddleResolved) {
      GameState.isMonsterVaseClicked = true;
      GameState.isMonsterBedClicked = false;
      ChatController chatController = new ChatController();
      chatController.choosePrompt();
      UiUtils.showDialog(
          "?!!!", "Hey!", "Don't touch me I'm not a monster! What do you even think I am!");
      if (GameState.isMonsterVaseClicked) {
        System.out.println("monster vase clicked");
      }
      App.setScene(AppUi.CHAT);

      return;
    }
  }

  @FXML
  public void clickMonsterFrame(MouseEvent event) throws IOException {

    if ((!GameState.isMonsterVaseRiddleResolved) && !GameState.isMonsterBedRiddleResolved) {
      UiUtils.showDialog("Hehehe", "You Won!", "Good Job!");
    }
  }

  @FXML
  public void click(MouseEvent event) throws IOException {
    System.out.println("door clicked");

    if (!GameState.isRiddleResolved) {
      UiUtils.showDialog("Info", "Riddle", "You need to resolve the riddle!");
      App.setScene(AppUi.CHAT);
      return;
    }

    if (!GameState.isKeyFound) {
      UiUtils.showDialog(
          "Info", "Find the key!", "You resolved the riddle, now you know where the key is.");
    } else {
      UiUtils.showDialog("Info", "You Won!", "Good Job!");
    }
  }

  /**
   * Handles the click event on the vase.
   *
   * @param event the mouse event
   */
  @FXML
  public void clickVase(MouseEvent event) {
    System.out.println("vase clicked");
    if (GameState.isRiddleResolved && !GameState.isKeyFound) {
      UiUtils.showDialog("Info", "Key Found", "You found a key under the vase!");
      GameState.isKeyFound = true;
    }
  }

  /**
   * Handles the click event on the window.
   *
   * @param event the mouse event
   */
  @FXML
  public void clickWindow(MouseEvent event) {
    System.out.println("window clicked");
  }
}
