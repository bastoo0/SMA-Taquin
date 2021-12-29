package alle.dupuch.tp1;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    private Environment environment;
    @FXML private GridPane currentGrid;
    @FXML private GridPane finalGrid;
    private Pane[][] currentGridInMemory;
    private Pane[][] finalGridInMemory;

    public Controller (Environment environment) {
        this.environment = environment;
    }

    public void initialize (URL url, ResourceBundle resourceBundle) {
        currentGridInMemory = new Pane [environment.getWidth()][];
        finalGridInMemory = new Pane [environment.getWidth()][];
        drawGrid (currentGrid, currentGridInMemory);
        drawGrid (finalGrid, finalGridInMemory);
    }

    public void drawGrid (GridPane grid, Pane [][] gridInMemory) {
        // On est obligés d'avoir une 2e grille en mémoire car pour accéder à
        // une cellule d'un GridPane, il faut reparcourir toute la grille.
        for (int x = 0; x < environment.getWidth(); ++x) {
            gridInMemory [x] = new Pane [environment.getHeight ()];
            // On définit la largeur des colonnes
            ColumnConstraints columnWidth = new ColumnConstraints(25);
            grid.getColumnConstraints().add(columnWidth);

            for (int y = 0; y < environment.getHeight (); ++y) {
                // Chaque cellule de la grille est un StackPane afin de pouvoir mettre un fond à la cellule.
                Label textSquare = new Label ();
                Pane drawnSquare = new StackPane (textSquare);

                // Style pour une cellule
                drawnSquare.getStyleClass().add ("square");

                // On dessine la cellule
                grid.add (drawnSquare, x, y);
                gridInMemory [x][y] = drawnSquare;
            }
        }

        // On définit la hauteur des lignes
        for (int y = 0; y < environment.getHeight (); ++y) {
            RowConstraints rowHeight = new RowConstraints(25);
            grid.getRowConstraints().add (rowHeight);
        }
    }

    public void updateGrid (GridPane grid, Pane [][] gridInMemory, boolean isCurrentGrid) {
        // On attend que tous les agents aient fini de bouger et on bloque leurs futurs mouvements
        // pour pouvoir afficher correctement la grille.
        if (isCurrentGrid) {
            List <Agent> agentsToLock = environment.getAgentList ();
            while (!agentsToLock.isEmpty()) {
                agentsToLock.removeIf (Agent::finishCurrentMove); // finishCurrentMove renvoie true si
                // l'agent n'était pas en train de se déplacer et bloque ses futurs mouvements
            }
        }

        // On récupère la position de tous les agents dans la grille correspondante.
        Map <List <Integer>, Agent> agentPositions = environment
                .getAgentList ()
                .stream ()
                .collect (
                        Collectors.toMap (
                            agent -> isCurrentGrid?
                                List.of (agent.getCurrentX(), agent.getCurrentY ()):
                                List.of (agent.getFinalX(), agent.getFinalY ()),
                            agent -> agent
                        )
                );

        for (int x = 0; x < environment.getWidth(); ++x) {
            for (int y = 0; y < environment.getHeight(); ++y) {
                var currentPosition = List.of (x, y);

                // On met à jour le numéro de l'agent sur la case correspondante
                String agentIndex = "";
                if (agentPositions.containsKey (currentPosition)) {
                    agentIndex += agentPositions.get (currentPosition).getId ();
                }
                Pane drawnSquare = gridInMemory [x][y];

                if (isCurrentGrid) {
                    // Si on est dans la grille actuelle,
                    var currentPosition = List.of (x, y);
                    if (!agentPositions.containsKey (currentPosition)) {
                        Pane drawnSquare = gridInMemory [x][y];
                        drawnSquare.getStyleClass().removeAll ("right-position", "wrong-position");
                        Label text = (Label) drawnSquare.getChildren().get (0);
                        text.setText (" ");
                    }
                }

            }
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
            Label text = (Label) drawnSquare.getChildren().get (0);
            text.setText (agent.toString ());
        }


        environment.getAgentList().stream ().forEach (Agent::allowMove);
    }
}
