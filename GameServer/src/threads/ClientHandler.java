package threads;

import socketEvents.ClientDisconnectedEvent;
import socketEvents.MessageReceivedEvent;
import socketEvents.ServerEventsListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ClientHandler extends Thread{
    private DataInputStream in;
    private ArrayList<ServerEventsListener> listeners;
    
    private int clientID;
    
    public ClientHandler(InputStream inputStream, int id){
        in = new DataInputStream(inputStream);
        listeners = new ArrayList<>();
        
        clientID = id;
    }
    
    @Override
    public void run() {
        boolean isConnected = true;
        while (isConnected) {
            try {
                String message = in.readUTF(); //Si aqui falla le llegara el id en el message?
                triggerMessageReceivedEvent(message);
            } catch (IOException ex) {
                ex.printStackTrace();
                isConnected = false;
                
                triggerClientDisconnectedEvent(clientID);
            }
        }
    }

    public void addEventsListener(ServerEventsListener listener){
        listeners.add(listener);
    }
    
    public void removeEventsListener(ServerEventsListener listener) {
        listeners.remove(listener);
    }
    
    public void triggerMessageReceivedEvent(String message) {
        MessageReceivedEvent evt = new MessageReceivedEvent(this, message);
        for (ServerEventsListener listener : listeners) {
            listener.onReceivedMessage(evt);
        }
    }
        
    public void triggerClientDisconnectedEvent(int id) {
        ClientDisconnectedEvent evt = new ClientDisconnectedEvent(this, id);
        for (ServerEventsListener listener : listeners) {
            listener.onClientDisconnected(evt);
        }
    }
}
