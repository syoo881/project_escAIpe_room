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

  // ... (other methods and initializations)

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
    System.out.println("Initialiszed");
    choosePrompt();
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    chatTextArea.appendText(msg.getRole() + ": " + msg.getContent() + "\n\n");
  }

  public void choosePrompt() {

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
            if (GameState.isMonsterVaseClicked) {
              runGpt(new ChatMessage("user", GptPromptEngineering.getRiddleWithGivenWord("vase")));
            } else if (GameState.isMonsterBedClicked) {
              runGpt(new ChatMessage("user", GptPromptEngineering.getRiddleWithGivenWord("bed")));
            } else {
              runGpt(new ChatMessage("user", "Hello"));
            }
            return null;
          }
        };
    Thread gptThread = new Thread(gptTask);
    gptThread.start();
  }

  public ChatMessage getGptGeneratedPrompt() throws ApiProxyException {
    ChatMessage prompt =
        new ChatMessage(
            "user",
            "In 6 words or less, give clues about vases, bedrooms and frames, in random order."
                + " Never say these words - just hints.");
    return runGpt(prompt); // Modify runGpt method as needed
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    Task<Void> gptTask =
        new Task<Void>() {
          @Override
          protected Void call() throws ApiProxyException {
            chatCompletionRequest.addMessage(msg);
            try {
              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
              Choice result = chatCompletionResult.getChoices().iterator().next();
              chatCompletionRequest.addMessage(result.getChatMessage());
              appendChatMessage(result.getChatMessage());

              Platform.runLater(
                  () -> {
                    if (result.getChatMessage().getRole().equals("assistant")
                        && result.getChatMessage().getContent().startsWith("Correct")) {
                      if (GameState.isMonsterVaseClicked) {
                        GameState.isMonsterVaseRiddleResolved = true;
                      } else if (GameState.isMonsterBedClicked) {
                        GameState.isMonsterBedRiddleResolved = true;
                      }
                    }
                  });
            } catch (ApiProxyException e) {
              e.printStackTrace();
              // Handle the exception appropriately
            }
            return null;
          }
        };

    Thread gptThread = new Thread(gptTask);
    gptThread.start();
    return msg;
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
                    if (GameState.isMonsterVaseClicked) {
                      GameState.isMonsterVaseRiddleResolved = true;
                    } else if (GameState.isMonsterBedClicked) {
                      GameState.isMonsterBedRiddleResolved = true;
                    }
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

    if (GameState.isMonsterVaseClicked) {
      GameState.isMonsterVaseClicked = false;
    }

    if (GameState.isMonsterBedClicked) {
      GameState.isMonsterBedClicked = false;
    }
    App.setScene(AppUi.ROOM);
  }
}
