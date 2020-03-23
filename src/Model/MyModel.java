package Model;

//<editor-fold desc="Imports">
import Client.Client;
import Client.IClientStrategy;
import IO.MyCompressorOutputStream;
import IO.MyDecompressorInputStream;
import Server.Server;
import Server.ServerStrategyGenerateMaze;
import Server.ServerStrategySolveSearchProblem;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.scene.input.KeyCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//</editor-fold>

/**
 * Created by Mohsen Abdalla & Evgeny Umansky. June 2019.
 */

public class MyModel  extends Observable implements IModel {
    private static final Logger LOGGER = LogManager.getLogger();

    public MyModel() {
        mazeGeneratingServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
        solveSearchProblemServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
        solutionPath = new ArrayList<>();
    }


    //<editor-fold desc="Servers">
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private Server mazeGeneratingServer;
    private Server solveSearchProblemServer;

    public void startServers() {
        mazeGeneratingServer.start();
        solveSearchProblemServer.start();
    }

    public void stopServers() {
        mazeGeneratingServer.stop();
        solveSearchProblemServer.stop();
    }

    public void exitProgram(){
        stopServers();
        threadPool.shutdown();
    }
    //</editor-fold>


    //<editor-fold desc="Save-Load">
    @Override
    public void save(File file) {
        try {
            FileOutputStream newFile = new FileOutputStream(file);
            OutputStream out = new MyCompressorOutputStream(newFile);
            maze.setStart(characterPositionRow, characterPositionColumn);
            out.write(maze.toByteArray());
            out.flush();
            out.close();
        } catch (IOException ex) {
            System.out.println("IOException");
        }
    }

    @Override
    public Maze load(File file) {
        byte mazeBytesTemp[] = new byte[0];
        try {
            FileInputStream fis = new FileInputStream(file.getPath());
            FileInputStream temp = new FileInputStream(file.getPath());
            InputStream in = new MyDecompressorInputStream(fis);

            mazeBytesTemp = new byte[temp.read()*temp.read()];
            in.read(mazeBytesTemp);
            in.close();
            fis.close();
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException");
        }
        catch (IOException e)
        {
            System.out.println("IOException");
        }

        int counter = 0;
        for(int i = mazeBytesTemp.length - 1; i >= 0; i--){
            if(mazeBytesTemp[i] == -1){
                counter++;
            }
            else {
                break;
            }
        }

        byte mazeBytes[] = new byte[mazeBytesTemp.length - counter];

        for(int i = 0; i < mazeBytes.length; i++){
            mazeBytes[i] = mazeBytesTemp[i];
        }

        maze  = new Maze(mazeBytes);

        characterPositionRow = maze.getStartPosition().getRowIndex();
        characterPositionColumn = maze.getStartPosition().getColumnIndex();

        return maze;
    }
    //</editor-fold>


