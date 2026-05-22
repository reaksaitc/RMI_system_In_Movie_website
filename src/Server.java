import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            // Create services
            AuthServiceImpl authService = new AuthServiceImpl();
            MovieServiceImpl movieService = new MovieServiceImpl();

            // Create RMI registry on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // Register services
            registry.rebind("AuthService", authService);
            registry.rebind("MovieService", movieService);

            System.out.println("✅ Server is running...");
            System.out.println("✅ AuthService registered!");
            System.out.println("✅ MovieService registered!");
            System.out.println("🎬 Movie RMI System ready!");

        } catch (Exception e) {
            System.out.println("❌ Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}