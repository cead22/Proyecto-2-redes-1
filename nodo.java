import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import nanoxml.*;

public class nodo {
    private ServerSocket serversock;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String mensaje;
    private int puerto;
    String directorio;
    Vector<String> nodos_vecinos;
    String bus;

    public nodo(int p, String dir){
	directorio = dir;
	puerto = p;
	socket = null;
    }

    private void enviar (ObjectOutputStream out, Object mensaje){
	try{
	    out.writeObject(mensaje);
	    out.flush();
	}
	catch(IOException e){
	    e.printStackTrace();
	}
    }

    private Object recibir (ObjectInputStream in){
	Object o = null;
	try{
	    o = in.readObject();
	}
	catch(Exception e){
	    e.printStackTrace();
	}
	return o;
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
	String aux[];
	String servidor;
	String archivo;

	/* comunicacion fotos - nodo */
	/* buscar fotos */
	if (cmd[0].equalsIgnoreCase("C") && cmd.length == 3){
	    if (cmd[1].equalsIgnoreCase("-t") || cmd[1].equalsIgnoreCase("-k")){
		//recibir(in);
		dfs_distribuido(cmd[1] + " " + cmd[2], new Vector<String>());
		//enviar(out,recibir(in));
		return 0;
	    }
	    else return -1;
	}
	/* solicitar foto */
	else if (cmd[0].equalsIgnoreCase("D") && cmd.length == 2 && cmd[1].matches("[\\S]+[:][\\S]+")){
	    aux = cmd[1].split(":");
	    servidor = aux[0];
	    archivo = aux[1];
	    System.out.println("Solicitud de foto " + archivo + " a servidor " + servidor);
	    return 0;
	}
	/* obterner numero de vecinos */
	else if (cmd[0].equalsIgnoreCase("A") && cmd.length == 1){
	    enviar(out, "Numero de Vecinos: " + nodos_vecinos.size());
	    return 0;
	}
	/* salir */
	else if (cmd[0].equalsIgnoreCase("Q") && cmd.length == 1){
	    return 1; /* para enviar mensaje al cliente y cerrar conexion */
	}
	/* fin comunicacion fotos - nodo */

	/* comunicacion nodo - nodo */
	else if (cmd[0].equalsIgnoreCase("B") && cmd.length == 3){
	    String res = "";
	    Vector<String> v = null;
	    try {
		v = (Vector<String>)recibir(in);
	    }
	    catch (Exception e) {
		System.out.println(e.getMessage());
	    }
	    dfs_distribuido(cmd[1] + " " + cmd[2],v);
	    //enviar(out,recibir(in));
	    return 0;
	}
	else return -1;
	/* fin comunicacion nodo - nodo */
    }
	
