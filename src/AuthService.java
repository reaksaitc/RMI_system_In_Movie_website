import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthService extends Remote {
    String register(String username, String password) throws RemoteException;
    String login(String username, String password) throws RemoteException;
    String logout(String token) throws RemoteException;
    boolean validateToken(String token) throws RemoteException;
}