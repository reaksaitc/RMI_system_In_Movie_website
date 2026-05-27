import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MovieService extends Remote {
    List<String> getAllMovies() throws RemoteException;
    String searchMovie(String title) throws RemoteException;
    String getMovieDetails(String title) throws RemoteException;
}