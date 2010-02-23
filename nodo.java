import java.io.*;
import java.net.*;
import java.util.*;
import nanoxml.*;

public class nodo {
    private ServerSocket serversock;
    private Socket socket = null;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String mensaje;
    private int puerto;
    String directorio;
    Vector vecinos = new Vector();
	
    public nodo(String dir){
	directorio = dir;
    }

    private void enviar (String mensaje){
	try{
	    out.writeObject(mensaje);
	    out.flush();
	    System.out.println("server>" + mensaje);
	}
	catch(IOException ioException){
	    ioException.printStackTrace();
	}
    }
    
    public String cadena(String arr[]) {
	String res = "";
	for (int i = 0; i < arr.length; i++){
	    res += arr[i] + ",";
	}
	return res;
    }

    private int verificar_comando(String comando) {
	String cmd[] = comando.split("[\\s]+");
	if (cmd[0].equalsIgnoreCase("C") && cmd.length == 3){
	    if (cmd[1].equalsIgnoreCase("-t") || cmd[1].equalsIgnoreCase("-k")){
		System.out.println("DFS param: busqueda");
		return 0;
	    }
	    else return -1;
	}
	else if (cmd[0].equalsIgnoreCase("D") && cmd.length == 2 && cmd[1].matches("[\\S]+[:][\\S]+")){
	    String aux[] = cmd[1].split(":");
	    String servidor = aux[0];
	    String archivo = aux[1];
	    System.out.println("Solicitud de foto " + archivo + " a servidor " + servidor);
	    return 0;
	}
	else if (cmd[0].equalsIgnoreCase("A") && cmd.length == 1){
	    System.out.println("Num vecinos");
	    return 0;
	}
	else if (cmd[0].equalsIgnoreCase("Q") && cmd.length == 1){
	    return 1;
	}
	else return -1;
    }
	
    public static Vector Vecinos(String traza) {
	File archivo = null;
	FileReader fr = null;
	BufferedReader br = null;
	Vector ln = new Vector();
	try {
	    // Apertura del fichero y creacion de BufferedReader para poder
	    // hacer una lectura comoda (disponer del metodo readLine()).
	    archivo = new File (traza);
	    fr = new FileReader (archivo);
	    br = new BufferedReader(fr);
	    
	    // Lectura del fichero
	    String linea;
	    while((linea = br.readLine()) != null){
		ln.addElement(linea);
	    }
	}
	catch(FileNotFoundException e){
	    System.err.println("El archivo "+ traza+" no existe.");
	}
	catch (Exception e){
	    e.printStackTrace();
	}
	finally{
	    try{                    
		if(null != fr){   
		    fr.close();     
		}                  
	    }catch (Exception e2){ 
		e2.printStackTrace();
	    }
	}
	return ln;
    }
    
    public void run(int puerto, String maquinas, String log){
	try{
	    String cliente;
	    // Se obtienen los nodos vecinos.
	    vecinos = Vecinos(maquinas);
	    //System.out.println(nodos_vec);
	
	    // crear socket
	    try {
		serversock = new ServerSocket(puerto, 10);
	    }
	    catch (BindException e) {
		System.err.println("Puerto en uso, escoja otro");
		System.exit(-1);
	    }	  

	    // aceptar conexion
	    socket = serversock.accept();
	    
	    // nombre de cliente
	    cliente = socket.getInetAddress().getHostName();

	    // streams
	    out = new ObjectOutputStream(socket.getOutputStream());
	    out.flush();
	    in = new ObjectInputStream(socket.getInputStream());

	    enviar("Conexion successful");

	    do{
		try{
		    mensaje = (String)in.readObject();
		    if (mensaje.equals("bye"))
			break;
		    System.out.println("client>" + mensaje);
		    
		    switch(verificar_comando(mensaje)) {
		    case -1:
			enviar("Comando invalido");
			mensaje = ""; // para evitar que coincida con bye
			break;
		    case 1:
			mensaje = "bye"; // para que salga el servidor
			enviar(mensaje); // para que salga el cliente
			break;
		    default:
			break;
		    }
		    /*
		    for (int k = 0; k < comando.length; k++){
			System.out.println(comando[k]);
		    }
		    */
		   
		    //System.out.println(cadena(comando));

		}
		catch(IOException ioe){
		    System.err.println("I/O ERROR: " + ioe.getMessage());
		}
		catch(ClassNotFoundException classnot){
		    System.err.println("Data received in unknown format");
		}
	    }while(!mensaje.equals("bye"));

	    in.close();
	    out.close();
	    serversock.close();
	}
	catch(IOException ioException){
	    ioException.printStackTrace();
	}
    }

