package masterproject.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientsHandler extends Thread {
    private final ServerSocket ss;
    private final SlavesHandler slaveServer;
    private static final int NUM_THREADS = 4;
    private static final ExecutorService clientExecutor = Executors.newFixedThreadPool(NUM_THREADS);


    public ClientsHandler(int port, SlavesHandler slaveServer) throws IOException {
        this.ss = new ServerSocket(port);
        this.slaveServer = slaveServer;
    }

    /**
     * Receive new client
     */
    @Override
    public void run() {
        while (this.slaveServer.isAlive()) {
            try {
                Socket clientSocket = ss.accept();
                Worker worker = new Worker(clientSocket, this.slaveServer);
                clientExecutor.execute(worker); // Executes the Worker via the ExecutorService
                System.out.println("New client has been connected!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
