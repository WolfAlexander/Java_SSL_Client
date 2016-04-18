import model.Client;

/**
 * Start SSL client
 */
public class Startup {
    public static void main(String[] args){
        System.setProperty("javax.net.ssl.trustStore", "keystore.jks");     //change later
        new Client("127.0.0.1", 5821);
    }
}
