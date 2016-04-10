import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Client {
    private Scanner sc = new Scanner(System.in);
    private String serverIP;
    private int port;
    private SSLSocket connection;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public Client(String serverIP, int port){
        this.serverIP = serverIP;
        this.port = port;

        start();
    }

    private void start(){
        try{
            connectToServer();
            setupIOStreams();
            waitForUserInput();
            receiveMessageFromServer();
        }catch (Exception ex){
            OutputToConsole.printErrorMessageToConsole(ex.getMessage());
            closeIOStreamsAndConnection();
        }
    }

    private void connectToServer() throws Exception{
        try{
            SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            connection = (SSLSocket)factory.createSocket(serverIP, port);
            OutputToConsole.printMessageToConsole("Connected to " + connection.getInetAddress().getHostName() + "!");
        }catch (IOException ex){
            throw new Exception("Could not connect to server!");
        }
    }

    private void setupIOStreams() throws Exception{
        try{
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();

            inputStream = new ObjectInputStream(connection.getInputStream());
            OutputToConsole.printMessageToConsole("IO streams are created!");
        }catch (Exception ex){
            throw new Exception("Could not create IO streams!");
        }
    }

    private void waitForUserInput(){
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
            while(true){
                System.out.println("Enter message: ");
                String message = sc.nextLine();

                if(message.equals("END"))
                    closeIOStreamsAndConnection();
                else
                    sendMessage(message);
            }
            }
        });
    }

    private  void sendMessage(String message){
        try{
            outputStream.writeObject(new TransferObject(message));
            outputStream.flush();

            OutputToConsole.printMessageToConsole("Message sent!");
        }catch (IOException ioEx){
            OutputToConsole.printErrorMessageToConsole("Could not send message!");
        }
    }

    private void receiveMessageFromServer(){
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                while(true){
                    try{
                        TransferObject received = (TransferObject)inputStream.readObject();
                        OutputToConsole.printMessageToConsole("Received message: \n" + received.getMessage());
                    }catch (ClassNotFoundException CNFEx){
                        OutputToConsole.printErrorMessageToConsole("Unknown object received!");
                    }catch (IOException IOEx){
                        IOEx.printStackTrace();
                        OutputToConsole.printErrorMessageToConsole("Could not receive object!");
                    }
                }
            }
        });
    }

    private void closeIOStreamsAndConnection(){
        OutputToConsole.printMessageToConsole("Closing client...");

        try {
            outputStream.close();
            inputStream.close();
            connection.close();
            System.exit(0);
        }catch (IOException ioEx){
            ioEx.printStackTrace();
        }
    }
}