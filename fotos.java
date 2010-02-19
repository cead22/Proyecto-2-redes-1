import java.io.*;
import java.net.*;

public class fotos{
    Socket SocketCliente;
    ObjectOutputStream salida;
    ObjectInputStream entrada;
    String mensaje;
    int puerto;
    String maquina;
    String comando;
    
    /* Constructor */
    fotos(int port,String maq){
	puerto = port;
	maquina = maq;

    }

    public void run()
    {
       	try{
	    // Se crea un socket 
	    SocketCliente = new Socket(maquina,puerto);
	    mensaje = null;
	    salida = new ObjectOutputStream(SocketCliente.getOutputStream()); 
	    salida.flush();
	    do {
		try{
		    /* Se obtiene el comando a ejecutar */
		    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    mensaje = br.readLine();
		    if (mensaje.equalsIgnoreCase("q"))
			mensaje = "bye";
		    sendMessage(mensaje);
		}
		catch(Exception e){
		    e.printStackTrace();
		}
	    } while (!mensaje.equalsIgnoreCase("bye"));
	}
	catch(UnknownHostException unknownHost){
	    System.err.println("Se esta tratado de conectar a un servidor desconocido, verifiquelo!");
	}
	catch(IOException ioException){
	    ioException.printStackTrace();
	}
	finally{
	    /* Se cierra la conexion */
	    try{
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
	}
	catch(IOException ioException){
	    ioException.printStackTrace();
	}
	
    }


    public static void main(String args[]){
	int puerto = 0;
	String maq = null;
	
	/* Revision de llamada */
	if (args.length != 4) {
	    System.out.println("Uso: fotos -s <servidor> -p <puerto>");
	    System.exit(-1);
	}

	if (args[0].equals("-s")  && args[2].equals("-p")) {
	    try {
		puerto = Integer.valueOf(args[3]);
	    }catch(NumberFormatException e){
		System.out.println("El puerto debe ser un numero entero entre 1025 y 65535");
	    }
	    maq = args[1];
	}
	else if (args[0].equals("-p") && args[2].equals("-s")) {
	    try {
		puerto = Integer.valueOf(args[1]);
	    }catch(NumberFormatException e){
		System.out.println("El puerto debe ser un numero entero entre 1025 y 65535");
	    }maq = args[3];
	}
	else {
	    System.out.println("Uso: edolab -f <maquinas> -p <puertoRemote>\n");
	    System.exit(-1);
	}
	fotos client = new fotos(puerto,maq);
	client.run();
	
    }
}