package alle.dupuch.tp1;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    private Environment environment;
    @FXML private GridPane currentGrid;
    @FXML private GridPane finalGrid;
    // On est obligés d'avoir une 2e grille en mémoire car pour accéder à
    // une cellule d'un GridPane, il faut reparcourir toute la grille.
    private Pane[][] currentGridInMemory;
    private Pane[][] finalGridInMemory;

    public Controller (Environment environment) {
        this.environment = environment;
    }

    public void initialize (URL url, ResourceBundle resourceBundle) {
        // On dessine le squelette de la grille actuelle et de la grille finale
        currentGridInMemory = new Pane [environment.getWidth()][environment.getHeight()];
        finalGridInMemory = new Pane [environment.getWidth()][environment.getHeight()];
        drawGrid (currentGrid, currentGridInMemory);
        drawGrid (finalGrid, finalGridInMemory);

        // On affiche le contenu de la grille finale
        updateGrid (finalGridInMemory);

        // On met à jour de temps en temps la grille actuelle.
        var timeline = new Timeline (new KeyFrame (Duration.millis (200), e -> updateGrid (currentGridInMemory)));
        // Pour l'instant, on fait une boucle infinie.
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play ();
    }

    public void drawGrid (GridPane grid, Pane [][] gridInMemory) {
        boolean currentGrid = gridInMemory == currentGridInMemory;

        for (int x = 0; x < environment.getWidth(); ++x) {
            // On définit la largeur des colonnes
            ColumnConstraints columnWidth = new ColumnConstraints(25);
            grid.getColumnConstraints().add(columnWidth);

            for (int y = 0; y < environment.getHeight (); ++y) {
                // Chaque cellule de la grille est un StackPane afin de pouvoir mettre un fond à la cellule.
                Label textSquare = new Label ();
                Pane drawnSquare = new StackPane (textSquare);

                // Style pour une cellule
                String squareStyle = currentGrid? "current-square": "final-square";
                drawnSquare.getStyleClass().add (squareStyle);

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

    public void updateGrid (Pane [][] gridInMemory) {
        // On attend que tous les agents aient fini de bouger et on bloque leurs futurs mouvements
        // pour pouvoir afficher correctement la grille.
        boolean isCurrentGrid = gridInMemory == currentGridInMemory;
        if (isCurrentGrid) {
            List <Agent> agentsToLock = environment.getAgentList ();
            while (!agentsToLock.isEmpty()) {
                agentsToLock.removeIf (Agent::tryFreeze); // tryFreeze renvoie true si
                // l'agent n'était pas en train de se déplacer et bloque ses futurs mouvements
            }
        }
        // On récupère la position de tous les agents dans la grille correspondante.
        Map <BoundedPoint2D, Agent> agentPositions = environment
                .getAgentList ()
                .stream ()
                .collect (
                        Collectors.toMap (
                            agent -> isCurrentGrid?
                                agent.getCurrentPosition():
                                agent.getFinalPosition (),
                            agent -> agent
                        )
                );

        for (int x = 0; x < environment.getWidth(); ++x) {
            for (int y = 0; y < environment.getHeight(); ++y) {
                var currentPosition = new BoundedPoint2D (x, y);
                Pane drawnSquare = gridInMemory [x][y];
                Label squareText = (Label) drawnSquare.getChildren ().get (0);

                // On met à jour le numéro de l'agent sur la case correspondante
                String agentIndex = "";
                if (agentPositions.containsKey (currentPosition)) {
                    agentIndex += agentPositions.get (currentPosition).getId ();
                }
                squareText.setText (agentIndex);

                // On met à jour le fond de la case
                var squareStyle = new ArrayList <> (List.of (isCurrentGrid? "current-square": "final-square"));
                // On regarde s'il y a un agent dans la case actuelle
                if (agentPositions.containsKey (currentPosition)) {
                    if (isCurrentGrid) {
                        // On compare sa position actuelle par rapport à la position finale,
                        // et on colorie la case en fonction
                        var agent = agentPositions.get (currentPosition);
                        squareStyle.add (agent.isInFinalPosition ()? "right-position": "wrong-position");
                    } else {
                        squareStyle.add ("busy");
                    }
                }
                drawnSquare.getStyleClass().setAll (squareStyle);
            }
        }

        // On redonne la possibilité aux agents de se déplacer dans la grille.
        if (isCurrentGrid) {
            environment.getAgentList().stream ().forEach (Agent::allowMove);
        }
    }
}
