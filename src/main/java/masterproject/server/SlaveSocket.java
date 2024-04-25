package masterproject.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public class SlaveSocket {
    private final Socket slaveSocket;
    private final ObjectOutputStream slaveOutput;
    private final ObjectInputStream slaveInput;
    private final UUID slaveId = UUID.randomUUID();

    public UUID getSlaveIdId() {
        return slaveId;
    }

    public SlaveSocket(Socket slaveSocket, ObjectOutputStream slaveOutput, ObjectInputStream slaveInput) {
        this.slaveSocket = slaveSocket;
        this.slaveOutput = slaveOutput;
        this.slaveInput = slaveInput;
    }

    public Socket getSlaveSocket() {
        return slaveSocket;
    }

    public ObjectOutputStream getSlaveOutput() {
        return slaveOutput;
    }

    public ObjectInputStream getSlaveInput() {
        return slaveInput;
    }
}
