import java.io.*;
import java.net.*;

public class fotos{
    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;
    int puerto;
    fotos(int port){
	puerto = port;
    }
    void run()
    {
	try{
	    //1. creating a socket to connect to the server
	    requestSocket = new Socket("localhost", 39141);
	    System.out.println("Connected to localhost in port 2004");
	    //2. get Input and Output streams
	    out = new ObjectOutputStream(requestSocket.getOutputStream());
	    out.flush();
	    in = new ObjectInputStream(requestSocket.getInputStream());
	    //3: Communicating with the server
	    do{
		try{
		    message = (String)in.readObject();
		    System.out.println("server>" + message);
		    sendMessage("Hi my server");
		    message = "bye";
		    sendMessage(message);
		}
		catch(ClassNotFoundException classNot){
		    System.err.println("data received in unknown format");
		}
	    }while(!message.equals("bye"));
	}
	catch(UnknownHostException unknownHost){
	    System.err.println("You are trying to connect to an unknown host!");
	}
	catch(IOException ioException){
	    ioException.printStackTrace();
	}
	finally{
	    //4: Closing connection
	    try{
		in.close();
		out.close();
		requestSocket.close();
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
	    System.out.println("client>" + msg);
	}
	catch(IOException ioException){
	    ioException.printStackTrace();
	}
    }
    public static void main(String args[])
    {
		int puerto;
		String maq;
		
		/* Revision de llamada */
		if (args.length() != 4) {
			System.out.println("Uso: fotos -s <servidor> -p <puerto>");
		}
		if (args[0] == "-s"  && args[2] == "-p") {
			puerto = Integer.valueOf(args[1]);
			maq = args[3];
		}
		else if (args[0] == "-p" && args[2] == "-s") {
			puerto = Integer.valueOf(args[2]);
			maq = args[4];
		}
		else {
			System.out.println("Uso: edolab -f <maquinas> -p <puertoRemote>\n");
		}
		fotos client = new fotos(puerto);
		client.run();
    }
}
