package masterproject.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import masterproject.CalculateRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.Timer;

public class MatricesCalculator {

    public VBox matrix1Box;
    public VBox matrix2Box;
    public VBox resultBox;
    @FXML
    private ComboBox<String> combOperation;

    private static ArrayList<int[][]> FinalResult=new ArrayList<>();
    private static ArrayList<int[][]> GetFinalResult(){
        return FinalResult;
    }


    public Button backButton;

    public String SERVER_HOST = "localhost";
    public int SERVER_PORT = 8888;
    public DialogPane feedbackDialog;
    public Text feedbackTitle;
    public Text feedbackContent;
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private final static UUID clientId = UUID.randomUUID();

    @FXML
    private Label finalResult;

    @FXML
    private Label labelMatrix1;

    @FXML
    private Label labelMatrix2;

    public void setSocket(Socket s) {
        this.clientSocket = s;
    }

    public void setInput(ObjectInputStream input) {
        this.input = input;
    }

    public void setOutput(ObjectOutputStream output) {
        this.output = output;
    }

    public UUID getClientId() {
        return clientId;
    }

    // Feedback message display
    public void showFeedback(String title, String content, long delay) {
        feedbackDialog.setVisible(false);
        feedbackTitle.setText(title);
        feedbackContent.setText(content);
        feedbackDialog.setVisible(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                feedbackDialog.setVisible(false);
            }
        }, delay); // the delay in millis
    }

    @FXML
    private void initialize() {
        try {
            feedbackDialog.setVisible(false);

            // Set the socket
            this.setSocket(new Socket(SERVER_HOST, SERVER_PORT));

            // Input - Output
            this.setOutput(new ObjectOutputStream(this.clientSocket.getOutputStream()));
            this.setInput(new ObjectInputStream(this.clientSocket.getInputStream()));

            // Set the running app
            this.output.writeObject(1);

            // Log message
            System.out.println("Server is running!");

            ObservableList<String> list = FXCollections.observableArrayList("Addition", "subtraction", "Multiplication");
            combOperation.setItems(list);


        } catch (Exception e) {
            showFeedback("Warning", "Server is down", 8000);
        }
    }


    @FXML
    void applyOperation(ActionEvent event) {
        // Display the matrices in the user interface
        displayFinalMatrix(FinalResult, resultBox);

    }

    @FXML
    private void backToLanding() throws IOException {
        this.clientSocket.close();
        App.setRoot("Landing");
    }

    @FXML
    void Select(ActionEvent event) throws IOException, ClassNotFoundException {

        // Retrieve the selected operation from the drop-down list
        String selectedOperation = combOperation.getValue();

        // Check which operation has been selected and define the corresponding code
        int operationCode;
        switch (selectedOperation) {
            case "Addition":
                operationCode = 1; // Code pour addition
                break;
            case "subtraction":
                operationCode = 2; // Code for subtraction
                break;
            case "Multiplication":
                operationCode = 3; // Code for multiplication
                break;
            default:
                operationCode = 0; //  Default code if no operation is selected
                break;
        }

        boolean value=true;
        int R1=0,R2=0,C1=0,C2=0;
        while (value){

            // Ask the user for the dimensions of the matrices
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Dimensions des matrices");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter the number of rows of the first matrix:");
            Optional<String> result = dialog.showAndWait();
            R1 = Integer.parseInt(result.orElse("0"));

            dialog.setContentText("Enter the number of columns of the first matrix:");
            result = dialog.showAndWait();
            C1 = Integer.parseInt(result.orElse("0"));

            dialog.setContentText("Enter the number of rows of the second matrix:");
            result = dialog.showAndWait();
            R2 = Integer.parseInt(result.orElse("0"));

            dialog.setContentText("Enter the number of columns of the second matrix:");
            result = dialog.showAndWait();
            C2 = Integer.parseInt(result.orElse("0"));

            // Use R1, C1, R2, and C2 as needed


            if(operationCode ==1 || operationCode == 2 ){
                if(R1!=R2 || C1!=C2){
                    this.showFeedback("Warning", "Both matrix1 and matrix2 should enter the same rows and columns. ", 8000);
                }else {
                    value=false;
                }
            }else if (operationCode==3){
                if(C1!=R2){
                    this.showFeedback("Warning", "The number of rows in matrix1 should match the number of columns in matrix2. ", 8000);
                }else {
                    value=false;
                }
            }else {
                this.showFeedback("Warning", "Select the operation please. ", 8000);
            }

        }
        
        if (R1 != 0 && R2 != 0 && C1 != 0 && C2 != 0) {
            // Generate random matrices with values between 0 and 20
            int[][] matrix1 = generateRandomMatrix(R1, C1);
            int[][] matrix2 = generateRandomMatrix(R2, C2);
            // Display the matrices in the user interface
            displayMatrix(matrix1, matrix1Box);
            displayMatrix(matrix2, matrix2Box);
            //send it to server
            CalculateRequest req = new CalculateRequest(operationCode, matrix1, matrix2);
            output.writeObject(req);
            FinalResult = (ArrayList<int[][]>) input.readObject();
            output.flush();
        }

    }

    // Method to generate a random matrix with values between 0 and 20
    private int[][] generateRandomMatrix(int rows, int columns) {
        int[][] matrix = new int[rows][columns];
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = rand.nextInt(21); // Random value between 0 and 20 (inclusive)
            }
        }
        return matrix;
    }

    private void displayMatrix(int[][] matrix, VBox box) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                Label label = getLabel(String.valueOf(matrix[row][col]));

                gridPane.add(label, col, row);
            }
        }
        // Add the GridPane to the existing VBox
        box.getChildren().add(gridPane);
    }



    private void displayFinalMatrix(ArrayList<int[][]> matrix, VBox box) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));
        int index=0;
        for (int i=0; i<matrix.size(); i++) {
            for (int row = 0; row < matrix.get(i).length; row++) {
                for (int col = 0; col < matrix.get(i)[row].length; col++) {
                    Label label = getLabel(String.valueOf(matrix.get(i)[row][col]));
                    gridPane.add(label, col, row+index);
                }
            }
            index += matrix.get(i).length;
        }

        // Add the GridPane to the existing VBox
        box.getChildren().add(gridPane);
    }
    private static Label getLabel(String value) {
        Label label = new Label(value);
        label.setMinWidth(80);
        label.setMinHeight(80);
        label.setMaxWidth(80);
        label.setMaxHeight(80);
        label.setAlignment(Pos.CENTER);
        label.setStyle(
                "-fx-border-color: black; " +
                        "-fx-border-width: 1px; " +
                        "-fx-padding: 10px; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-color: #212E2E;"+
                        "-fx-border-color: #445858;"
        );
        return label;
    }
}




