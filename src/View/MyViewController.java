package View;
import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.control.Label;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

/**
 * Created by Mohsen Abdalla & Evgeny Umansky. June 2019.
 */

public class MyViewController extends Canvas implements Observer {

    //<editor-fold desc="Music">
    @FXML public ToggleButton musicB;
    static boolean musicIsOn = false;
    static Media menuTheme = new Media(MyViewController.class.getResource("/resources/Main_window/ThemeSong.mp3").toExternalForm());
    static MediaPlayer mplayer = new MediaPlayer(menuTheme);
    public void musicButton(ActionEvent actionEvent) {
        if (musicIsOn){
            mplayer.pause();
            musicIsOn = false;

        }else{
            mplayer.play();
            mplayer.setVolume(0.5);
            musicIsOn = true;
        }

    }

    public MediaPlayer getMplayer(){
        return mplayer;
    }
    //</editor-fold>


    //<editor-fold desc="Maze-MazeWindow">
    int rows;
    int columns;
    public javafx.scene.control.Button start_game_button;

    public void newMazeWindow(ActionEvent event) {
        mazeWindow(event, null);
    }

    private void mazeWindow(ActionEvent actionEvent, Maze maze){
        try {
            FXMLLoader fxmlLoader = null;
            mplayer.setAutoPlay(false);
            mplayer.pause();
            musicIsOn = false;

            if(tb1.isSelected()){
                fxmlLoader = new FXMLLoader(getClass().getResource("MazeKhalessi.fxml"));
                menuTheme = new Media(MyViewController.class.getResource("/resources/Khaleesi/KhalissiMP3.mp3").toExternalForm());
            }
            else if(tb2.isSelected()){
                fxmlLoader = new FXMLLoader(getClass().getResource("MazeJon.fxml"));
                menuTheme = new Media(MyViewController.class.getResource("/resources/Snow/JonMP3.mp3").toExternalForm());
            }
            else if(tb3.isSelected()){
                fxmlLoader = new FXMLLoader(getClass().getResource("MazeCersei.fxml"));
                menuTheme = new Media(MyViewController.class.getResource("/resources/Cersei/CerseiMP3.mp3").toExternalForm());
            }
            else if (tb4.isSelected()){
                fxmlLoader = new FXMLLoader(getClass().getResource("MazeStannis.fxml"));
                menuTheme = new Media(MyViewController.class.getResource("/resources/Stannis/StannissMP3.mp3").toExternalForm());
            }
            else if(fxmlLoader == null){
//                showAlert("You have to Choose your king to start the game of maze !");
                fxmlLoader = new FXMLLoader(getClass().getResource("MazeKhalessi.fxml"));
                menuTheme = new Media(MyViewController.class.getResource("/resources/Khaleesi/KhalissiMP3.mp3").toExternalForm());
            }
            mplayer = new MediaPlayer(menuTheme);
            mplayer.setAutoPlay(true);
            musicIsOn = true;
            Parent rootMaze = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Game of Mazes");
            Scene scene = new Scene(rootMaze,1000,700);
            stage.setScene(scene);

            MazeViewController view = fxmlLoader.getController();
            view.setResizeEvent(scene);
            view.setViewModel(viewModel);
            viewModel.addObserver(view);

            if(maze == null) {
                view.generateMaze(rows, columns);
            }
            else{
                view.displayMaze(maze);
            }

            SetStageCloseEvent(stage);
            stage.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void SetStageCloseEvent(Stage stage) {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                // viewModel.stopServers();
                musicIsOn = false;
                mplayer.pause();
                menuTheme = new Media(MyViewController.class.getResource("/resources/Main_window/ThemeSong.mp3").toExternalForm());
                mplayer = new MediaPlayer(menuTheme);

                start_game_button.setDisable(false);

            }
        });
    }
    //</editor-fold>


    //<editor-fold desc="KingChoice">
    @FXML public ToggleButton tb1;
    @FXML public ToggleButton tb2;
    @FXML public ToggleButton tb3;
    @FXML public ToggleButton tb4;
    @FXML public Label kingLabelText;

    public void radioButtonSelectKing(ActionEvent actionEvent) {
        Parent root = ((Node)actionEvent.getSource()).getParent();
        Font font = Font.loadFont(getClass().getResourceAsStream("/resources/Fonts/MasonRegular.ttf"), 16);

        kingLabelText.setStyle("-fx-text-fill: white");

        kingLabelText.setFont(font);
        if(tb1.isSelected()){
            kingLabelText.setText("Daenerys Targaryen, Queen of the Andals, Khaleesi of the Great Grass Sea, "
                    + "Breaker of Chains and Mother of Dragons");

        }
        else if(tb2.isSelected()){

            kingLabelText.setText("Jon Snow, King in the North, King Beyond the Wall,Lord of Winterfell, "+
                    "the White Wolf, the Undead");

        }
        else if(tb3.isSelected()){
            kingLabelText.setText("Cersei Lannister, Queen of the Andals and the First Men, "+
                    "Protector of the Seven Kingdoms" );
        }
        else if(tb4.isSelected()){
            kingLabelText.setText("Stannis Baratheon, King of the Andals and the First Men, " +
                    "Lord of the Seven Kingdoms, and Protector of the Realm");
        }
        else{
            kingLabelText.setText("");
        }
    }
    //</editor-fold>


    //<editor-fold desc="GameLevel">
    @FXML public TextField textField_mazeRows;
    @FXML public TextField textField_mazeColumns;

    public void normalLevel(ActionEvent actionEvent) {
        textField_mazeRows.clear();
        textField_mazeColumns.clear();
        textField_mazeColumns.appendText("10");
        textField_mazeRows.appendText("10");
    }

    public void hardLevel(ActionEvent actionEvent) {
        textField_mazeRows.clear();
        textField_mazeColumns.clear();
        textField_mazeColumns.appendText("50");
        textField_mazeRows.appendText("50");
    }

    public void AllMenMustDieLevel(ActionEvent actionEvent) {
        textField_mazeRows.clear();
        textField_mazeColumns.clear();
        textField_mazeColumns.appendText("100");
        textField_mazeRows.appendText("100");
    }
    //</editor-fold>


    //<editor-fold desc="ViewModel">
    @FXML private MyViewModel viewModel;

    @Override
    public void update(Observable o, Object arg) {
//        if (o == viewModel) {
//            displayMaze(viewModel.getMaze());
//            start_game_button.setDisable(false);
//        }
    }


    public void setViewModel(MyViewModel viewModel) {

        this.viewModel = viewModel;

        start_game_button.setOnAction(e ->{
            if(!textField_mazeRows.getText().matches("[0-9]*") ||
                    !textField_mazeColumns.getText().matches("[0-9]*")){
                showAlert("Enter a valid numbers !!!!");
                return;

            }
            setRowsAndColumnsByStartButton();
            mazeWindow(e, null);
                start_game_button.setDisable(true);
        });
    }

    private void setRowsAndColumnsByStartButton(){

        rows = Integer.valueOf(textField_mazeColumns.getText());
        columns = Integer.valueOf(textField_mazeRows.getText());
    }
    //</editor-fold>


    //<editor-fold desc="TopMenuBar-Functions">
    private void showAlert(String alertMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(alertMessage);
        alert.show();

    }


    public void loadMaze(ActionEvent actionEvent) {
        Maze maze = null;
        FileChooser fc = new FileChooser();
        fc.setTitle("Load maze");
        fc.setInitialDirectory(new File("src/sample"));
        fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text files",".maze"));

        File file = fc.showOpenDialog(new Stage());
        if(file != null) {
            maze = viewModel.load(file);
        }
        mazeWindow(actionEvent, maze);
    }

    public void exit(ActionEvent actionEvent) {
        Alert alert = new Alert(CONFIRMATION);
        alert.setContentText("Are you sure that you want to exit the game? Press OK to EXIT!");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.CANCEL) {
            return;
        }
        viewModel.exitProgram();
        Platform.exit();
    }

    public void algoInfo(ActionEvent actionEvent) {
        showAlert("We use BFS, DFS, BEST algorithms to find the solution path.\r\n" +
                "\r\nWe use the DFS algorithm to generate random mazes");
    }

    public void progInfo(ActionEvent actionEvent) {
        showAlert("We are The kings of Westeros:\r\n" + "Mohsen Abdalla & Evgeny Umansky");

    }

    public void gameInfo(ActionEvent actionEvent) {
        showAlert("- To start the game you must choose the king you want to see on the Iron Throne.\r\n" +
                "- If you don't choose one, you will be Khalissi!\r\n" +
                "- Choose the maze size.\r\n" +
                "- Press \"Let the game begin\" button to start the game\r\n" +
                "- By the way, if you wanna shut the music: click on the music button in the left bottom side of the window.");

    }


    public void propWindow(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Properties");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Properties.fxml").openStream());

            Label generator = (Label) root.lookup("#generator");
            Label threads = (Label) root.lookup("#threads");
            Label algo = (Label) root.lookup("#algo");

            Scene scene = new Scene(root, 400, 350);

            scene.getStylesheets().add(String.valueOf(getClass().getResource("Properties.css")));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes

            generator.setText("Maze Generator: " + viewModel.getProperty("IMazeGenerator"));
            threads.setText("Threads Number: " + viewModel.getProperty("threadsNum"));
            algo.setText("Solved by: " + viewModel.getProperty("ISearchingAlgorithm"));

            stage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

}
