package masterproject.server;

import masterproject.SlaveRequest;
import masterproject.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SlaveWorker extends Thread {
    private final SlaveSocket slaveSocket;
    private final Utils.SubMatrix matrix;
    private final String filter;
    private Utils.SubMatrix result;
    private final String[] args;

    public SlaveWorker(SlaveSocket slaveSocket, Utils.SubMatrix matrix, String filter, String[] args) {
        this.slaveSocket = slaveSocket;
        this.matrix = matrix;
        this.filter = filter;
        this.args = args;
    }

    public Utils.SubMatrix getResult() {
        return result;
    }

    @Override
    public void run() {
        try {
            // Slave input/output
            ObjectOutputStream slaveOutput = slaveSocket.getSlaveOutput();
            ObjectInputStream slaveInput = slaveSocket.getSlaveInput();
            SlaveRequest request = new SlaveRequest(matrix, filter, args);
            // Send
            slaveOutput.writeObject(0);
            slaveOutput.writeObject(request);
            slaveOutput.flush();
            System.out.println("Sub image has been sent to the slave-" + slaveSocket.getSlaveIdId());
            // Receive the result
            this.result = (Utils.SubMatrix) slaveInput.readObject();
            System.out.println("The slave-" + slaveSocket.getSlaveIdId() + " has finished successfully");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }
}
