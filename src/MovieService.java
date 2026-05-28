import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MovieService extends Remote {
    List<String> getAllMovies(String token) throws RemoteException;
    String searchMovie(String token, String title) throws RemoteException;
    String getMovieDetails(String token, String title) throws RemoteException;
}