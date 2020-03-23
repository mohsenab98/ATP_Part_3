package Model;

import algorithms.mazeGenerators.Maze;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.ArrayList;
/**
 * Created by Mohsen Abdalla & Evgeny Umansky. June 2019.
 */

public interface IModel {
    void generateMaze(int width, int height);
    void solveMaze();
    ArrayList getSolutionPath();
    void moveCharacter(KeyCode movement);
    Maze getMaze();
    int getCharacterPositionRow();
    int getCharacterPositionColumn();
    String getProperty(String property);
    void exitProgram();
    void save(File file);
    Maze load(File file);
    //Resize
    void setXCharacterPos(double xCharPos);
    void setYCharacterPos(double yCharPos);
    void startServers();
    void stopServers();

    boolean mouseAction(double mazeDisHight,double mazeRowNum,double mazeDisWidth, double mazeColNum, double xPos, double yPos);
}
