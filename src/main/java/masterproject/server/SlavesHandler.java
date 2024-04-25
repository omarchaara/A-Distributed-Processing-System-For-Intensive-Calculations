package masterproject.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SlavesHandler extends Thread {
    private final ServerSocket ss;
    private final ArrayList<SlaveSocket> slaves = new ArrayList<>();


    public SlavesHandler(int port) throws IOException {
        this.ss = new ServerSocket(port);
    }
    public ArrayList<SlaveSocket> getSlaves() {
        return slaves;
    }

    /**
     * Receive new slaves
     */
    @Override
    public void run() {
        while (!ss.isClosed()) {
            try {
                // Accept new slaves connection
                Socket slaveSocket = ss.accept();
                // Create input/output
                ObjectOutputStream slaveOutput = new ObjectOutputStream(slaveSocket.getOutputStream());
                ObjectInputStream slaveInput = new ObjectInputStream(slaveSocket.getInputStream());
                // Create the slave socket
                SlaveSocket ss = new SlaveSocket(slaveSocket, slaveOutput, slaveInput);
                slaves.add(ss);
                // Log the message
                System.out.println("New slave connected on " + ss.getSlaveSocket().getLocalPort());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
