import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieServiceImpl extends UnicastRemoteObject implements MovieService {

    private Map<String, String> movies = new HashMap<>();

    public MovieServiceImpl() throws RemoteException {
        super();
        // Add some movies to our "database"
        movies.put("Inception", "Year: 2010 | Director: Christopher Nolan | Genre: Sci-Fi | Rating: 8.8/10");
        movies.put("The Dark Knight", "Year: 2008 | Director: Christopher Nolan | Genre: Action | Rating: 9.0/10");
        movies.put("Interstellar", "Year: 2014 | Director: Christopher Nolan | Genre: Sci-Fi | Rating: 8.6/10");
        movies.put("Avengers Endgame", "Year: 2019 | Director: Russo Brothers | Genre: Action | Rating: 8.4/10");
        movies.put("The Godfather", "Year: 1972 | Director: Francis Ford Coppola | Genre: Crime | Rating: 9.2/10");
    }

    @Override
    public List<String> getAllMovies() throws RemoteException {
        return new ArrayList<>(movies.keySet());
    }

    @Override
    public String searchMovie(String title) throws RemoteException {
        for (String key : movies.keySet()) {
            if (key.toLowerCase().contains(title.toLowerCase())) {
                return "Found: " + key;
            }
        }
        return "Movie not found!";
    }

    @Override
    public String getMovieDetails(String title) throws RemoteException {
        for (String key : movies.keySet()) {
            if (key.toLowerCase().contains(title.toLowerCase())) {
                return key + "\n" + movies.get(key);
            }
        }
        return "Movie not found!";
    }
}