package masterproject.client;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.text.Text;

public class LandingController {

    public DialogPane feedbackDialog;
    public Text feedbackTitle;
    public Text feedbackContent;

    @FXML
    private void openPhotoEditor() throws IOException {
        App.setRoot("PhotoEditor");
    }
    @FXML
    private void openMatricesCalculator() throws IOException {
        App.setRoot("MatricesCalculator");
    }
}
