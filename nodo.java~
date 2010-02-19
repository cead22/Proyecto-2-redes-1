import java.io.*;
import java.net.*;

public class Servidor {
    private ServerSocket socket;
    private Socket conexion = null;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String mensaje;
    private int puerto;

    public Servidor(int p){
	puerto = p;
    }

    private void enviar (String msg)
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

    public void run(){
	try{
	    //1. creating a server socket
	    socket = new ServerSocket(puerto, 10);
	    //2. Wait for conexion
	    System.out.println("Waiting for connection");
	    conexion = socket.accept();
	    System.out.println("Conexion received from " + conexion.getInetAddress().getHostName());
	    //3. get Input and Output streams
	    out = new ObjectOutputStream(conexion.getOutputStream());
	    out.flush();
	    in = new ObjectInputStream(conexion.getInputStream());
	    sendMensaje("Conexion successful");
	    //4. The two parts communicate via the input and output streams
	    do{
		try{
		    mensaje = (String)in.readObject();
		    System.out.println("client>" + mensaje);
		    if (mensaje.equals("bye"))
			enviar("bye");
		}
		catch(ClassNotFoundException classnot){
		    System.err.println("Data received in unknown format");
		}
	    }while(!mensaje.equals("bye"));
	}
	catch(IOException ioException){
	    ioException.printStackTrace();
	}
	finally{
	    //4: Closing conexion
	    try{
		in.close();
		out.close();
		socket.close();
	    }
	    catch(IOException ioException){
		ioException.printStackTrace();
	    }
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
