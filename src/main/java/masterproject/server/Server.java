package masterproject.server;

import masterproject.server.RMI.Filters;
import masterproject.server.RMI.FiltersImpl;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
public class Server {

    public static int CLIENT_PORT = 8888;
    public static int SLAVE_PORT = 2222;
    public static int RMI_PORT = 2024;
    public static String RMI_HOSTNAME = "localhost";

    public static void main(String[] args) {
        try {
//            Run sever for slaves
            SlavesHandler slaveServer = new SlavesHandler(SLAVE_PORT);
            slaveServer.start();
            System.out.println("Slaves server is running at port: " + SLAVE_PORT);

//            Run server for clients
            ClientsHandler clientServer = new ClientsHandler(CLIENT_PORT, slaveServer);
            clientServer.start();
            System.out.println("Clients server is running at port: " + CLIENT_PORT);

//            Run RMI filters Server
            Filters remoteObject = new FiltersImpl();
            // Create and export the RMI registry on RMI_PORT 2024
            LocateRegistry.createRegistry(RMI_PORT);
            // Bind the remote object to the registry with a name "Filters"
            Naming.rebind("rmi://" + RMI_HOSTNAME + ":" + RMI_PORT + "/Filters", remoteObject);
            System.out.println("RMI filters server is running at port: " + RMI_PORT);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
