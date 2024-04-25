package masterproject.server;

import masterproject.CalculateRequest;
import masterproject.FilterRequest;
import masterproject.Utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Worker extends Thread {
    private final Socket clientSocket;
    private final ArrayList<SlaveSocket> slaves;
    public int activeApp=0;

    public Worker(Socket clientSocket, SlavesHandler slavesServer) {
        this.clientSocket = clientSocket;
        this.slaves = slavesServer.getSlaves();
    }

    @Override
    public void run() {

        try (
                ObjectOutputStream clientOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream clientInput = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            // Get the active app id
            this.activeApp = (int) clientInput.readObject();

            // Run the app
            while (clientSocket.isConnected()) {
                if (this.activeApp == 0) {
                    System.out.println("Client running Photo editor app!");
                    FilterRequest filterRequest = (FilterRequest) clientInput.readObject();
                    // Divide matrix to sub matrices
                    ArrayList<Utils.SubMatrix> subMatrices = Utils.divideMatrix(filterRequest.getImage(), slaves.size());
                    if (!subMatrices.isEmpty()) {
                        System.out.println("Image divided to " + slaves.size() + " sub matrices");
                        ArrayList<Utils.SubMatrix> resultImage = new ArrayList<>();
                        // Send sub matrices to slaves
                        ArrayList<SlaveWorker> slavesThreads = new ArrayList<>();
                        for (int i = 0; i < subMatrices.size(); i++) {
                            SlaveWorker st = new SlaveWorker(slaves.get(i), subMatrices.get(i), filterRequest.getFilter(), filterRequest.getArgs());
                            st.start();
                            slavesThreads.add(st);
                        }
                        // Wait for all slaves to finish
                        for (SlaveWorker slavesThread : slavesThreads) {
                            slavesThread.join();
                        }
                        // Add the result
                        for (SlaveWorker slavesThread : slavesThreads) {
                            resultImage.add(slavesThread.getResult());
                        }
//                    Send the image result to the client
                        clientOutput.writeObject(Utils.mergeMatrices(resultImage, filterRequest.getImage().length, filterRequest.getImage()[0].length));
                        clientOutput.flush();
                        System.out.println("Image has been sent back to the client!");
                    } else {
                        clientOutput.writeObject(null);
                    }
                } else {
                    System.out.println("Client running Matrices calculator app!");

                    CalculateRequest req= (CalculateRequest) clientInput.readObject();
                    int op = req.getOperation();
                    int[][] matrix1 = req.getMatrix1();
                    int[][] matrix2 = req.getMatrix2();

                    // Matrices calculator logic

                    if (op == 1 || op == 2) {
                        ArrayList<int[][]> subMatrices1 = Utils.divideMatrix2(matrix1, slaves.size());
                        ArrayList<int[][]> subMatrices2 = Utils.divideMatrix2(matrix2, slaves.size());
                        ArrayList<int[][]> finalresult=new ArrayList<>();
                        if (subMatrices1.size() == subMatrices2.size()) {
                            int[][] MatrixResult;
                            for (int i = 0; i < slaves.size(); i++) {
                                if(i==subMatrices1.size()){
                                    break;
                                }
                                // Slave input/output
                                ObjectOutputStream slaveOutput = slaves.get(i).getSlaveOutput();
                                ObjectInputStream slaveInput = slaves.get(i).getSlaveInput();
                                CalculateRequest threadSlaves = new CalculateRequest(op, subMatrices1.get(i), subMatrices2.get(i));
                                slaveOutput.writeObject(activeApp);
                                slaveOutput.writeObject(threadSlaves);
                                MatrixResult= (int[][]) slaveInput.readObject();
                                finalresult.add(MatrixResult);

                            }

                            // Send the matrix result to the client
                            clientOutput.writeObject(finalresult);
                            clientOutput.flush();

                        } else {
                            System.out.println("error: matrix1.size() != matrix2.size()");
                        }
                    }else {
                        //multiplication
                        ArrayList<int[][]> subMatrices1 = Utils.divideMatrix2(matrix1, slaves.size());
                        ArrayList<int[][]> finalresult=new ArrayList<>();
                        if (matrix1[0].length == matrix2.length){
                            int[][] MatrixResult;
                            for (int i = 0; i < slaves.size(); i++) {
                                if(i==subMatrices1.size()){
                                    break;
                                }
                                // Slave input/output
                                ObjectOutputStream slaveOutput = slaves.get(i).getSlaveOutput();
                                ObjectInputStream slaveInput = slaves.get(i).getSlaveInput();
                                CalculateRequest threadSlaves = new CalculateRequest(op, subMatrices1.get(i), matrix2);
                                //send it
                                slaveOutput.writeObject(activeApp);
                                slaveOutput.writeObject(threadSlaves);
                                slaveOutput.flush();
                                // read it
                                MatrixResult= (int[][]) slaveInput.readObject();
                                finalresult.add(MatrixResult);
                            }
                            // Send the matrix result to the client
                            clientOutput.writeObject(finalresult);
                            clientOutput.flush();
                        }else {
                            System.out.println("error: matrix1[0].length != matrix2.length");
                        }
                    }//end if(op==?)
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
