package threads;

import socketEvents.ClientDisconnectedEvent;
import socketEvents.ServerEventsListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class DisconnectionsHandler extends Thread{
    private Map<Integer, Socket> clients;
    
    private ArrayList<ServerEventsListener> listeners;
    
    public DisconnectionsHandler(Map<Integer, Socket> clients){
        this.clients = clients;
        listeners = new ArrayList<>();
    }
    
    @Override
    public void run() {
        while(true) {
            synchronized (clients) { //Si este ya lo tiene bloqueado cuando llame a onClientDisconnected este podra acceder a clients? o si vuelvo a usar synchronized(clients) el bloqueo se lo queda onClientDisconnected ???
                for (Map.Entry<Integer, Socket> client : clients.entrySet()){
                    Socket clientSocket = client.getValue();
                    int sessionID = client.getKey();
                    InetAddress inetAddress = clientSocket.getInetAddress();
                    try {
                        if (!inetAddress.isReachable(2000)){
                            clientSocket.close();
                            triggerClientDisconnectedEvent(sessionID);
                            System.out.println("El cliente con ID = "+sessionID+" ya no esta conectado");
                        }
                    } catch(IOException ex){
                        ex.printStackTrace();
                        
                    }
                }
            }  
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void addEventsListener(ServerEventsListener listener){
        listeners.add(listener);
    }
    
    public void removeMiEventoListener(ServerEventsListener listener) {
        listeners.remove(listener);
    }
    
    public void triggerClientDisconnectedEvent(int id) {
        ClientDisconnectedEvent evt = new ClientDisconnectedEvent(this, id);
        for (ServerEventsListener listener : listeners) {
            listener.onClientDisconnected(evt);
        }
    }
    
}
