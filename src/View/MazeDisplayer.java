package View;

import algorithms.mazeGenerators.Position;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by Mohsen Abdalla & Evgeny Umansky. June 2019.
 */
public class MazeDisplayer extends Canvas {


    //<editor-fold desc="Maze & CharacterPosition">
    private int[][] maze;

    public int [] [] getMaze(){
        return this.maze;
    }

    public void setMaze(int[][] maze, int exitRow, int exitColumn, ArrayList solutionPath, boolean isSolved) {
        this.maze = maze;
        this.exitPositionRow = exitRow;
        this.exitPositionColumn = exitColumn;
        this.solutionPath = solutionPath;
        this.isSolved = isSolved;
        redraw();
    }

    private int characterPositionRow;
    private int characterPositionColumn;

    private int exitPositionRow;
    private int exitPositionColumn;

    public void setCharacterPosition(int row, int column) {
        characterPositionRow = row;
        characterPositionColumn = column;
        redraw();
    }
    //</editor-fold>


    //<editor-fold desc="PropertyFromFxmlMazeDisplayer">
    private StringProperty ImageFileNameWall = new SimpleStringProperty();
    private StringProperty ImageFileNameCharacter = new SimpleStringProperty();
    private StringProperty ImageFileNameExit = new SimpleStringProperty();
    private StringProperty ImageFileNamePath = new SimpleStringProperty();
    private StringProperty ImageFileNameSolution = new SimpleStringProperty();

    public String getImageFileNameWall() {
        return ImageFileNameWall.get();
    }

    public void setImageFileNameWall(String imageFileNameWall) {
        this.ImageFileNameWall.set(imageFileNameWall);
    }

    public String getImageFileNameCharacter() {
        return ImageFileNameCharacter.get();
    }

    public void setImageFileNameCharacter(String imageFileNameCharacter) {
        this.ImageFileNameCharacter.set(imageFileNameCharacter);
    }

    public String getImageFileNameExit() {
        return ImageFileNameExit.get();
    }

    public void setImageFileNameExit(String ImageFileNameExit) {
        this.ImageFileNameExit.set(ImageFileNameExit);
    }

    public String getImageFileNamePath() {
        return ImageFileNamePath.get();
    }

    public void setImageFileNamePath(String imageFileNamePath) {
        this.ImageFileNamePath.set(imageFileNamePath);
    }


    public String getImageFileNameSolution() {
        return ImageFileNameSolution.get();
    }

    public void setImageFileNameSolution(String ImageFileNameSolution) {
        this.ImageFileNameSolution.set(ImageFileNameSolution);
    }
    //</editor-fold>



    //<editor-fold desc="Draw Maze">
    private ArrayList solutionPath;
    private boolean isSolved;

    public boolean isSolved() {
        return isSolved;
    }

    public void redraw() {
        if (maze != null) {
            double canvasHeight = getHeight();
            double canvasWidth = getWidth();
            double cellHeight = canvasHeight / maze.length;
            double cellWidth = canvasWidth / maze[0].length;

            try {
                Image wallImage = new Image(new FileInputStream(ImageFileNameWall.get()));
                Image characterImage = new Image(new FileInputStream(ImageFileNameCharacter.get()));
                Image exitImage = new Image(new FileInputStream(ImageFileNameExit.get()));
                Image solutionImage = new Image(new FileInputStream(ImageFileNameSolution.get()));

                GraphicsContext gc = getGraphicsContext2D();
                gc.clearRect(0, 0, getWidth(), getHeight());

                //Draw Maze

                for (int i = 0; i < maze.length; i++) {
                    for (int j = 0; j < maze[i].length; j++) {
                        if (maze[i][j] == 1) {
                            gc.drawImage(wallImage, j * cellHeight, i * cellWidth, cellHeight, cellWidth);
                        }
                    }
                }

                //Draw Solution path
                if(this.isSolved){
                    for(int i = 0; i < solutionPath.size(); i++){
                        Position position = (Position)solutionPath.get(i);
                        gc.drawImage(solutionImage, position.getColumnIndex() * cellHeight, position.getRowIndex() * cellWidth, cellHeight, cellWidth);
                    }

                }

                //Draw Exit
                gc.drawImage(exitImage, exitPositionColumn * cellHeight, exitPositionRow * cellWidth, cellHeight, cellWidth);
                //Draw Character
                gc.drawImage(characterImage, characterPositionColumn * cellHeight, characterPositionRow * cellWidth, cellHeight, cellWidth);

            } catch (FileNotFoundException e) {
                //e.printStackTrace();
            }
        }
    }
    //</editor-fold>
}
