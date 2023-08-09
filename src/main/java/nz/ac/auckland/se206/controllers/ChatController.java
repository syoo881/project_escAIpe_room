package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.UiUtils;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

/** Controller class for the chat view. */
public class ChatController {
  @FXML private TextArea chatTextArea;
  @FXML private TextField inputText;
  @FXML private Button sendButton;

  private ChatCompletionRequest chatCompletionRequest;

  private static boolean isMonsterVaseRiddle = false;
  private boolean isMonsterBedRiddle = false;

  // ... (other methods and initializations)

  public static void startMonsterVaseRiddle() {
    isMonsterVaseRiddle = true;
    UiUtils.showDialog(
        "?!!!", "Hey!", "Don't touch me I'm not a monster! What do you even think I am!");
    App.setScene(AppUi.CHAT);
  }

  public void startMonsterBedRiddle() {
    isMonsterBedRiddle = true;
  }

  /**
   * Initializes the chat view, loading the riddle.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML

  // The GPT model is initialized with a riddle prompt.
  // It calls in the getRiddleWithGivenWord method from the GptPromptEngineering class.
  // It is currently hard coded to use the word "vase" as the riddle word.
  public void initialize() throws ApiProxyException {

    // here, I think we put an if statement to decide what riddlewithGivenWord we give through to
    // GPT
    // Based off what the user clicked.
    // USe threading here. Put these in background tasks.

    Task<Void> gptTask =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {
            chatCompletionRequest =
                new ChatCompletionRequest()
                    .setN(1)
                    .setTemperature(0.2)
                    .setTopP(0.5)
                    .setMaxTokens(100);
            runGpt(new ChatMessage("user", GptPromptEngineering.getRiddleWithGivenWord("vase")));
            return null;
          }
        };
    Thread gptThread = new Thread(gptTask);
    gptThread.start();
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    chatTextArea.appendText(msg.getRole() + ": " + msg.getContent() + "\n\n");
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    try {
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());
      appendChatMessage(result.getChatMessage());
      return result.getChatMessage();
    } catch (ApiProxyException e) {
      // TODO handle exception appropriately
      // Add a popuperror message when no internet connection
      // Maybe add like "Try again later"
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Sends a message to the GPT model.
   *
   * @param event the action event triggered by the send button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {
    String message = inputText.getText();
    if (message.trim().isEmpty()) {
      return;
    }
    inputText.clear();
    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);

    Task<Void> backgroundTask =
        new Task<Void>() {

          @Override
          protected Void call() throws ApiProxyException {

            ChatMessage lastMsg = runGpt(msg);

            Platform.runLater(
                () -> {
                  if (lastMsg.getRole().equals("assistant")
                      && lastMsg.getContent().startsWith("Correct")) {
                    GameState.isMonsterVaseRiddleResolved = true;
                  }
                });
            return null;
          }
        };

    // This thread thing might not be needed, but I'm not sure.
    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.start();
  }

  /**
   * Navigates back to the previous view.
   *
   * @param event the action event triggered by the go back button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {

    App.setScene(AppUi.ROOM);
  }
}
