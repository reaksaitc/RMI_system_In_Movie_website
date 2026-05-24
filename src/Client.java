import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Client {

    private static final String AES_KEY = "1234567890123456"; // same key as server!

    // AES Encrypt method
    private static String encrypt(String text) throws Exception {
        SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes()));
    }

    public static void main(String[] args) {
        try {
            // Connect to server
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            AuthService authService = (AuthService) registry.lookup("AuthService");
            MovieService movieService = (MovieService) registry.lookup("MovieService");

            Scanner scanner = new Scanner(System.in);
            boolean isLoggedIn = false;
            String currentUser = "";

            System.out.println(" Welcome to Movie RMI System!");
            System.out.println("================================");

            while (true) {
                if (!isLoggedIn) {
                    System.out.println("\n1. Register");
                    System.out.println("2. Login");
                    System.out.println("3. Exit");
                    System.out.print("Choose: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice == 1) {
                        System.out.print("Enter username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        String result = authService.register(username, password);
                        System.out.println(result);

                    } else if (choice == 2) {
                        System.out.print("Enter username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        String encryptedPassword = encrypt(password);
                        String result = authService.login(username, encryptedPassword);
                        System.out.println(result);
                        if (result.contains("Login successful")) {
                            isLoggedIn = true;
                            currentUser = username;
                        }

                    } else if (choice == 3) {
                        System.out.println("Goodbye!");
                        break;
                    }

                } else {
                    System.out.println("\n Welcome " + currentUser + "!");
                    System.out.println("1. Get All Movies");
                    System.out.println("2. Search Movie");
                    System.out.println("3. Get Movie Details");
                    System.out.println("4. Logout");
                    System.out.print("Choose: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice == 1) {
                        List<String> movies = movieService.getAllMovies();
                        System.out.println("\nAll Movies:");
                        for (String movie : movies) {
                            System.out.println("  - " + movie);
                        }

                    } else if (choice == 2) {
                        System.out.print("Enter movie title: ");
                        String title = scanner.nextLine();
                        System.out.println(movieService.searchMovie(title));

                    } else if (choice == 3) {
                        System.out.print("Enter movie title: ");
                        String title = scanner.nextLine();
                        System.out.println(movieService.getMovieDetails(title));

                    } else if (choice == 4) {
                        isLoggedIn = false;
                        currentUser = "";
                        System.out.println("Logged out successfully!");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(" Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
