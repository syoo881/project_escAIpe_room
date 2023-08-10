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

          // Set the close request event handler here
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
              String prompt;
              chatCompletionRequest =
                  new ChatCompletionRequest()
                      .setN(1)
                      .setTemperature(0.5)
                      .setTopP(0.9)
                      .setMaxTokens(100);
              if (!GameState.isBedRiddleResolved && !GameState.isMonsterVaseResolved) {
                prompt =
                    "You are a black blob on a frame. You have two close friends hiding in this"
                        + " room. They are a vase, Vanessa and hidden under a bed, called Cade."
                        + " Give one sentence hints about Vanessa and Cade without using the word"
                        + " vase and bed. The sentences are at most 7 words long. . Also talk about"
                        + " your two hidden friends as well. Also, only print out one sentences at"
                        + " a time.";

                // Request GPT API for completion

                chatCompletionRequest.addMessage(new ChatMessage("user", prompt));
              } else if (GameState.isBedRiddleResolved && !GameState.isMonsterVaseResolved) {
                System.out.println("BedRiddle is solved");
                prompt =
                    "Note: Only answer with one sentence. The sentences are at most 7 words long."
                        + " Since the BedRiddle is solved, first congratulate the user for finding"
                        + " Fred and solving the riddle. Then, give a hint for the vase, but you"
                        + " still cannot use the word vase. After, talk about your friend Vanessa"
                        + " (the vase) Also, only print out one sentence at a time.";
                chatCompletionRequest.addMessage(new ChatMessage("user", prompt));
              } else if (!GameState.isBedRiddleResolved && GameState.isMonsterVaseResolved) {
                prompt =
                    "Note: Only answer with one sentence. The sentences are at most 7 words long."
                        + " Since the MonsterVaseGame is solved, first congratulate the user for"
                        + " finding Vanessa the vase and solving the game. Then, give a hint for"
                        + " the bed but you still cannot use the word bed. After, talk about your"
                        + " friend Cade (hiding under the bed). Also, only print out one sentence"
                        + " at a time.";
                chatCompletionRequest.addMessage(new ChatMessage("user", prompt));

              } else {
                prompt =
                    "Note: Only anwer with one sentence. The sentences are at most 7 words long."
                        + " Since both games are solved, First congratulate the user for finding"
                        + " both friends and solving the games. Then, tell them that they have one"
                        + " last task to do, which is to find someone/something. Also, only print"
                        + " out one sentence at a time.";
                chatCompletionRequest.addMessage(new ChatMessage("user", prompt));
              }

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

  private void startTimer() {
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
              Platform.runLater(
                  () -> {
                    timerLabel.setText("XX:XX");
                    promptUpdateTimeline.stop();
                    App.showGameOverDialog(); // Call the method to display game over dialog
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
          "?!!!",
          "Hey! Don't touch me I'm not a monster! What do you even think I am!",
          " You're gonna have to beat" + " me with your hands! First to three!");
      App.setScene(AppUi.RCS);
    } else if (GameState.isMonsterVaseResolved) {
      UiUtils.showDialog(":(", "You Meanie!", "Go away!");
    }
  }

  @FXML
  public void clickBed(MouseEvent event) throws IOException {

    if (!GameState.isBedRiddleResolved) {
      UiUtils.showDialog("?!!!!", "MEOW!", "IM NOT SHOWING UNTIL YOU ANSWER RIGHT!");
      App.setScene(AppUi.CHAT);
      return;
    } else if (GameState.isBedRiddleResolved) {
      UiUtils.showDialog("O_O", "WOW!", "You're pretty good!");
      return;
    }
  }

  @FXML
  public void clickMonsterFrame(MouseEvent event) throws IOException {

    if ((!GameState.isMonsterVaseResolved) && !GameState.isBedRiddleResolved) {
      UiUtils.showDialog("Hehehe", "You found my Friends!", "Now you'll have to beat me!");
    }
  }
}
