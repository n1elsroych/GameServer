package threads;

import gameEvents.DataPackageReceivedEvent;
import gameEvents.GameEventsListener;
import socketEvents.ClientConnectedEvent;
import socketEvents.ClientDisconnectedEvent;
import socketEvents.MessageReceivedEvent;
import socketEvents.ServerEventsListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server implements ServerEventsListener{
    private ServerSocket serverSocket;
    private Map<Integer, Socket> clients;
    ConnectionsHandler connectionsHandler;
    DisconnectionsHandler disconnectionsHandler;
    ArrayList<GameEventsListener> listeners;
    
    private int sessionID;
    
    public Server(int port) throws IOException{
        serverSocket = new ServerSocket(port);
        clients = new HashMap<>();
        sessionID = 0;
        listeners = new ArrayList<>();
        
        System.out.println("Servidor iniciado en el puerto " + port);
    }
    
    public void start() throws IOException{
        connectionsHandler = new ConnectionsHandler(serverSocket);
        connectionsHandler.addEventsListener(this);
        connectionsHandler.start();
        
        disconnectionsHandler = new DisconnectionsHandler(clients);
        disconnectionsHandler.addEventsListener(this);
        disconnectionsHandler.start();
    }
    
    public void sendBroadcast(String message) throws IOException{
        DataOutputStream out;
        synchronized (clients) {
            for (Socket clientSocket: clients.values()) {
                out = new DataOutputStream(clientSocket.getOutputStream());
                out.writeUTF(message);
            }
        }
    }
    
    @Override
    public void onUserConnected(ClientConnectedEvent evt) {
        try {
            Socket clientSocket = evt.getSocket();
            sessionID++;
            synchronized (clients) {
                clients.put(sessionID, clientSocket);
                System.out.println("Se agrego un usuario a la lista con el ID = "+sessionID);
            }
            ClientHandler clientHandler = new ClientHandler(clientSocket.getInputStream(), sessionID);
            clientHandler.addEventsListener(this);
            clientHandler.start();
            
            OutputHandler out = new OutputHandler(clientSocket.getOutputStream(), "<id>"+sessionID+";");
            out.start(); //esto debe lanzar gameServer
        } catch(IOException ex) {
            ex.printStackTrace();
            //clientId--;
        }
    }
    
    @Override
    public void onReceivedMessage(MessageReceivedEvent evt) {
        String message = evt.getMessage();
        triggerDataPackageReceivedEvent(message); 
    }
    
    @Override
    public void onClientDisconnected(ClientDisconnectedEvent evt) {
        int id = evt.getId();
        synchronized(clients){
            try {
                clients.get(id).close();
                clients.remove(id);
                System.out.println("El cliente  con ID = "+id+" ya no esta conectado");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void sendValidationError(int sessionID, String message) throws IOException{
        Socket clientSocket = clients.get(sessionID);
        OutputHandler outputHandler = new OutputHandler(clientSocket.getOutputStream(), message);
        outputHandler.start();
    }

    public void sendConfirmation(int sessionID) throws IOException {
        Socket clientSocket = clients.get(sessionID);
        OutputHandler outputHandler = new OutputHandler(clientSocket.getOutputStream(), "<success>");
        outputHandler.start();
    }
    
    public void addEventsListener(GameEventsListener listener){
        listeners.add(listener);
    }
    
    public void removeEventsListener(GameEventsListener listener) {
        listeners.remove(listener);
    }
    
    private void triggerDataPackageReceivedEvent(String message){
        DataPackageReceivedEvent evt = new DataPackageReceivedEvent(this, message);
        for (GameEventsListener listener : listeners){
            listener.onMessageReceived(evt);
        }
    }
}
