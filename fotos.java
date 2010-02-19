import java.io.*;
import java.net.*;

public class fotos{
    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;
    int puerto;
    String maquina;
    fotos(int port,String maq){
	puerto = port;
	maquina = maq;
    }
    void run()
    {
	try{
	    //1. creating a socket to connect to the server
	    requestSocket = new Socket(maquina, puerto);
	    System.out.println("Conectado a: " +  maquina + "a traves del puerto: " + puerto );
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
	int puerto = Integer.valueOf(args[1]);
	String maq = args[3];
	
	/* Revision de llamada */
	if (args.length != 4) {
	    System.out.println("Uso: fotos -s <servidor> -p <puerto>");
	    System.exit(-1);
	}
	if (args[0] == "-s"  && args[2] == "-p") {
	    puerto = Integer.valueOf(args[1]);
	    maq = args[3];
	}
	else if (args[0] == "-p" && args[2] == "-s") {
	    puerto = Integer.valueOf(args[1]);
	    maq = args[3];
	}
	else {
	    System.out.println("Uso: edolab -f <maquinas> -p <puertoRemote>\n");
	    System.exit(-1);
	}
	fotos client = new fotos(puerto,maq);
	client.run();
    }
}
