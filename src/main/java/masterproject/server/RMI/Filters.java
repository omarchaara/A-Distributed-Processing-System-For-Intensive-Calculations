package masterproject.server.RMI;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Filters extends Remote, Serializable {
    public int[][] convertToGrayscale(int[][] image) throws RemoteException;
    public int[][] sepiaFilter(int[][] image) throws RemoteException;
    public int[][] adjustBrightness(int[][] image, int value) throws RemoteException;
    public int[][] sharpenFilter(int[][] image, float sharpenFactor) throws RemoteException;
    public int[][] gaussianBlur(int[][] image, double sigma) throws RemoteException;
}