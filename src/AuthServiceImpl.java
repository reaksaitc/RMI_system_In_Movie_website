import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {

    private Map<String, String> users  = new HashMap<>();
    private Map<String, String> tokens = new HashMap<>();
    private static final String AES_KEY = "1234567890123456";
    private static final String NODE_ID = "NODE-001";
    private static final String SERVICE  = "AUTH-SERVICE";

    public AuthServiceImpl() throws RemoteException {
        super();
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
        if (users.containsKey(username)) {
            System.out.println("[" + NODE_ID + "][" + SERVICE + "] register() | user: " + username + " | already exists!");
            return "Username already exists!";
        }
        users.put(username, password);
        System.out.println("[" + NODE_ID + "][" + SERVICE + "] register() | user: " + username + " | stored successfully!");
        return "Registration successful!";
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        try {
            String decrypted = decrypt(password);
            if (users.containsKey(username) && users.get(username).equals(decrypted)) {
                String token = UUID.randomUUID().toString();
                tokens.put(token, username);
                System.out.println("[" + NODE_ID + "][" + SERVICE + "] login() | user: " + username + " | match! | generated token: " + token);
                return token;
            }
            System.out.println("[" + NODE_ID + "][" + SERVICE + "] login() | user: " + username + " | failed!");
            return "Invalid username or password!";
        } catch (Exception e) {
            System.out.println("[" + NODE_ID + "][" + SERVICE + "] login() | user: " + username + " | error: " + e.getMessage());
            return "Authentication error: " + e.getMessage();
        }
    }

    @Override
    public String logout(String token) throws RemoteException {
        if (tokens.containsKey(token)) {
            String username = tokens.get(token);
            tokens.remove(token);
            System.out.println("[" + NODE_ID + "][" + SERVICE + "] logout() | user: " + username + " | token removed | session ended!");
            return "Logged out successfully!";
        }
        System.out.println("[" + NODE_ID + "][" + SERVICE + "] logout() | invalid token | rejected!");
        return "Invalid token!";
    }

    @Override
    public boolean validateToken(String token) throws RemoteException {
        boolean valid = tokens.containsKey(token);
        String username = valid ? tokens.get(token) : "unknown";
        System.out.println("[" + NODE_ID + "][" + SERVICE + "] validateToken() | token: " + token + " | user: " + username + " | " + (valid ? "valid " : "invalid "));
        return valid;
    }
}