    public static Vector<String> Vecinos (String traza) {
	File archivo = null;
	FileReader fr = null;
	BufferedReader br = null;
	Vector <String> ln  = new Vector <String>();
	try {
	    // Apertura del fichero y creacion de BufferedReader para poder
	    // hacer una lectura comoda (disponer del metodo readLine()).
	    archivo = new File (traza);
	    fr = new FileReader (archivo);
	    br = new BufferedReader(fr);
	    
	    // Lectura del fichero
	    String linea;
	    while((linea = br.readLine()) != null && !linea.equals("127.0.0.1") && !linea.equals("localhost")){
		linea = InetAddress.getByName(linea).getHostAddress();
		ln.addElement(linea);
	    }
	}
	catch(EOFException e) {
	    return ln;
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
	    nodos_vecinos = Vecinos(maquinas);
	 
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
	    //	    System.out.println("blah: "+socket.getInetAddress().getHostAddress());
	    // streams
	    out = new ObjectOutputStream(socket.getOutputStream());
	    out.flush();
	    in = new ObjectInputStream(socket.getInputStream());

	    enviar(out,"<exito/>");

	    do{
		//try{
		    mensaje = (String)recibir(in);
		    if (mensaje.equals("<bye/>"))
			break;
		    System.out.println(mensaje);
		    
		    switch(verificar_comando(mensaje)) {
		    case -1:
			enviar(out,"Comando invalido");
			mensaje = ""; // para evitar que coincida con bye
			break;
		    case 1:
			mensaje = "<bye/>"; // para que salga el servidor
			enviar(out,mensaje); // para que salga el cliente
			break;
		    default:
			//mensaje = "<bye/>";
			break;
		    }
		    //	}
	    //catch(IOException ioe){
	    //	    System.err.println("I/O ERROR: " + ioe.getMessage());
	    //}
		//catch(ClassNotFoundException classnot){
		//  System.err.println("Data received in unknown format");
		//}
	    }while(!mensaje.equals("<bye/>"));

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
    
    public void dfs_distribuido (String busqueda, Vector<String> visitados){

	String resultado = "";
	File archivo = new File(directorio);
	String[] archivos_xml = archivo.list(new explorador(".xml"));
	Socket sock = null;
	ObjectOutputStream salida = null;
	ObjectInputStream entrada = null;
	
	/* busqueda local */
	for (int i = 0; i < archivos_xml.length; i++) {
	    if (match(archivos_xml[i], busqueda)){
		resultado = resultado + archivos_xml[i] + "\n";
	    }
	}
	try {
	    System.out.println("0. Servidor: "+ InetAddress.getLocalHost().getHostName()+"\nresultado: " + resultado);
	}
	catch (Exception e){
	    System.err.println("here1: " + e.getMessage());
	}
	/* marcar como visitado */
	visitados.add(socket.getInetAddress().getHostAddress());

	System.out.println(nodos_vecinos);

	/* busqueda remota */
	try {
	    for (int i = 0; i < nodos_vecinos.size(); i++) {
		if (!visitados.contains(nodos_vecinos.elementAt(i))){
		    System.out.println("Visitados: " + visitados);
		    sock = new Socket(nodos_vecinos.elementAt(i),puerto);
		    salida = new ObjectOutputStream(sock.getOutputStream());
		    entrada = new ObjectInputStream(sock.getInputStream());
		    System.out.println("A: "+recibir(entrada));
		    
		    enviar(salida,"B " + busqueda);
		    enviar(salida,visitados);
		    
		    System.out.println("1. Servidor: "+ InetAddress.getLocalHost().getHostName()+"\nresultado: " + resultado);
		    resultado = resultado + (String)entrada.readObject();
		    System.out.println("2. Servidor: "+ InetAddress.getLocalHost().getHostName()+"\nresultado: " + resultado);
		    enviar(salida,"<bye/>");
		    salida.close();
		    entrada.close();
		    sock.close();
		}
	    }
	    
	    enviar(out,resultado);
	    
	}
	catch (Exception e){
	    System.err.println("here: " + e.getMessage());
	}
	System.out.println("wasa");
    } 

    public boolean match (String archivo, String busqueda) {

	XMLElement xml = new XMLElement();
	FileReader reader = null;
	Vector children = null;
	String nombre_elem;
	String contenido_elem;
	String tipo_busqueda = (busqueda.split("[\\s]+"))[0];
	String cadena = (busqueda.split("[\\s]+"))[1];
	String atributo = null;
	Pattern patron = Pattern.compile(cadena,Pattern.CASE_INSENSITIVE);
	Matcher aux;
	
	try {
	    reader = new FileReader(archivo);
	    xml.parseFromReader(reader);
	    children = xml.getChildren();
	}
	catch (IOException e) {
	    System.err.println(e.getMessage());
	}

	if (tipo_busqueda.equalsIgnoreCase("-t")){	
	    for (int i = 0; i < children.size(); i++){
		/* Se obtiene el nombre del tag */
		nombre_elem = ((XMLElement)children.elementAt(i)).getName();
		/* Verificacion que el tag sea titulo */		
		if (nombre_elem.equals("titulo")) {
		    /* Se obtiene el contenido del tag titulo */
		    contenido_elem = ((XMLElement)children.elementAt(i)).getContent();
		    /* Se verifica si hay un substring con la cadena dada */
		    aux = patron.matcher(contenido_elem);
		    if (aux.find()){
			return true;
		    }
		    return false;
		}
	    }
	}
	else {
	    for (int i = 0; i < children.size(); i++){
		/* Se obtiene el nombre del tag */
		nombre_elem = ((XMLElement)children.elementAt(i)).getName();
		/* Se verifica que el tag sea palabrasClave */
		if (nombre_elem.equals("palabrasClave")){
		    /* Para c/entrada */
		    children = ((XMLElement)children.elementAt(i)).getChildren();
		    for (int j = 0; j < children.size(); j++){
			contenido_elem = (String)((XMLElement)children.elementAt(j)).getAttribute("palabra");
			aux = patron.matcher(contenido_elem);
			if (aux.find()){
			    return true;
			}
		    }
		    return false;
		}
	    }
	} 
	return false; // no necesario si el xml esta bien hecho
    }

    public static void main(String args[]) throws Exception {
	int puerto = 0;
	int argc = args.length;
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
	
	nodo servidor = new nodo(puerto,directorio);
	//	System.out.println(InetAddress.getByName("carlos"));

	while(true){
	    servidor.run(puerto, maquinas, traza);
	}
    }
}

