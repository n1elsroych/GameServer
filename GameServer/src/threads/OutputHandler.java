package threads;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OutputHandler extends Thread{
    DataOutputStream out;
    String dataPackage;
    
    public OutputHandler(OutputStream output, String dataPackage){
        out = new DataOutputStream(output);
        this.dataPackage = dataPackage;
    }
    
    @Override
    public void run() {
        //while (true){
            try {
                out.writeUTF(dataPackage);
                //return;
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("No se pudo enviar el mensaje");
            }
        //}
    }
}
