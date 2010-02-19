import java.io.*;
import java.net.*;

public class fotos{
    Socket SocketCliente;
    ObjectOutputStream salida;
    ObjectInputStream entrada;
    String mensaje;
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
	    SocketCliente = new Socket(maquina, puerto);
	    System.out.println("Conectado a: " +  maquina + "a traves del puerto: " + puerto );
	    //2. get Input and Output streams
	    salida = new ObjectOutputStream(SocketCliente.getOutputStream());
	    salida.flush();
	    entrada = new ObjectInputStream(SocketCliente.getInputStream());
	    //3: Communicating with the server
	    do{
		try{
		    mensaje = (String)entrada.readObject();
		    System.out.println("server>" + mensaje);
		    sendMessage("Hi my server");
		    mensaje = "bye";
		    sendMessage(mensaje);
		}
		catch(ClassNotFoundException classNot){
		    System.err.println("data received in unknown format");
		}
	    }while(!mensaje.equals("bye"));
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
		entrada.close();
		salida.close();
		SocketCliente.close();
	    }
	    catch(IOException ioException){
		ioException.printStackTrace();
	    }
	}
    }
    void sendMessage(String msg)
    {
	try{
	    salida.writeObject(msg);
	    salida.flush();
	    System.out.println("client>" + msg);
	}
	catch(IOException ioException){
	    ioException.printStackTrace();
	}
    }
    public static void main(String args[])
    {
	int puerto = 0;
	String maq = null;
	
	/* Revision de llamada */
	if (args.length != 4) {
	    System.out.println("Uso: fotos -s <servidor> -p <puerto>");
	    System.exit(-1);
	}
	if (args[0] == "-s"  && args[2] == "-p") {
	    try {
		puerto = Integer.valueOf(args[1]);
	    }
	    catch(NumberFormatException e) {
		System.out.println("El puerto debe ser un entero entre 1025 y 65536");
	    }
	    maq = args[3];
	}
	else if (args[0] == "-p" && args[2] == "-s") {
	    try {
		puerto = Integer.valueOf(args[3]);
	    }
	    catch(NumberFormatException e) {
		System.out.println("El puerto debe ser un entero entre 1025 y 65536");
	    }
	    maq = args[1];
	}
	else {
	    System.out.println("Uso: edolab -f <maquinas> -p <puertoRemote>\n");
	    System.exit(-1);
	}
	fotos client = new fotos(puerto,maq);
	client.run();
    }
}
