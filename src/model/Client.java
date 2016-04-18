package model;

import DTO.Devices;
import DTO.GetDataRequest;
import util.OutputToConsole;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.Executors;

/**
 * Client for SSL server
 * This client communicates with server through SSLSocket by sending
 * and receiving objects of type TransferObject
 */
public class Client {
    private Scanner sc = new Scanner(System.in);
    private String serverIP;
    private int port;
    private SSLSocket connection;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    /**
     * Client constructor initializes serverIp and port
     * and starts client
     * @param serverIP - server ip adress as string
     * @param port - integer port number
     */
    public Client(String serverIP, int port){
        this.serverIP = serverIP;
        this.port = port;

        start();
    }

    /**
     * Start method launches all clients functionality
     * and handles all exception error messages
     */
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

    /**
     * Connects to server
     * In case of IOException throws new Exception
     * with custom user friendly error message
     * @throws Exception
     */
    private void connectToServer() throws Exception{
        try{
            SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            connection = (SSLSocket)factory.createSocket(serverIP, port);
            OutputToConsole.printMessageToConsole("Connected to " + connection.getInetAddress().getHostName() + "!");
        }catch (IOException ex){
            ex.printStackTrace();
            throw new Exception("Could not connect to server!");
        }
    }

    /**
     * Setups an output stream and an input stream
     * In case of IOException throws new Exception
     * with custom user friendly error message
     * @throws Exception
     */
    private void setupIOStreams() throws Exception{
        try{
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();

            inputStream = new ObjectInputStream(connection.getInputStream());
            OutputToConsole.printMessageToConsole("IO streams are created!");
        }catch (Exception ex){
            ex.printStackTrace();
            throw new Exception("Could not create IO streams!");
        }
    }

    /**
     * This method start new thread that will wait for user input
     * When input is entered, it will be checked if it is END keyword,
     * if not input will be sent ti sendMessage method,
     * if END keyword was entered then client wants to close program -
     * thread ends with calling clean up method
     */
    private void waitForUserInput(){
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                while(true){
                    System.out.println("Enter message: ");
                    String message = sc.nextLine();

                    if(message.equals("END"))
                        break;
                    else
                        sendMessage(message);
                }

                closeIOStreamsAndConnection();
            }
        });
    }

    /**
     * First converts message to needed object and then sends it to output stream
     * In case of failure an error message is printed out
     * @param message - String message
     */
    private  void sendMessage(String message){
        try{
            outputStream.writeObject(new GetDataRequest("devices"));
            outputStream.flush();

            OutputToConsole.printMessageToConsole("Message sent!");
        }catch (IOException ioEx){
            ioEx.printStackTrace();
            OutputToConsole.printErrorMessageToConsole("Could not send message!");
        }
    }

    /**
     * Receives messages from server in another thread
     * Receives only specified type of objects - other ways prints error message and waits for next message
     */
    private void receiveMessageFromServer(){
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
            while(true){
                try{
                    Devices received = (Devices) inputStream.readObject();
                    //OutputToConsole.printMessageToConsole("Received message: \n" + received.getMessage());
                    System.out.println(received);
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

    /**
     * Clean up method - closes IO streams and closes socket
     * In both cases (failure/success) closes program
     */
    private void closeIOStreamsAndConnection(){
        OutputToConsole.printMessageToConsole("Closing client...");

        try {
            if(outputStream != null)
                outputStream.close();
            if(inputStream != null)
                inputStream.close();
            if(connection != null)
                connection.close();
            System.exit(0);
        }catch (Exception ex){
            ex.printStackTrace();
            System.exit(0);
        }
    }
}