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

    // Clear screen (Windows & Unix)
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

    // Pause so user can read result before screen clears
    private static void pause(Scanner scanner) {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
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
            String lastResult = "(no previous action yet)";

            while (true) {
                clearScreen();

                if (!isLoggedIn) {
                    System.out.println(" Welcome to Movie RMI System!");
                    System.out.println("================================");
                    System.out.println("\n1. Register");
                    System.out.println("2. Login");
                    System.out.println("3. Back");
                    System.out.println("4. Exit");
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
                        lastResult = "[Register] " + result;
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
                        lastResult = "[Login] " + result;
                        System.out.println(result);
                        if (result.contains("Login successful")) {
                            isLoggedIn = true;
                            currentUser = username;
                        }
                        pause(scanner);

                    } else if (choice == 3) {
                        clearScreen();
                        System.out.println(" Last Result");
                        System.out.println("================================");
                        System.out.println(lastResult);
                        pause(scanner);

                    } else if (choice == 4) {
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
                    System.out.println("4. Back");
                    System.out.println("5. Logout");
                    System.out.print("Choose: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice == 1) {
                        clearScreen();
                        System.out.println(" All Movies");
                        System.out.println("================================");
                        List<String> movies = movieService.getAllMovies();
                        StringBuilder sb = new StringBuilder();
                        for (String movie : movies) {
                            sb.append("  - ").append(movie).append("\n");
                        }
                        lastResult = "[All Movies]\n" + sb.toString();
                        System.out.print(sb);
                        pause(scanner);

                    } else if (choice == 2) {
                        clearScreen();
                        System.out.println(" Search Movie");
                        System.out.println("================================");
                        System.out.print("Enter movie title: ");
                        String title = scanner.nextLine();
                        String result = movieService.searchMovie(title);
                        lastResult = "[Search: " + title + "] " + result;
                        System.out.println(result);
                        pause(scanner);

                    } else if (choice == 3) {
                        clearScreen();
                        System.out.println(" Movie Details");
                        System.out.println("================================");
                        System.out.print("Enter movie title: ");
                        String title = scanner.nextLine();
                        String result = movieService.getMovieDetails(title);
                        lastResult = "[Details: " + title + "]\n" + result;
                        System.out.println(result);
                        pause(scanner);

                    } else if (choice == 4) {
                        clearScreen();
                        System.out.println(" Last Result");
                        System.out.println("================================");
                        System.out.println(lastResult);
                        pause(scanner);

                    } else if (choice == 5) {
                        isLoggedIn = false;
                        currentUser = "";
                        lastResult = "(no previous action yet)";
                        clearScreen();
                        System.out.println("Logged out successfully!");
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