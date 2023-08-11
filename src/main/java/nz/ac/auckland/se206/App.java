package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.controllers.ChatController;

/**
 * This is the entry point of the JavaFX application, while you can change this class, it should
 * remain as the class that runs the JavaFX application.
 */
public class App extends Application {

  private static Scene scene;

  private static ChatController chatInstance;

  public static ChatController getInstance() {
    if (chatInstance == null) {
      chatInstance = new ChatController();
    }
    return chatInstance;
  }

  public static void main(final String[] args) {
    launch();
  }

  public static void setRoot(String fxml) throws IOException {
    scene.setRoot(loadFxml(fxml));
  }

  /**
   * Returns the node associated to the input file. The method expects that the file is located in
   * "src/main/resources/fxml".
   *
   * @param fxml The name of the FXML file (without extension).
   * @return The node of the input file.
   * @throws IOException If the file is not found.
   */
  public static Parent loadFxml(final String fxml) throws IOException {
    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "Canvas" scene.
   *
   * @param stage The primary stage of the application.
   * @throws IOException If "src/main/resources/fxml/canvas.fxml" is not found.
   */
  @Override
  public void start(final Stage stage) throws IOException {

    SceneManager.addUi(SceneManager.AppUi.ROOM, loadFxml("room"));
    SceneManager.addUi(SceneManager.AppUi.RCS, loadFxml("rcs"));
    SceneManager.addUi(SceneManager.AppUi.CHAT, loadFxml("chat"));
    SceneManager.addUi(SceneManager.AppUi.MEMORY, loadFxml("memory"));
    SceneManager.addUi(SceneManager.AppUi.EXIT, loadFxml("exit"));

    scene = new Scene(SceneManager.getUiRoot(SceneManager.AppUi.ROOM), 600, 470);
    // Rename the title of image, to attribute the AdobeFirefly (-image generated by AdobeFirefly)
    Image icon = new Image("file:src/main/resources/images/imageIcon.png");
    stage.getIcons().add(icon);
    stage.setTitle("SOFTENG206 ESCAPE ROOM");
    stage.setResizable(false);
    stage.setScene(scene);
    stage.show();
  }

  public static void setScene(AppUi fxml) {
    scene.setRoot(SceneManager.getUiRoot(fxml));
  }

  public static void showGameOverDialog() {
    Platform.runLater(
        () -> {
          UiUtils.showDialog("Game Over", "Time's up!", "Thanks for playing the game!");
        });
  }
}
