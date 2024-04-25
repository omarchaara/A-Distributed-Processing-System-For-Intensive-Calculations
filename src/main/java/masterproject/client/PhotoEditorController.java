package masterproject.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import masterproject.FilterRequest;
import masterproject.Utils;

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.io.File;
import java.io.IOException;

public class PhotoEditorController {
    public ImageView originalImageView;
    public ImageView filteredImageView;
    public Image defaultImg;

    public MenuItem importImage;
    public MenuItem saveImage;
    public MenuItem resetImage;
    public MenuItem close;
    public MenuItem grayscaleFilter;
    public MenuItem sepiaFilter;
    public MenuItem brightnessFilter;
    public MenuItem sharpenFilter;
    public MenuItem gaussianFilter;

    public String SERVER_HOST = "localhost";
    public int SERVER_PORT = 8888;
    public Menu filtersMenu;
    public BorderPane filteredImagePane;
    public BorderPane originallmagePane;

    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public void setSocket(Socket s) {
        this.clientSocket = s;
    }

    public void setInput(ObjectInputStream input) {
        this.input = input;
    }

    public void setOutput(ObjectOutputStream output) {
        this.output = output;
    }

    @FXML
    private void initialize() {
        this.defaultImg = this.originalImageView.getImage();
        // Initialize socket
        try {
            // Set the socket
            this.setSocket(new Socket(SERVER_HOST, SERVER_PORT));

            // Input - Output
            this.setOutput(new ObjectOutputStream(this.clientSocket.getOutputStream()));
            this.setInput(new ObjectInputStream(this.clientSocket.getInputStream()));

            // Set the running app
            this.output.writeObject(0);

            // Log message
            System.out.println("Server is running!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void backToLanding() throws IOException {
        this.clientSocket.close();
        App.setRoot("Landing");
    }

//    Filters
    @FXML
    private void applyFilter(String filter) {
        if (this.originalImageView.getImage() != this.defaultImg) {
            try {
                // Add the image to the request
                int[][] imageInt = Utils.imageToIntMatrix(this.originalImageView.getImage());
                FilterRequest request = null;
                switch (filter) {
                    case "grayscale": {
                        request = new FilterRequest(imageInt, "grayscale", new String[0]);
                        break;
                    }
                    case "sepia": {
                        request = new FilterRequest(imageInt, "sepia", new String[0]);
                        break;
                    }
                    case "brightness": {
                        String[] args = new String[1];
                        TextInputDialog dialog = new TextInputDialog("20");
                        dialog.setTitle("Brightness value");
                        dialog.setHeaderText("Please enter the value of brightness:");
                        Optional<String> result = dialog.showAndWait();

                        args[0] = result.orElse("0");
                        request = new FilterRequest(imageInt, "brightness", args);
                        break;
                    }
                    case "sharpen": {
                        String[] args = new String[1];
                        TextInputDialog dialog = new TextInputDialog("8");
                        dialog.setTitle("Sharpen factor");
                        dialog.setHeaderText("Please enter the sharpen factor:");
                        Optional<String> result = dialog.showAndWait();

                        args[0] = result.orElse("0");
                        request = new FilterRequest(imageInt, "sharpen", args);
                        break;
                    }
                    case "gaussian": {
                        String[] args = new String[1];
                        TextInputDialog dialog = new TextInputDialog("5");
                        dialog.setTitle("Sigma");
                        dialog.setHeaderText("Please enter the sigma value:");
                        Optional<String> result = dialog.showAndWait();

                        args[0] = result.orElse("0");
                        request = new FilterRequest(imageInt, "gaussian", args);
                        break;
                    }
                }
                assert request != null;
                System.out.println("Client choice:" + request.getFilter());
                // Send the request
                this.output.writeObject(request);
                this.output.flush();
                System.out.println("Image sent to the server");

                // Receive the response
                int[][] res = (int[][]) this.input.readObject();
                System.out.println("Server respond!");

                Image filteredImg = Utils.intMatrixToImage(res);

                // Update the view
                this.filteredImageView.setImage(filteredImg);
                System.out.println("Image processed successfully");
            } catch (Exception e) {
                System.out.println("Failed: " + e.getMessage());
            }
        } else {
            System.out.println("Warning: Load an image first");
        }
    }

    public void grayscaleFilter() {
        this.applyFilter("grayscale");
    }
    public void sepiaFilter() {
        this.applyFilter("sepia");
    }
    public void brightnessFilter() {
        this.applyFilter("brightness");
    }
    public void sharpenFilter() {
        this.applyFilter("sharpen");
    }
    public void gaussianFilter() {
        this.applyFilter("gaussian");
    }

//    File
    @FXML
    private void reset() {
        originalImageView.setImage(defaultImg);
        filteredImageView.setImage(defaultImg);
        this.setItemsDisabled(true);
    }

    @FXML
    private void loadImage() {
        defaultImg = originalImageView.getImage();

        // Create a FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");

        // Add filters to the file chooser
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Show the file chooser and get the selected file
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Load the selected image
            Image image = new Image(selectedFile.toURI().toString());
            originalImageView.setImage(image);
            this.setItemsDisabled(false);
        }
    }
    public void setItemsDisabled(boolean status) {
        saveImage.setDisable(status);
        resetImage.setDisable(status);
        filtersMenu.setDisable(status);
    }
    @FXML
    private void saveImage() {
        Image filteredImage = filteredImageView.getImage();

        if (filteredImage != null) {
            // Create a FileChooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");

            // Add filters to the file chooser
            FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("PNG Files", "*.png");
            fileChooser.getExtensionFilters().add(imageFilter);

            // Show the file chooser and get the selected file
            File file = fileChooser.showSaveDialog(new Stage());

            if (file != null) {
                try {
                    // Convert and save the JavaFX Image directly
                    ImageIO.write(SwingFXUtils.fromFXImage(filteredImage, null), "png", file);
                    System.out.println("Image saved successfully!");
                } catch (IOException e) {
                    System.out.println("Error saving image: " + e.getMessage());
                }
            }
        } else {
            System.out.println("Warning: No filtered image to save");
        }
    }
}
