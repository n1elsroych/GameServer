package threads;

import events.ClientEventsListener;
import events.DisconnectionEvent;
import events.MessageReceivedEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client implements ClientEventsListener{
    private Socket socket;
    private int id;
    
    public Client(String serverAddress, int port) throws IOException{
        socket = new Socket(serverAddress, port);
    }
    
    public void connect() throws IOException{
        register();
        
        InputHandler inputHandler = new InputHandler(socket.getInputStream());
        inputHandler.addEventsListener(this);
        inputHandler.start();
        
        OutputHandler outputHandler = new OutputHandler(socket.getOutputStream(), id);
        outputHandler.addEventsListener(this);
        outputHandler.start();
    }
    
    private void register() throws IOException{
        DataInputStream in = new DataInputStream(socket.getInputStream());
        String idData = in.readUTF();
        if (idData.contains("<id>")){
            int i = idData.indexOf("<id>") + "<id>".length();
            int f = idData.indexOf(";", i);
            id =  Integer.parseInt(idData.substring(i, f));
            System.out.println("Se me ha asignado "+id);
            Scanner scanner = new Scanner(System.in);
            String validationMsg = "";
            while (!validationMsg.contains("<success>")){
                System.out.println("Introduzca su nombre de usuario: ");
                String username = scanner.nextLine();
                System.out.println("Introduzca su contrasenia:");
                String password = scanner.nextLine();

                String responseData = "<register>"+idData+"<username>"+username+";<password>"+password+";";
                System.out.println("Enviando al servidor: "+responseData);

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(responseData);

                validationMsg = in.readUTF(); //Esperamos confirmacion del servidor
                System.out.println(validationMsg);
            }
        }
    }

    @Override
    public void onReceivedMessage(MessageReceivedEvent evt) {
        String message = evt.getMessage();
        System.out.println(message);
    }

    @Override
    public void onDisconnected(DisconnectionEvent evt) {
        try {
            socket.close();
            System.out.println("Te has desconectado");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
