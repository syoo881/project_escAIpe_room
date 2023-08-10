package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.UiUtils;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.speech.TextToSpeech;

/** Controller class for the room view. */
public class RoomController {

  @FXML private Rectangle door;
  @FXML private Rectangle window;
  @FXML private Rectangle vase;
  @FXML private Label frameSpeech;

  @FXML private ImageView normalVase;
  Image monsterVaseImage = new Image(getClass().getResourceAsStream("/images/monstervase.png"));
  Image normalVaseImage = new Image(getClass().getResourceAsStream("/images/vase.png"));

  @FXML private ImageView monsterBed;
  Image monsterBedImage = new Image(getClass().getResourceAsStream("/images/eyes.png"));
  Image normalBedImage = new Image(getClass().getResourceAsStream("/images/empty.png"));

  @FXML private Label timerLabel;
  private int remainingSeconds = COUNTDOWN_SECONDS;
  private static final int COUNTDOWN_SECONDS = 120;
  private ChatCompletionRequest chatCompletionRequest;
  private Timeline promptUpdateTimeline;
  private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
  TextToSpeech frameSpeechTts = new TextToSpeech();

  /** Initializes the room view, it is called when the room loads. */
  public void initialize() {
    // Initialization code goes here

    startTimer();
    startPromptUpdate();
    introduceTts();

    Platform.runLater(
        () -> {
          Stage stage = (Stage) frameSpeech.getScene().getWindow();
          stage.setOnCloseRequest(
              event -> {
                stopBackgroundTasks();
                Platform.exit();
                System.exit(0);
              });
        });
  }

  private void introduceTts() {
    Task<Void> frameTts =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {
            frameSpeechTts.speak("Hello welcome to my home");
            return null;
          }
        };
    Thread frameTtsThread = new Thread(frameTts);
    frameTtsThread.start();
  }

  private void stopBackgroundTasks() {
    if (promptUpdateTimeline != null) {
      promptUpdateTimeline.stop();
    }
  }

  // Check if the background tasks work

  private void startPromptUpdate() {
    promptUpdateTimeline =
        new Timeline(new KeyFrame(Duration.seconds(10), event -> updateFrameSpeech()));
    promptUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
    promptUpdateTimeline.play();

    // Stop prompt updates after 2 minutes
    executorService.schedule(() -> promptUpdateTimeline.stop(), 2, TimeUnit.MINUTES);
  }

  private void updateFrameSpeech() {
    executorService.execute(
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            try {
              // Generate a prompt using GptPromptEngineering
              String prompt =
                  "You are a black blob on a frame. You have two close friends hiding in this room."
                      + " They are a vase and a bed. Give 5 word sentences the user as hints but"
                      + " you must never use the word vase and bed, as that would be giving it"
                      + " away. As a reference, you are a helpful cheerful individual, giving hints"
                      + " in the form of jokes, or references, such as: Ever had the feeling of"
                      + " someone grabbing under your matress? Also, only print out one sentences"
                      + " at a time.";

              // Request GPT API for completion
              chatCompletionRequest =
                  new ChatCompletionRequest()
                      .setN(1)
                      .setTemperature(0.2)
                      .setTopP(0.5)
                      .setMaxTokens(100);
              chatCompletionRequest.addMessage(new ChatMessage("user", prompt));

              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();

              // Get the response content
              String response = null;
              for (ChatCompletionResult.Choice choice : chatCompletionResult.getChoices()) {
                response = choice.getChatMessage().getContent();
                break; // We only need the first choice
              }

              final String finalResponse = response; // Create a final variable

              // Update the frameSpeech label
              Platform.runLater(() -> frameSpeech.setText(finalResponse));
            } catch (ApiProxyException e) {
              e.printStackTrace();
              // Handle the exception appropriately
            }
            return null;
          }
        });
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
                if (remainingSeconds % 5 == 0) {
                  // Need to get reference of original chatController class, to use Rungpt
                }
              }
              Platform.runLater(
                  () -> {
                    timerLabel.setText("Time's up!");
                    promptUpdateTimeline.stop();
                  });
            });

    countdownThread.setDaemon(true);
    countdownThread.start();
  }

  public void toMonsterBed() {
    monsterBed.setImage(monsterBedImage);
  }

  public void resetBed() {
    monsterBed.setImage(normalBedImage);
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
   * @throws ApiProxyException
   */
  @FXML
  public void clickMonsterVase(MouseEvent event) throws IOException, ApiProxyException {

    if (!GameState.isMonsterVaseResolved) {

      UiUtils.showDialog(
          "?!!!", "Hey!", "Don't touch me I'm not a monster! What do you even think I am!");

      App.setScene(AppUi.CHAT);
    } else if (GameState.isMonsterVaseResolved) {
      UiUtils.showDialog(":(", "You Meanie!", "Go away!");
    }
  }

  @FXML
  public void clickBed(MouseEvent event) throws IOException {

    if (!GameState.isBedRiddleResolved) {
      UiUtils.showDialog("?!!!!", "MEOW!", "IM NOT SHOWING UNTIL YOU ANSWER RIGHT!");
      App.setScene(AppUi.CHAT);
    } else if (GameState.isBedRiddleResolved) {
      UiUtils.showDialog("O_O", "WOW!", "You're pretty good!");
    }
  }

  @FXML
  public void clickMonsterFrame(MouseEvent event) throws IOException {

    if ((!GameState.isMonsterVaseResolved) && !GameState.isBedRiddleResolved) {
      UiUtils.showDialog("Hehehe", "You found my Friends!", "Now you'll have to beat me!");
    }
  }

  @FXML
  public void click(MouseEvent event) throws IOException {
    System.out.println("door clicked");

    if (!GameState.isMonsterVaseResolved) {
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

  // /**
  //  * Handles the click event on the vase.
  //  *
  //  * @param event the mouse event
  //  */
  // @FXML
  // public void clickVase(MouseEvent event) {
  //   System.out.println("vase clicked");
  //   if (GameState.isRiddleResolved && !GameState.isKeyFound) {
  //     UiUtils.showDialog("Info", "Key Found", "You found a key under the vase!");
  //     GameState.isKeyFound = true;
  //   }
  // }

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
