import java.io.*;
import java.net.*;

public class Servidor{
    ServerSocket providerSocket;
    Socket connection = null;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;
    int puerto;
    Servidor(int port){
	puerto = port;
    }
    void run()
    {
	try{
	    //1. creating a server socket
	    providerSocket = new ServerSocket(puerto, 10);
	    //2. Wait for connection
	    System.out.println("Waiting for connection");
	    connection = providerSocket.accept();
	    System.out.println("Connection received from " + connection.getInetAddress().getHostName());
	    //3. get Input and Output streams
	    out = new ObjectOutputStream(connection.getOutputStream());
	    out.flush();
	    in = new ObjectInputStream(connection.getInputStream());
	    sendMessage("Connection successful");
	    //4. The two parts communicate via the input and output streams
	    do{
		try{
		    message = (String)in.readObject();
		    System.out.println("client>" + message);
		    if (message.equals("bye"))
			sendMessage("bye");
		}
		catch(ClassNotFoundException classnot){
		    System.err.println("Data received in unknown format");
		}
	    }while(!message.equals("bye"));
	}
	catch(IOException ioException){
	    ioException.printStackTrace();
	}
	finally{
	    //4: Closing connection
	    try{
		in.close();
		out.close();
		providerSocket.close();
	    }
	    catch(IOException ioException){
		ioException.printStackTrace();
	    }
	}
    }
    void sendMessage(String msg)
    {
	try{
	    out.writeObject(msg);
	    out.flush();
	    System.out.println("server>" + msg);
	}
	catch(IOException ioException){
	    ioException.printStackTrace();
	}
    }
    public static void main(String args[])
    {
	int puerto =  Integer.valueOf(args[0]);
	Servidor server = new Servidor(puerto);
	while(true){
	    server.run();
	}
    }
}