    public static void uso(String s){
	System.out.println(s);
	System.out.println("Uso: nodo -p <puerto> -f <maquinas> -l <archivoTrazas> -d <directorio>");
	System.exit(-1);
    }
    public static void uso(){
	System.out.println("Uso: nodo -p <puerto> -f <maquinas> -l <archivoTrazas> -d <directorio>");
	System.exit(-1);
    }
    
    public void dfs_distribuido (String busqueda, Vector visitados, Vector resultados){	
	File archivo = new File(".");
	String[] xml = archivo.list(new explorador(".java"));	

    } 

    public boolean match (String archivo, String busqueda) {

	XMLElement xml = new XMLElement();
	FileReader reader = null;
	Vector children = null;
	String nombre_elem;
	String contenido_elem;
	String tipo_busqueda = (busqueda.split("[\\s]+"))[0];
	String cadena = (busqueda.split("[\\s]+"))[1];

	try {
	    reader = new FileReader(archivo);
	    xml.parseFromReader(reader);
	    children = xml.getChildren();
	}
	catch (IOException e) {
	    System.err.println(e.getMessage());
	}
	
	System.out.println("tipo busqueda: "+tipo_busqueda+"\ncadena: "+cadena);
	
	if (tipo_busqueda.equalsIgnoreCase("-t")){
	    System.out.println(children.size());
	    for (int i = 0; i < children.size(); i++){
		nombre_elem = ((XMLElement)children.elementAt(i)).getName();
		System.out.println("nombre_elem: "+ nombre_elem);
		if (nombre_elem.equals("titulo")) {
		    contenido_elem = ((XMLElement)children.elementAt(i)).getContent();
		    System.out.println("nombre elemento: " + nombre_elem + "\ncontenido elemento: " + contenido_elem);
		    if (contenido_elem.matches(".*" + cadena + ".*"))
			return true;
		    return false;
		}
	    }
	}
	return false;


    }

    public static void main(String args[]) throws Exception {
    
	int argc = args.length;
	int puerto = 0;
	String maquinas = null;
	String traza = null;
	String directorio = null;
	boolean check[] = {false,false,false,false};
	String dir_Act = System.getProperty("user.dir");
	
	// Revision de parametros de  llamada
	for (int i = 0; i < argc - 1; i += 2){
	    if (args[i].equals("-p")){
		try {
		    puerto = Integer.valueOf(args[i+1]);
		}
		catch(NumberFormatException e) {
		    uso("El puerto debe ser un entero entre 1025 y 65536");
		}
		if (!(puerto > 1024 && puerto < 65536)) {
		    uso("El puerto debe ser un entero entre 1025 y 65536");
		}
		check[0] = true;
	    }
	    else if (args[i].equals("-f")){
		maquinas = args[i+1];
		check[1] = true;
	    }
	    else if (args[i].equals("-l")){
		traza = args[i+1];
		check[2] = true;
	    }
	    else if (args[i].equals("-d")){
		directorio = args[i+1];
		check[3] = true;
	    }    
	    else uso();
	}
	traza = dir_Act + maquinas;
	if (!(check[0] && check[1] && check[2] && check[3])) uso();
	// Fin revision de parametros de llamada
	
	nodo servidor = new nodo(directorio);

	System.out.println(servidor.match("sol.xml", "-t sol"));

	while(true){
	    servidor.run(puerto, maquinas, traza);
	}
    }
}

