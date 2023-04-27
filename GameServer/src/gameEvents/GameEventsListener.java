package gameEvents;

import java.util.EventListener;

public interface GameEventsListener extends EventListener{
    
    public void onMessageReceived(DataPackageReceivedEvent evt);
}