    //<editor-fold desc="Maze-Generate">
    private Maze maze;
    @Override
    public Maze getMaze() {
        return maze;
    }
    @Override
    public void generateMaze(int rows, int columns) {
        // Generate maze

        threadPool.execute( () -> {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                    public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                        try {
                            solutionPath.clear();
                            maze = null;
                            characterPositionRow = 0;
                            characterPositionColumn = 0;

                            LOGGER.info("GenerateMaze client is started!");
                            LOGGER.info(String.format("GenerateMaze's port is %d", 5400));

                            ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                            ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                            toServer.flush();
                            int[] mazeDimensions = new int[]{rows, columns};
                            toServer.writeObject(mazeDimensions);
                            toServer.flush();
                            byte[] compressedMaze = (byte[]) ((byte[]) fromServer.readObject());
                            InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                            byte[] decompressedMaze = new byte[compressedMaze.length];
                            is.read(decompressedMaze);
                            maze = new Maze(decompressedMaze);

                            characterPositionRow = maze.getStartPosition().getRowIndex();
                            characterPositionColumn = maze.getStartPosition().getColumnIndex();

                            LOGGER.info(String.format("Maze's size: %d X %d", rows, columns));
                            LOGGER.info(String.format("Start position (%d, %d) ", maze.getStartPosition().getRowIndex(), maze.getStartPosition().getColumnIndex()));
                            LOGGER.info(String.format("Goal position (%d, %d) ", maze.getGoalPosition().getRowIndex(), maze.getGoalPosition().getColumnIndex()));

//                            maze.print();
                        } catch (Exception var10) {
                            var10.printStackTrace();
                            LOGGER.error("Exception: GenerateMaze client strategy cannot be created", var10);
                        }

                    }
                });
                client.communicateWithServer();
            } catch (UnknownHostException var1) {
                var1.printStackTrace();
                LOGGER.error("UnknownHostException: GenerateMaze client does not created", var1);
            }

            setChanged();
            notifyObservers();
        });
    }
    //</editor-fold>


    //<editor-fold desc="SolveMaze">
    private ArrayList<Position> solutionPath;
    public void solveMaze() {
       threadPool.execute(() -> {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                    public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                        try {
                            LOGGER.info("SolveMaze client is started!");
                            LOGGER.info(String.format("SolveMaze's port is %d", 5401));

                            String solveAlgo = getProperty("ISearchingAlgorithm");

                            if(!solveAlgo.equals("DepthFirstSearch") && !solveAlgo.equals("BestFirstSearch") && !solveAlgo.equals("BreadthFirstSearch")){
                                solveAlgo = "DepthFirstSearch";
                            }

                            ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                            ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                            toServer.flush();
                            // maze.print();
                            toServer.writeObject(maze);
                            toServer.flush();
                            Solution mazeSolution = (Solution) fromServer.readObject();
                            System.out.println(String.format("Solution steps: %s", mazeSolution));
                            ArrayList<AState> mazeSolutionSteps = mazeSolution.getSolutionPath();

                            LOGGER.info(String.format("The solving algorithm is: %s", solveAlgo));

                            for (int i = 0; i < mazeSolutionSteps.size(); ++i) {
                                AState state = mazeSolutionSteps.get(i);
                                Position pos = (Position) state.getState();

                                LOGGER.info(String.format("Step %d: (%d, %d)", i, pos.getRowIndex(), pos.getColumnIndex()));

                                solutionPath.add(pos);
                            }
                        } catch (Exception var10) {
                            var10.printStackTrace();
                            LOGGER.error("Exception: SolveMaze client strategy cannot be created", var10);
                        }

                    }
                });
                client.communicateWithServer();
            } catch (UnknownHostException var1) {
                var1.printStackTrace();
                LOGGER.error("UnknownHostException: SolveMaze client does not created", var1);
            }

            setChanged();
            notifyObservers();
        });
    }

    public ArrayList getSolutionPath(){
        return solutionPath;
    }
    //</editor-fold>


    //<editor-fold desc="Character-Functions">
    private int characterPositionRow;
    private int characterPositionColumn;
    private double xCharacterPos;
    private double yCharacterPos;

    @Override
    public int getCharacterPositionRow() {
        return characterPositionRow;
    }

    @Override
    public int getCharacterPositionColumn() {
        return characterPositionColumn;
    }

    @Override
    public void setXCharacterPos(double xCharPos){
        this.xCharacterPos = xCharPos;
    }
    @Override
    public void setYCharacterPos(double yCharPos){
        this.yCharacterPos = yCharPos;
    }

    @Override
    public void moveCharacter(KeyCode movement) {
        switch (movement) {
            case NUMPAD8:
            case UP:
            case W:
                if(characterPositionRow - 1 >= 0 && maze.getMaze()[characterPositionRow - 1][characterPositionColumn] == 0){
                    characterPositionRow--;
                }
                break;
            case NUMPAD2:
            case DOWN:
            case S:
                if(characterPositionRow + 1 < maze.getMaze().length && maze.getMaze()[characterPositionRow + 1][characterPositionColumn] == 0){
                    characterPositionRow++;
                }
                break;
            case NUMPAD6:
            case RIGHT:
            case D:
                if(characterPositionColumn + 1 < maze.getMaze()[characterPositionRow].length && maze.getMaze()[characterPositionRow][characterPositionColumn + 1] == 0) {
                    characterPositionColumn++;
                }
                break;
            case NUMPAD4:
            case LEFT:
            case A:
                if(characterPositionColumn - 1 >= 0 && maze.getMaze()[characterPositionRow][characterPositionColumn - 1] == 0) {
                    characterPositionColumn--;
                }
                break;
            // Diagonal
            case NUMPAD1:
            case Z:
                if(characterPositionRow + 1 <  maze.getMaze().length && characterPositionColumn - 1 >= 0
                        && maze.getMaze()[characterPositionRow + 1][characterPositionColumn - 1] == 0){
                    characterPositionColumn--;
                    characterPositionRow++;
                }
                break;
            case NUMPAD3:
            case C:
                if(characterPositionRow + 1 <  maze.getMaze().length && characterPositionColumn + 1 < maze.getMaze()[characterPositionRow].length
                        && maze.getMaze()[characterPositionRow + 1][characterPositionColumn + 1] == 0){
                    characterPositionColumn++;
                    characterPositionRow++;
                }
                break;
            case NUMPAD7:
            case Q:
                if(characterPositionRow - 1 >= 0 && characterPositionColumn - 1 >= 0 && maze.getMaze()[characterPositionRow - 1][characterPositionColumn - 1] == 0){
                    characterPositionColumn--;
                    characterPositionRow--;
                }
                break;
            case NUMPAD9:
            case E:
                if(characterPositionRow - 1 >= 0 && characterPositionColumn + 1 < maze.getMaze()[characterPositionRow].length
                        && maze.getMaze()[characterPositionRow - 1][characterPositionColumn + 1] == 0){
                    characterPositionColumn++;
                    characterPositionRow--;
                }
                break;
        }
        setChanged();
        notifyObservers();
    }
    //</editor-fold>


    //<editor-fold desc="Configuration-Property">
    private Properties prop;
    public String getProperty(String property){
        InputStream input = ServerStrategyGenerateMaze.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            this.prop = new Properties();
            this.prop.load(input);
        }
        catch (Throwable var19) {
            var19.printStackTrace();
        }
        property = this.prop.getProperty(property);

        return property;
    }
    //</editor-fold>


    //<editor-fold desc="MouseFunctions">

    public boolean mouseAction(double mazeDisHight, double mazeRowNum, double mazeDisWidth, double mazeColNum, double xPos, double yPos){
        // use the maze cell hight/width value to be sure if we can go throw the maze path we want
        double mazeCellHight = mazeDisHight / mazeRowNum;
        double mazeCellWidth = mazeDisWidth / mazeColNum;
        boolean xLegalMove = false;
        int newXPos = -1 , newYPos = -1;
        boolean yLegalMove = false;

        // check the x/y position is in a safe area in the maze displayer if it is save the wanted indexes
        for(int i = 0; i < this.maze.getMaze().length; i++)
        {
            if(newXPos == -1 && yPos >= i*mazeCellHight && yPos <= (i*mazeCellHight + mazeCellHight))
            {
                xLegalMove = true;
                newXPos = i;

            }
            if(newYPos == -1 && xPos >= i*mazeCellWidth && xPos <= (i*mazeCellWidth + mazeCellWidth))
            {
                yLegalMove = true;
                newYPos = i;

            }
        }
        // check if the relased / dragged position is in outside of the maze or on a wall block
        int newRowindex =(int) (yPos / mazeCellHight);
        int newColindex =(int) (xPos / mazeCellWidth);

        if(newXPos >= this.maze.getMaze().length || newYPos >= this.maze.getMaze()[0].length ||
                newXPos < 0 || newYPos < 0 ||
                this.maze.getMaze()[newXPos][newYPos] == 1 ||
                Math.abs(newRowindex - characterPositionRow) > 1 ||
                Math.abs(newColindex - characterPositionColumn) > 1){
            return false;
        }

        // if the wanted position is legal update the character position accordingly
        else if(xLegalMove && yLegalMove) {
            this.xCharacterPos = xPos;
            this.yCharacterPos = yPos;
            this.characterPositionRow = newXPos;
            this.characterPositionColumn = newYPos;
            setChanged();
            notifyObservers();
            return true;
        }
        return false;
    }
    //</editor-fold>
}
