package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.UiUtils;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

public class VaseGameController {
  private static final String PAPER = "paper";
  private static final String ROCK = "rock";
  private static final String SCISSORS = "scissors";
  private Image image;

  @FXML private Label cadeScore;

  @FXML private ImageView computer;

  @FXML private Button paperButton;

  @FXML private ImageView player;

  @FXML private Label result;

  @FXML private Button rockButton;

  @FXML private Button scissorButton;

  @FXML private Label userScore;

  @FXML
  private void playerTurn(ActionEvent event) {
    //In the players turn, they can choose one of the options and it sets the playerChoice String.
    String playerChoice = null;
    switch (((Button) event.getSource()).getId()) {
      case "paperButton":
        image = new Image(getClass().getResourceAsStream("/images/paper.png"));
        playerChoice = PAPER;
        break;
      case "rockButton":
        image = new Image(getClass().getResourceAsStream("/images/rock.png"));
        playerChoice = ROCK;
        break;
      case "scissorButton":
        image = new Image(getClass().getResourceAsStream("/images/scissor.png"));
        playerChoice = SCISSORS;
        break;
    }
    player.setImage(image);

    chooseWinner(playerChoice, computerTurn());
  }

  @FXML
  //Setting the random number so the computer chooses randomly
  private String computerTurn() {
    String computerChoice = null;
    int index = (int) (Math.random() * 3);
    switch (index) {
      case 0:
        image = new Image(getClass().getResourceAsStream("/images/paper.png"));
        computerChoice = PAPER;
        break;
      case 1:
        image = new Image(getClass().getResourceAsStream("/images/rock.png"));
        computerChoice = ROCK;
        break;
      case 2:
        image = new Image(getClass().getResourceAsStream("/images/scissor.png"));
        computerChoice = SCISSORS;
        break;
    }
    computer.setImage(image);
    return computerChoice;
  }

  public void playerWin() {
    //Setting the condition for when the player wins
    result.setText("You Win");
    userScore.setText(String.valueOf(Integer.parseInt(userScore.getText()) + 1));
    if (Integer.parseInt(userScore.getText()) == 1) {
      result.setText("You Win the Game");
      userScore.setText("0");
      cadeScore.setText("0");
      GameState.isMonsterVaseResolved = true;
      UiUtils.showDialog("WOW!", "Awww man!", "You won! My name is Vanessa");
      App.setScene(AppUi.ROOM);
    }
  }

  public void computerWin() {
    //Setting the condition for when the computer wins
    result.setText("You Lose");
    cadeScore.setText(String.valueOf(Integer.parseInt(cadeScore.getText()) + 1));
    if (Integer.parseInt(cadeScore.getText()) == 1) {
      result.setText("You Lose the Game");
      UiUtils.showDialog("Haha!", "You suck at this game!", "Cmon GO again!");
      userScore.setText("0");
      cadeScore.setText("0");
    }
  }

  public void draw() {
    result.setText("Draw");
  }

  private void chooseWinner(String playerChoice, String computerChoice) {
    //Choose winner of the game
    if (playerChoice.equals(computerChoice)) {
      draw();
    } else if (playerChoice.equals(PAPER) && computerChoice.equals(ROCK)) {
      playerWin();
    } else if (playerChoice.equals(ROCK) && computerChoice.equals(SCISSORS)) {
      playerWin();
    } else if (playerChoice.equals(SCISSORS) && computerChoice.equals(PAPER)) {
      playerWin();
    } else {
      computerWin();
    }
  }

  @FXML
  public void initialize() throws ApiProxyException {}
}
