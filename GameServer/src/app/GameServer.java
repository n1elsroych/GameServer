package app;

import gameEvents.GameEventsListener;
import gameEvents.DataPackageReceivedEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import threads.Server;

public class GameServer implements GameEventsListener{
    private Server server;
    private Map<Integer, User> users;
   
    public GameServer(int port) throws IOException{
        server = new Server(port);
        users = new HashMap<>();
    }
    
    public void start() throws IOException{
        server.addEventsListener(this);
        server.start();
    }

    private String getData(String type, String dataMessage){
        int i = dataMessage.indexOf(type) + type.length();
        int f = dataMessage.indexOf(";", i);
        return dataMessage.substring(i, f);
    }
    
    private void login(String dataPackage){
        int sessionID = Integer.parseInt(getData("<id>", dataPackage));
        String username = getData("<username>", dataPackage);
        String password = getData("<password>", dataPackage);
    } 
    
    private boolean containsUsername(String username){
        for (User user : users.values()){
            if (user.getUsername().equals(username)) return true;
        }
        return false;
    }
    
    private void register(String dataPackage){
        int sessionID = Integer.parseInt(getData("<id>", dataPackage));
        String username = getData("<username>", dataPackage);
        if (containsUsername(username)){
            try {
                server.sendValidationError(sessionID, "El nombre de usuario ya esta en uso");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }
        String password = getData("<password>", dataPackage);
        User user = new User(username, password);
        users.put(sessionID, user);
        try {
            server.sendConfirmation(sessionID);
        } catch (IOException ex) {
            ex.printStackTrace();
            users.remove(sessionID);
        }
    }
    
    private void sendMessage(String dataPackage){
        int sessionID = Integer.parseInt(getData("<origin>", dataPackage));
        String message = getData("<message>", dataPackage);
        message = "["+users.get(sessionID).getUsername()+"]: "+message;
        try {
            server.sendBroadcast(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
 
    @Override
    public void onMessageReceived(DataPackageReceivedEvent evt) {
        String dataPackage = evt.getMessage();
        if (dataPackage.contains("<login>")){
            login(dataPackage);
            return;
        }
        if (dataPackage.contains("<register>")){
            register(dataPackage);
            return;
        }
        if (dataPackage.contains("<message>")) sendMessage(dataPackage);
    }
}
