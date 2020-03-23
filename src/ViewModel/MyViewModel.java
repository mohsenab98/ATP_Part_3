package ViewModel;

import Model.IModel;
import algorithms.mazeGenerators.Maze;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Mohsen Abdalla & Evgeny Umansky. June 2019.
 */

public class MyViewModel extends Observable implements Observer {

    public void stopServers() {
        model.stopServers();
    }
    public void startServers() {
        model.startServers();
    }
    //<editor-fold desc="Model">
    private IModel model;
    public MyViewModel(IModel model){
        this.model = model;
    }
    public void exitProgram(){
        model.exitProgram();
    }
    //</editor-fold>


    //<editor-fold desc="CharacterMovement">
    private int characterPositionRowIndex;
    private int characterPositionColumnIndex;

    public StringProperty characterPositionRow = new SimpleStringProperty("1"); //For Binding
    public StringProperty characterPositionColumn = new SimpleStringProperty("1"); //For Binding

    @Override
    public void update(Observable o, Object arg) {
        if (o==model){
            characterPositionRowIndex = model.getCharacterPositionRow();
            characterPositionRow.set(characterPositionRowIndex + "");
            characterPositionColumnIndex = model.getCharacterPositionColumn();
            characterPositionColumn.set(characterPositionColumnIndex + "");
            setChanged();
            notifyObservers();
        }
    }

    public void moveCharacter(KeyCode movement){
        model.moveCharacter(movement);
    }

    public int getCharacterPositionRow() {
        return characterPositionRowIndex;
    }

    public int getCharacterPositionColumn() {
        return characterPositionColumnIndex;
    }

    public void setXCharacterPos(double xCharPos){
        model.setXCharacterPos(xCharPos);
    }

    public void setYCharacterPos(double yCharPos){
        model.setYCharacterPos(yCharPos);
    }

    //</editor-fold>


    //<editor-fold desc="MazeFunctions">
    public void save(File file) {
        model.save(file);
    }

    public Maze load(File file) {
        return model.load(file);
    }

    public void generateMaze(int width, int height){
        model.generateMaze(width, height);
    }

    public void solveMaze(){
        model.solveMaze();
    }

    public ArrayList getSolutionPath(){
        return model.getSolutionPath();
    }


    public Maze getMaze() {
        return model.getMaze();
    }
    //</editor-fold>


    //<editor-fold desc="ConfigurationProperty">
    public  String getProperty(String property){
        return model.getProperty(property);
    }
    //</editor-fold>


    //<editor-fold desc="MouseFunctions">

    public boolean mouseAction(double mazeDisHight,double mazeRowNum,double mazeDisWidth, double mazeColNum, double xPos, double yPos) {
        return model.mouseAction(mazeDisHight, mazeRowNum, mazeDisWidth, mazeColNum , xPos, yPos); }

    //</editor-fold>

    }
