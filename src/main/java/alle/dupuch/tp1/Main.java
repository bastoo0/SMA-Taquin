package alle.dupuch.tp1;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends Application {
    public GridPane drawnGrid;
    private Pane [][] gridInMemory;

    public void start(Stage stage) throws IOException {
        BorderPane root = FXMLLoader.load (getClass ().getResource("user-interface.fxml"));
        drawnGrid = (GridPane) ((VBox) root.getLeft()).getChildren ().get (1); // à mettre dans des getters / setters
        Scene scene = new Scene(root, 320, 240);
        stage.setTitle("Taquin - ALLE DUPUCH");
        stage.setScene(scene);

        var env = new Environment (5, 5, 5);
        initializeView (env);
        var t = new Thread (env);
        t.start ();
        env.getAgentList()
                .stream ()
                .forEach (agent -> {
                    (new Thread (agent)).start ();
                });

        updateView (env);
        stage.show();
        /*while (true) {
            try {
                Thread.sleep (100000);
                //updateView (env);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        Timeline timeline =
                new Timeline(new KeyFrame(Duration.millis(1000), e -> {updateView (env);}));
        timeline.setCycleCount(Animation.INDEFINITE); // loop forever
        timeline.play();
    }

    public void initializeView (Environment environment) {
        // On est obligés d'avoir une 2e grille en mémoire car pour accéder à
        // une cellule d'un GridPane, il faut reparcourir toute la grille.
        gridInMemory = new Pane [environment.getWidth()][];
        for (int x = 0; x < environment.getWidth(); ++x) {
            gridInMemory [x] = new Pane [environment.getHeight ()];
            for (int y = 0; y < environment.getHeight (); ++y) {
                Pane drawnSquare = new StackPane();
                Text textSquare = new Text (" ");

                drawnSquare.getStyleClass().add ("square");
                System.out.println (drawnSquare.getStyleClass ());
                drawnSquare.getChildren ().add (textSquare);
                textSquare.getStyleClass().add ("text-square");
                drawnGrid.add (drawnSquare, x, y);
                gridInMemory [x][y] = drawnSquare;
            }
        }
    }

    //définir taux de rafraichissement
    public void updateView (Environment environment) {
        Set<List <Integer>> agentPositions = new HashSet<>();
        List <Agent> agentsToLock = environment.getAgentList ();
        while (!agentsToLock.isEmpty()) {
            agentsToLock.removeIf (Agent::finishCurrentMove);
        }

        for (Agent agent: environment.getAgentList ()) {
            // Modifier les accesseurs de currentX et currentY
            var agentPosition = List.of (agent.currentX, agent.currentY);
            agentPositions.add (agentPosition);

            var finalPosition = List.of (agent.getFinalX (), agent.getFinalY ());
            String squareBackground = agentPosition.equals (finalPosition)? "right-position": "wrong-position";
            Pane drawnSquare = gridInMemory [agent.currentX][agent.currentY];
            drawnSquare.getStyleClass().removeAll ("right-position", "wrong-position");
            drawnSquare.getStyleClass().add (squareBackground);
            Text text = (Text) drawnSquare.getChildren().get (0);
            text.setText (agent.toString ());
        }

        for (int x = 0; x < environment.getWidth(); ++x) {
            for (int y = 0; y < environment.getHeight(); ++y) {
                var currentPosition = List.of(x, y);
                if (!agentPositions.contains (currentPosition)) {
                    Pane drawnSquare = gridInMemory [x][y];
                    drawnSquare.getStyleClass().removeAll ("right-position", "wrong-position");
                    Text text = (Text) drawnSquare.getChildren().get (0);
                    text.setText (" ");
                }
            }
        }
        environment.getAgentList().stream ().forEach (Agent::allowMove);
    }

    public static void main(String[] args) {
        launch();
    }

    // Ne pas oublier d'arrêter proprement l'application
}