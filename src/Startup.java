/**
 * Start testing SSL client
 */
public class Startup {
    public static void main(String[] args){
        System.setProperty("javax.net.ssl.trustStore", "keyStoreTest.jks");     //change later
        System.setProperty("javax.net.ssl.trustStorePassword", "password");     //change later

        new Client("127.0.0.1", 5821);
    }
}
