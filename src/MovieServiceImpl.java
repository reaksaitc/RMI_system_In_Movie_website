import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieServiceImpl extends UnicastRemoteObject implements MovieService {

    private Map<String, String> movies = new HashMap<>();
    private AuthService authService;
    private static final String NODE_ID = "NODE-001";
    private static final String SERVICE  = "MOVIE-SERVICE";

    public MovieServiceImpl(AuthService authService) throws RemoteException {
        super();
        this.authService = authService;
        movies.put("Inception", "Year: 2010 | Director: Christopher Nolan | Genre: Sci-Fi | Rating: 8.8/10");
        movies.put("The Dark Knight", "Year: 2008 | Director: Christopher Nolan | Genre: Action | Rating: 9.0/10");
        movies.put("Interstellar", "Year: 2014 | Director: Christopher Nolan | Genre: Sci-Fi | Rating: 8.6/10");
        movies.put("Avengers Endgame", "Year: 2019 | Director: Russo Brothers | Genre: Action | Rating: 8.4/10");
        movies.put("The Godfather", "Year: 1972 | Director: Francis Ford Coppola | Genre: Crime | Rating: 9.2/10");
    }

    @Override
    public List<String> getAllMovies(String token) throws RemoteException {
        if (!authService.validateToken(token)) {
            System.out.println("[" + NODE_ID + "][" + SERVICE + "] getAllMovies() | token: " + token + " | Unauthorized access! ");
            return new ArrayList<>();
        }
        System.out.println("[" + NODE_ID + "][" + SERVICE + "] getAllMovies() | token: " + token + " | returning " + movies.size() + " movies ");
        return new ArrayList<>(movies.keySet());
    }

    @Override
    public String searchMovie(String token, String title) throws RemoteException {
        if (!authService.validateToken(token)) {
            System.out.println("[" + NODE_ID + "][" + SERVICE + "] searchMovie() | token: " + token + " | Unauthorized access! ");
            return "Unauthorized access!";
        }
        for (String key : movies.keySet()) {
            if (key.toLowerCase().contains(title.toLowerCase())) {
                System.out.println("[" + NODE_ID + "][" + SERVICE + "] searchMovie() | token: " + token + " | searching: " + title + " | found: " + key + " ");
                return "Found: " + key;
            }
        }
        System.out.println("[" + NODE_ID + "][" + SERVICE + "] searchMovie() | token: " + token + " | searching: " + title + " | not found! ");
        return "Movie not found!";
    }

    @Override
    public String getMovieDetails(String token, String title) throws RemoteException {
        if (!authService.validateToken(token)) {
            System.out.println("[" + NODE_ID + "][" + SERVICE + "] getMovieDetails() | token: " + token + " | Unauthorized access! ");
            return "Unauthorized access!";
        }
        for (String key : movies.keySet()) {
            if (key.toLowerCase().contains(title.toLowerCase())) {
                System.out.println("[" + NODE_ID + "][" + SERVICE + "] getMovieDetails() | token: " + token + " | title: " + title + " | returned details! ");
                return key + "\n" + movies.get(key);
            }
        }
        System.out.println("[" + NODE_ID + "][" + SERVICE + "] getMovieDetails() | token: " + token + " | title: " + title + " | not found! ");
        return "Movie not found!";
    }
}