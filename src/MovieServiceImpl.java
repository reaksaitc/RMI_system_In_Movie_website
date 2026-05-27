import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MovieServiceImpl extends UnicastRemoteObject implements MovieService {

    private Map<String, String> movies = new HashMap<>();
    private static final AtomicInteger clientCounter = new AtomicInteger(0);
    private static final Map<Long, String> threadClientMap = new java.util.concurrent.ConcurrentHashMap<>();
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public MovieServiceImpl() throws RemoteException {
        super();
        movies.put("Inception", "Year: 2010 | Director: Christopher Nolan | Genre: Sci-Fi | Rating: 8.8/10");
        movies.put("The Dark Knight", "Year: 2008 | Director: Christopher Nolan | Genre: Action | Rating: 9.0/10");
        movies.put("Interstellar", "Year: 2014 | Director: Christopher Nolan | Genre: Sci-Fi | Rating: 8.6/10");
        movies.put("Avengers Endgame", "Year: 2019 | Director: Russo Brothers | Genre: Action | Rating: 8.4/10");
        movies.put("The Godfather", "Year: 1972 | Director: Francis Ford Coppola | Genre: Crime | Rating: 9.2/10");
    }

    private String getClientId() {
        long threadId = Thread.currentThread().getId();
        return threadClientMap.computeIfAbsent(threadId,
            id -> "CLIENT-" + clientCounter.incrementAndGet());
    }

    private String now() {
        return LocalTime.now().format(fmt);
    }

    private void log(String message) {
        System.out.println("[" + getClientId() + "][" + now() + "] " + message);
    }

    @Override
    public List<String> getAllMovies() throws RemoteException {
        log("[MOVIE] Request: Get All Movies");
        return new ArrayList<>(movies.keySet());
    }

    @Override
    public String searchMovie(String title) throws RemoteException {
        log("[MOVIE] Request: Search Movie - \"" + title + "\"");
        for (String key : movies.keySet()) {
            if (key.toLowerCase().contains(title.toLowerCase())) {
                log("[MOVIE] Search SUCCESS - found: " + key);
                return "Found: " + key;
            }
        }
        log("[MOVIE] Search FAILED - not found: " + title);
        return "Movie not found!";
    }

    @Override
    public String getMovieDetails(String title) throws RemoteException {
        log("[MOVIE] Request: Get Details - \"" + title + "\"");
        for (String key : movies.keySet()) {
            if (key.toLowerCase().contains(title.toLowerCase())) {
                log("[MOVIE] Details SUCCESS - found: " + key);
                return key + "\n" + movies.get(key);
            }
        }
        log("[MOVIE] Details FAILED - not found: " + title);
        return "Movie not found!";
    }
}