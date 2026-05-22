import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {
    
    private Map<String, String> users = new HashMap<>();
    private static final String AES_KEY = "1234567890123456"; // 16 chars = 128-bit key

    public AuthServiceImpl() throws RemoteException {
        super();
    }

    // AES Decrypt method
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
            return "❌ Username already exists!";
        }
        users.put(username, password);
        return "✅ Registration successful!";
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        try {
            String decrypted = decrypt(password);
            if (users.containsKey(username) && users.get(username).equals(decrypted)) {
                return "✅ Login successful! Welcome " + username;
            }
            return "❌ Invalid username or password!";
        } catch (Exception e) {
            return "❌ Authentication error: " + e.getMessage();
        }
    }
}
