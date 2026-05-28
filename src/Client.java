import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Client {

    private static final String AES_KEY = "1234567890123456";

    private static String encrypt(String text) throws Exception {
        SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes()));
    }

    private static void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb = os.contains("win")
                ? new ProcessBuilder("cmd", "/c", "cls")
                : new ProcessBuilder("clear");
            pb.inheritIO().start().waitFor();
        } catch (Exception e) {
            for (int i = 0; i < 40; i++) System.out.println();
        }
    }

    private static void pause(Scanner scanner) {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            AuthService authService   = (AuthService)   registry.lookup("AuthService");
            MovieService movieService = (MovieService) registry.lookup("MovieService");

            Scanner scanner     = new Scanner(System.in);
            boolean isLoggedIn  = false;
            String currentUser  = "";
            String token        = "";        // ← stores token after login

            while (true) {
                clearScreen();

                if (!isLoggedIn) {
                    System.out.println(" Welcome to Movie RMI System!");
                    System.out.println("================================");
                    System.out.println("\n1. Register");
                    System.out.println("2. Login");
                    System.out.println("3. Exit");
                    System.out.print("Choose: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice == 1) {
                        clearScreen();
                        System.out.println(" Register");
                        System.out.println("================================");
                        System.out.print("Enter username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        String result = authService.register(username, password);
                        System.out.println(result);
                        pause(scanner);

                    } else if (choice == 2) {
                        clearScreen();
                        System.out.println(" Login");
                        System.out.println("================================");
                        System.out.print("Enter username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        String encryptedPassword = encrypt(password);
                        String result = authService.login(username, encryptedPassword);

                        // token is a UUID → does not contain spaces
                        // invalid messages always contain spaces e.g. "Invalid username..."
                        if (!result.contains(" ")) {
                            token       = result;
                            isLoggedIn  = true;
                            currentUser = username;
                            System.out.println("Login successful! Welcome " + username);
                            System.out.println("Your token: " + token);
                        } else {
                            System.out.println(result);
                        }
                        pause(scanner);

                    } else if (choice == 3) {
                        clearScreen();
                        System.out.println("Goodbye!");
                        break;
                    }

                } else {
                    System.out.println(" Welcome " + currentUser + "!");
                    System.out.println("================================");
                    System.out.println("\n1. Get All Movies");
                    System.out.println("2. Search Movie");
                    System.out.println("3. Get Movie Details");
                    System.out.println("4. Logout");
                    System.out.print("Choose: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice == 1) {
                        clearScreen();
                        System.out.println(" All Movies");
                        System.out.println("================================");
                        List<String> movies = movieService.getAllMovies(token);  // ← pass token
                        if (movies.isEmpty()) {
                            System.out.println("Unauthorized access!");
                        } else {
                            for (String movie : movies) {
                                System.out.println("  - " + movie);
                            }
                        }
                        pause(scanner);

                    } else if (choice == 2) {
                        clearScreen();
                        System.out.println(" Search Movie");
                        System.out.println("================================");
                        System.out.print("Enter movie title: ");
                        String title  = scanner.nextLine();
                        String result = movieService.searchMovie(token, title);  // ← pass token
                        System.out.println(result);
                        pause(scanner);

                    } else if (choice == 3) {
                        clearScreen();
                        System.out.println(" Movie Details");
                        System.out.println("================================");
                        System.out.print("Enter movie title: ");
                        String title  = scanner.nextLine();
                        String result = movieService.getMovieDetails(token, title);  // ← pass token
                        System.out.println(result);
                        pause(scanner);

                    } else if (choice == 4) {
                        String result = authService.logout(token);  // ← invalidate token on server
                        System.out.println(result);
                        isLoggedIn  = false;
                        currentUser = "";
                        token       = "";   // ← clear token
                        pause(scanner);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(" Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}