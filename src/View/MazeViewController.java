package View;

import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Mohsen Abdalla & Evgeny Umansky. June 2019.
 */

public class MazeViewController extends Canvas implements IView, Observer {

    //<editor-fold desc="ViewModel & ScrollMaze">

    @FXML private MyViewModel viewModel;
    public javafx.scene.control.ScrollPane scroll_bar;
    boolean isSolved;
    static boolean hasWonTheGame = false;

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;

        solve.setOnAction(e -> {
            solveMaze();
            isSolved = true;
        });

        scroll_bar.addEventFilter(ScrollEvent.SCROLL, event -> {
            if(event.isControlDown())
            {
                scrollMaze(event);
                event.consume();
            }
        });

    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            displayMaze(viewModel.getMaze());
        }
    }

    public void exit(ActionEvent actionEvent) {
        viewModel.exitProgram();
        Platform.exit();
    }

    public void scrollMaze(ScrollEvent scrollEvent){
        if(scrollEvent.isControlDown()){
            mazeDisplayer.setHeight(mazeDisplayer.getHeight() + scrollEvent.getDeltaY());
            mazeDisplayer.setWidth(mazeDisplayer.getWidth() + scrollEvent.getDeltaY());
            mazeDisplayer.redraw();
        }

        scrollEvent.consume();
    }
    //</editor-fold>


    //<editor-fold desc="mazeDisplayer">
    @FXML public MazeDisplayer mazeDisplayer;
    public void displayMaze(Maze maze) {
        hasWonTheGame = false;
        mazeDisplayer.setMaze(maze.getMaze(), maze.getGoalPosition().getRowIndex(), maze.getGoalPosition().getColumnIndex(), viewModel.getSolutionPath(), isSolved);
        int characterPositionRow = viewModel.getCharacterPositionRow();
        int characterPositionColumn = viewModel.getCharacterPositionColumn();

        if(characterPositionRow == maze.getGoalPosition().getRowIndex() && characterPositionColumn == maze.getGoalPosition().getColumnIndex()){
            ActionEvent event = new ActionEvent();
            winWindow(event);
        }

        mazeDisplayer.setCharacterPosition(characterPositionRow, characterPositionColumn);
        this.characterPositionRow.set(characterPositionRow + "");
        this.characterPositionColumn.set(characterPositionColumn + "");
    }
    //</editor-fold>


    //<editor-fold desc="WinWindow">
    static boolean isWinWindow = false;
    static Media menuTheme = new Media(MyViewController.class.getResource("/resources/Win_window/WinMP3.mp3").toExternalForm());
    static MediaPlayer mplayer = new MediaPlayer(menuTheme);

    private void winWindow(ActionEvent actionEvent){
        if(!isWinWindow) {
            isWinWindow = true;
            solve.setDisable(true);
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Win.fxml"));
                Parent rootMaze = fxmlLoader.load();
                Stage stage = new Stage();

                stage.setTitle("Game of Mazes");
                Scene scene = new Scene(rootMaze, 600, 400);
                stage.setScene(scene);
                MyViewController.musicIsOn = false;
                MyViewController.mplayer.pause();
                mplayer.play();
                SetStageCloseEvent(stage);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void SetStageCloseEvent(Stage stage) {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                mplayer.stop();
                hasWonTheGame = true;
                isWinWindow = false;
            }
        });
    }
    //</editor-fold>


    //<editor-fold desc="Maze-Functions">
    int rows;
    int columns;

    public void generateMaze(int rows, int columns) {
        solve.setDisable(false);
        this.rows = rows;
        this.columns = columns;
        viewModel.generateMaze(rows, columns);
    }

    public void newGenerate(ActionEvent actionEvent) {
        generateMaze(this.rows, this.columns);
    }

    public void saveMaze(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("src/sample"));
        fc.setTitle("Saving the maze");
        FileChooser.ExtensionFilter ef = new FileChooser.ExtensionFilter("Text files", ".maze");
        fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text files", ".maze"));
        File file = fc.showSaveDialog((Stage) mazeDisplayer.getScene().getWindow());
        if(file != null)
            viewModel.save(file);
    }


    @FXML private javafx.scene.control.Button solve;

    public void solveMaze(){
        solve.setDisable(true);
        viewModel.solveMaze();
         mazeDisplayer.setFocusTraversable(true);
         mazeDisplayer.setOnKeyPressed(this::KeyPressed);
    }
    //</editor-fold>


    //<editor-fold desc="KeyPressedCharacterOnMaze">
    public void KeyPressed(KeyEvent keyEvent) {
        if(!hasWonTheGame){
            viewModel.moveCharacter(keyEvent.getCode());
            keyEvent.consume();
        }
    }

    private StringProperty characterPositionRow = new SimpleStringProperty();

    private StringProperty characterPositionColumn = new SimpleStringProperty();
    //</editor-fold>


    //<editor-fold desc="ResizeEvent">
    public BorderPane mazeBoard;
    public void setResizeEvent(Scene scene) {
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                mazeBoard.setPrefWidth(newSceneWidth.longValue()*0.9);
                mazeDisplayer.setWidth(mazeBoard.getPrefWidth());
                viewModel.setXCharacterPos(viewModel.getCharacterPositionColumn()*mazeDisplayer.getWidth()/mazeDisplayer.getMaze()[0].length);
                mazeDisplayer.redraw();
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                mazeBoard.setPrefHeight(newSceneHeight.longValue()*0.9);
                mazeDisplayer.setHeight(mazeBoard.getPrefHeight());
                viewModel.setYCharacterPos(viewModel.getCharacterPositionRow()*mazeDisplayer.getHeight()/mazeDisplayer.getMaze().length);
                mazeDisplayer.redraw();
            }
        });
    }
    //</editor-fold>


    //<editor-fold desc="AlertFunctions">
    private void showAlert(String alertMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(alertMessage);
        alert.show();

    }

    public void SolveButton(ActionEvent actionEvent) {
        showAlert("Press \"Show me road to the Iron Throne\" button to show solution");

    }

    public void GameRules(ActionEvent actionEvent) {
        showAlert("You have to find the path to the Iron Throne throw the maze.\r\n" +
                "\r\nUse the arrows, WASD or NUMs buttons to move around the maze.");

    }

    public void algoInfo(ActionEvent actionEvent) {
        showAlert("We use BFS, DFS, BEST algorithms to find the solution path.\r\n" +
                "\r\nWe use the DFS algorithm to generate random mazes");
    }

    public void progInfo(ActionEvent actionEvent) {
        showAlert("We are The kings of Westeros:\r\n" + "Mohsen Abdalla & Evgeny Umansky");
    }
    //</editor-fold>


    //<editor-fold desc="MouseFunctions">

    public void mouseDrag(MouseEvent me)
    {
        if(!hasWonTheGame) {
            if (viewModel.mouseAction(mazeDisplayer.getHeight() , viewModel.getMaze().getMaze().length,
                    mazeDisplayer.getWidth() , viewModel.getMaze().getMaze()[0].length, me.getX(), me.getY())) {
                isSolved = false;

            }else{
                showAlert("Ohhh you think you can go throw a wall?! Think again: you can't!");
            }

        }
    }

    public void mouseReleased(MouseEvent me)
    {
        if(!hasWonTheGame) {
            viewModel.mouseAction(mazeDisplayer.getHeight() , viewModel.getMaze().getMaze().length,
                    mazeDisplayer.getWidth() , viewModel.getMaze().getMaze()[0].length, me.getX(), me.getY());
//            me.consume();
        }
    }
    //</editor-fold>
}
