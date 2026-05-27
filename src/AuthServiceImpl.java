import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {

    private Map<String, String> users = new HashMap<>();
    private static final String AES_KEY = "1234567890123456";
    private static final AtomicInteger clientCounter = new AtomicInteger(0);
    private static final Map<Long, String> threadClientMap = new java.util.concurrent.ConcurrentHashMap<>();
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public AuthServiceImpl() throws RemoteException {
        super();
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

    private String decrypt(String encryptedText) throws Exception {
        SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        return new String(cipher.doFinal(decoded));
    }

    @Override
    public String register(String username, String password) throws RemoteException {
        log("[AUTH] Register request - username: " + username + " | password: " + password);
        if (users.containsKey(username)) {
            log("[AUTH] Register FAILED - username already exists: " + username);
            return "Username already exists!";
        }
        users.put(username, password);
        log("[AUTH] Register SUCCESS - new user: " + username + " | password: " + password);
        return "Registration successful!";
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        log("[AUTH] Login request - username: " + username);
        try {
            String decrypted = decrypt(password);
            log("[AUTH] Login decrypted password: " + decrypted);
            if (users.containsKey(username) && users.get(username).equals(decrypted)) {
                log("[AUTH] Login SUCCESS - user: " + username + " | password: " + decrypted);
                return "Login successful! Welcome " + username;
            }
            log("[AUTH] Login FAILED - invalid credentials for: " + username);
            return "Invalid username or password!";
        } catch (Exception e) {
            log("[AUTH] Login ERROR - " + e.getMessage());
            return "Authentication error: " + e.getMessage();
        }
    }
}