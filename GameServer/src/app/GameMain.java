package app;

import java.io.IOException;

public class GameMain {

    public static void main(String[] args) {
        try {
            GameServer gameServer = new GameServer(8888);
            gameServer.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
}
