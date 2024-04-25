package masterproject.slave;

import masterproject.CalculateRequest;
import masterproject.SlaveRequest;
import masterproject.Utils;
import masterproject.server.RMI.Filters;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.Naming;


public class Slave {
    public static int activeApp = 1;

    public static void main(String[] args) {
        String SERVER_HOST = "localhost";
        int SERVER_PORT = 2222;
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            System.out.println("Slave connected on: " + socket.getInetAddress());

//            Get filters RMI
            // Look up the remote object from the registry with the name "MyRemoteObject"
            Filters filters = (Filters) Naming.lookup("rmi://localhost:2024/Filters");

            while (!socket.isClosed()) {
                activeApp = (int) input.readObject();

                if (activeApp == 0) {
                    SlaveRequest req = (SlaveRequest) input.readObject();
                    System.out.println("Filter: " + req.getFilter());
                    if (req.getSubMatrix().matrix.length > 0) {
                        System.out.println("Slave received a request!");
                        Utils.SubMatrix result = new Utils.SubMatrix(req.getSubMatrix().startRow, req.getSubMatrix().endRow, req.getSubMatrix().index);
                        // Apply filter
                        switch (req.getFilter()) {
                            case "grayscale": {
                                result.matrix = filters.convertToGrayscale(req.getSubMatrix().matrix);
                                break;
                            }
                            case "sepia": {
                                result.matrix = filters.sepiaFilter(req.getSubMatrix().matrix);
                                break;
                            }
                            case "brightness": {
                                if (req.getArgs().length != 1) {
                                    throw new IllegalArgumentException();
                                }
                                int brightnessValue = Integer.parseInt(req.getArgs()[0]);
                                result.matrix = filters.adjustBrightness(req.getSubMatrix().matrix, brightnessValue);
                                break;
                            }
                            case "sharpen": {
                                if (req.getArgs().length != 1) {
                                    throw new IllegalArgumentException();
                                }
                                int sharpenFactor = Integer.parseInt(req.getArgs()[0]);
                                result.matrix = filters.sharpenFilter(req.getSubMatrix().matrix, sharpenFactor);
                                break;
                            }
                            case "gaussian": {
                                if (req.getArgs().length != 1) {
                                    throw new IllegalArgumentException();
                                }
                                double sigma = Double.parseDouble(req.getArgs()[0]);
                                result.matrix = filters.gaussianBlur(req.getSubMatrix().matrix, sigma);
                                break;
                            }
                        }
                        output.writeObject(result);
                        output.flush();
                        System.out.println("Slave handled the request and sent it to the server!");
                    }
                } else {


                    CalculateRequest req = (CalculateRequest) input.readObject();
                    System.out.println("Slave received a request!");
                    int op = req.getOperation();
                    int[][] matrix1 = req.getMatrix1();
                    int[][] matrix2 = req.getMatrix2();
                    int startRow = 0;
                    int endRow = matrix1.length;
                    int[][] resultMatrix = new int[matrix1.length][matrix2[0].length];


                    if (op == 1 || op == 2) {
                        for (int k = startRow; k < endRow; k++) {
                            int[] rowMatrix1 = matrix1[k];
                            int[] rowMatrix2 = matrix2[k];
                            int[] resultRow = new int[rowMatrix1.length];
                            for (int j = 0; j < rowMatrix1.length; j++) {
                                resultRow[j] = (op == 1) ? rowMatrix1[j] + rowMatrix2[j] : rowMatrix1[j] - rowMatrix2[j];
                            }
                            resultMatrix[k] = resultRow;
                        }
                        Utils.printMatrix(resultMatrix);
                        output.writeObject(resultMatrix);
                    } else {
                        // Code for matrix multiplication
                        for (int k = startRow; k < endRow; k++) {
                            for (int i = 0; i < matrix2[0].length; i++) {
                                int sum = 0;
                                for (int j = 0; j < matrix2.length; j++) {
                                    sum += matrix1[k][j] * matrix2[j][i];
                                }
                                resultMatrix[k][i] = sum;
                            }
                        }
                        Utils.printMatrix(resultMatrix);
                        output.writeObject(resultMatrix);
                        output.flush();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
