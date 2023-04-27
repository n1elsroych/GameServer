package gameEvents;

import java.util.EventObject;

public class DataPackageReceivedEvent extends EventObject{
    private String message;
    
    public DataPackageReceivedEvent(Object source, String message){
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
