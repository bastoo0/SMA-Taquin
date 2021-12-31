package alle.dupuch.tp1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class Main extends Application {
    public void start(Stage stage) throws IOException {
        // Après avoir créé le JAR de l'appli, faire en sorte que l'on puisse lancer le JAR
        // dans un CLI avec des paramètres comme -a=5 (agentCount), etc
        int environmentWidth = 5;
        int environmentHeight = 5;
        int agentCount = 5;

        BoundedPoint2D.setBottomLeft (new Point2D (0, 0));
        BoundedPoint2D.setTopRight (new Point2D (environmentWidth - 1, environmentHeight - 1));

        Environment environment = new Environment (environmentWidth, environmentHeight, agentCount);

        // On lance la création de l'interface
        FXMLLoader fxmlLoader = new FXMLLoader (getClass ().getResource ("interface.fxml"));
        fxmlLoader.setController (new Controller (environment));
        Parent root = fxmlLoader.load ();

        // On paramètre la scène
        Scene scene = new Scene (root, 300, 185);
        stage.setTitle("Taquin - ALLE DUPUCH");
        Image icon = new Image (getClass ().getResourceAsStream ("icon.png"));
        stage.getIcons ().add (icon);
        stage.setScene(scene);

        stage.show();
        stage.toFront (); // On affiche la fenêtre au premier plan.

        // On démarre le solveur
        environment.getAgentList ()
            .stream ()
            .forEach (agent -> {
                (new Thread (agent)).start ();
            });
    }

    public static void main(String[] args) {
        launch (args);
    }

    // Ne pas oublier d'arrêter proprement l'application
}