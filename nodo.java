import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import nanoxml.*;

/**
 * Clase para representar los nodos conectados a la red.
 * @author Carlos Alvarez y Marion CArambula.
 */
public class nodo {
    /** Socket por el cual se escuchan
     * peticiones de conexion */
    private ServerSocket serversock;
    /** Socket por el cual se establece
     * la comunicacion con la aplicacion
     * fotos */
    private Socket socket;
    /** Canal por el cual se 
     * envian streams de datos */
    private ObjectOutputStream out;
    /** Canal por el cual se 
     * reciben streams de datos */
    private ObjectInputStream in;
    /** Comando recibido de 
     * consulta/transferencia */
    private String mensaje;
    /** Puerto logico por el cual 
     * se establece la comunicacion */
    private int puerto;
    /** directorio donde se encuentran
     * las fotos */
    String directorio;
    /** computadores alcanzables desde este */
    Vector<String> nodos_vecinos;

   /** 
    * Crea un nodo a partir de un puerto y un directorio.
    * @param p puerto a traves el cual se hara la conexion.
    * @param dir directorio donde.
    */
    public nodo(int p, String dir){
	directorio = dir;
	puerto = p;
	socket = null;
    }


     /** 
     * Envia un mensaje determinado a traves de una salida
     * especificada.
     * @param out salida a traves de la cual se mandara el mensaje.
     * @param mensaje mensaje a enviar.
     * @throws IOException
     */
    private void enviar (ObjectOutputStream out, Object mensaje){
	try{
	    out.writeObject(mensaje);
	    out.flush();
	}
	catch(IOException e){
	    e.printStackTrace();
	}
    }

     /** 
     * Recibe un mensaje a traves de un canal de entrada especificado
     * @param Canal de entrada del mensaje.
     * @throws IOException
     */
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
    
    /** 
     * Convierte un arreglo en una cadena de caracteres.
     * @param arr[] Arreglo de String a convertir en una cadena.
     * @return La cadena de caracteres.
     */
    public String cadena(String arr[]) {
	String res = "";
	for (int i = 0; i < arr.length; i++){
	    res += arr[i] + ",";
	}
	return res;
    }

    /** 
     * Verifica una cadena de caracteres que representa un   
     * comando y ejecuta la accion pertinente.
     * @param comando String que indica la accion a tomar.
     * @return 0 si la accion fue llevada a cabo exitosamente -1 si fracaso 1 si el comando recibido fue q (terminar la conexion).
     */
    private int verificar_comando(String comando,BufferedWriter traza) {
	String cmd[] = comando.split("[\\s]+");
	String aux[];
	String servidor;
	String archivo;

	/* comunicacion fotos - nodo */
	/* buscar fotos */
	if (cmd[0].equalsIgnoreCase("C") && cmd.length == 3){
	    if (cmd[1].equalsIgnoreCase("-t") || cmd[1].equalsIgnoreCase("-k")){
		try{
		    traza.write("Consulta   ");
		    traza.write(comando);
		    traza.write("  recibida desde " + socket.getInetAddress().getHostName()+"\n");
		    traza.flush();
		}
		catch(Exception e){
		    System.out.println("error");
		}
		//recibir(in);
		dfs_distribuido(cmd[1] + " " + cmd[2], new Vector<String>());
		//recibir(in);
		return 0;
	    }
	    else return -1;
	}
	/* solicitar foto */
	else if (cmd[0].equalsIgnoreCase("D") && cmd.length == 2 && cmd[1].matches("[\\S]+[:][\\S]+")){
	    aux = cmd[1].split(":");
	    servidor = aux[0];
	    archivo = aux[1];
	    try{
		traza.write("Peticion de foto    ");
		traza.write(comando);
		traza.write("  recibida desde " + socket.getInetAddress().getHostName()+"\n");
		traza.flush();
	    }
	    catch(Exception e){
		System.out.println("error");
	    }
	    //System.out.println("Solicitud de foto " + archivo + " a servidor " + servidor);
	    
	    try {
		enviar_archivo(archivo);
	    }catch(Exception e) {
		e.getMessage();
	
		e.printStackTrace();
		System.exit(-1);
	    }
	    return 0;
	}
	/* obterner numero de vecinos */
	else if (cmd[0].equalsIgnoreCase("A") && cmd.length == 1){
	    try{
                traza.write("Solicitud de numeros de vecinos recibida desde " + socket.getInetAddress().getHostName()+"\n");
                traza.flush();
            }
            catch(Exception e){
                System.out.println("error");
            }

	    enviar(out, "Numero de Vecinos: " + nodos_vecinos.size());
	    return 0;
	}
	/* salir */
	else if (cmd[0].equalsIgnoreCase("Q") && cmd.length == 1){
	    try{
                traza.write("Solicitud para cerrar conexion recibida desde " + socket.getInetAddress().getHostName()+"\n");
                traza.flush();
            }
            catch(Exception e){
                System.out.println("error");
            }

	    return 1; /* para enviar mensaje al cliente y cerrar conexion */
	}
	/* fin comunicacion fotos - nodo */

	/* comunicacion nodo - nodo */
	else if (cmd[0].equalsIgnoreCase("B") && cmd.length == 3){
	    String res = "";
	    Vector<String> visitados = null;
	    try {
		visitados = (Vector<String>)recibir(in);
	    }
	    catch (Exception e) {
		System.out.println(e.getMessage());
	    }
	    visitados = dfs_distribuido(cmd[1] + " " + cmd[2],visitados);
	    enviar(out,visitados);
	    //enviar(out,recibir(in));
	    return 0;
	}
	else return -1;
	/* fin comunicacion nodo - nodo */
    }
	
    /** 
     * Obtiene las vecinos de un nodo y los almacena en
     * un vector.
     * @param traza Archivo donde se encuentran especificados los vecinos de un nodo.
     * @throws EOFException, FileNotFoundException
     * @return Vector de visitados
     */ 
    public static Vector<String> Vecinos (String maquinas) {
	File archivo = null;
	FileReader fr = null;
	BufferedReader br = null;
	Vector <String> ln  = new Vector <String>();
	try {
	    archivo = new File (maquinas);
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
	    System.err.println("El archivo "+ maquinas+" no existe.");
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
    
    /** 
     * Lleva a cabo la creacion del socket y ejecuta todas las acciones
     * pertinentes para la realizacion del intercambio de informacion y 
     * fotos.
     * @param puerto Puerto donde se llevara a cabo la conexion.
     * @param maquinas Archivo donde se almacenan los vecinos del nodo.
     * @param log Archivo donde se escribe el resultado de la busqueda.
     * @throws BindException, IOException
     */
    public void run(int puerto, String maquinas, String log){
	try{
	    String cliente;
	    BufferedWriter traza2 = new BufferedWriter(new FileWriter(log,true));
	    
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
	    
	    System.out.println("Conexion establecida");
	    out = new ObjectOutputStream(socket.getOutputStream());
	    out.flush();
	    in = new ObjectInputStream(socket.getInputStream());
	    enviar(out,"<exito/>");

	    do{
		mensaje = (String)recibir(in);
		if (mensaje.equals("<bye/>"))
		    break;
		System.out.println(mensaje);
		
		switch(verificar_comando(mensaje,traza2)) {
		case -1:
		    enviar(out,"Comando invalido");
		    mensaje = ""; // para evitar que coincida con bye
		    break;
		case 1:
		    mensaje = "<bye/>"; // para que salga el servidor
		    enviar(out,mensaje); // para que salga el cliente
		    break;
		default:
		    break;
		}
	    } while(!mensaje.equals("<bye/>"));

	    in.close();
	    out.close();
	    traza2.close();
	    serversock.close();
	}
	catch(IOException ioException){
	    ioException.printStackTrace();
	}
    }
    
    /** 
     * Indica la forma correcta de hacer la llamada al programa.
     * @param s String a imprimir por pantalla.
     */
    public static void uso(String s){
	System.out.println(s);
	System.out.println("Uso: nodo -p <puerto> -f <maquinas> -l <archivoTrazas> -d <directorio>");
	System.exit(-1);
    }
    
    /** 
     * Indica la forma correcta de hacer la llamada al programa.
     */
    public static void uso(){
	System.out.println("Uso: nodo -p <puerto> -f <maquinas> -l <archivoTrazas> -d <directorio>");
	System.exit(-1);
    }
    /** 
     * Obtiene el ip publico de la maquina donde se esta ejecutando el programa
     * @return Ip Publico
     * @throws  Exception
     */
    private String mi_ip () {
	Enumeration e1;
	Enumeration e2;
	NetworkInterface ni;
	String ip;
	try {
	    e1 = NetworkInterface.getNetworkInterfaces();
	    while(e1.hasMoreElements()) {
		ni = (NetworkInterface) e1.nextElement();
		e2 = ni.getInetAddresses();
		while (e2.hasMoreElements()){
		    ip = ((InetAddress) e2.nextElement()).getHostAddress();
		    if (ip.matches("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}") && !ip.equals("127.0.0.1")) {
			return ip;
		    }
		}
	    }
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	return "conexion nula";
    }

    /** 
     * Lleva a cabo un dfs para obtener informacion sobre los nodos vecinos
     * @param busqueda Indica que tipo de busqueda se esta realizado: por titulo o por palabras claves.
     * @param visitados Representa el vector de nodos que ya fueron visitados previamente.
     */
    public Vector<String> dfs_distribuido (String busqueda, Vector<String> visitados){
	String resultado = "";
	File archivo = new File(directorio);
	String[] archivos_xml = archivo.list(new explorador(".xml"));
	Socket sock = null;
	ObjectOutputStream salida = null;
	ObjectInputStream entrada = null;
	String foto;
	
	/* busqueda local */
	for (int i = 0; i < archivos_xml.length; i++) {
	    if (!(foto = match(directorio + "/" +archivos_xml[i], busqueda)).equals("<no/>")){
		resultado = resultado + "\n===\n+ Archivo: " + archivos_xml[i].substring(0,archivos_xml[i].length()-4) + "\n" + foto + "\n";
	    }
	}

	/* marcar como visitado */
	visitados.add(mi_ip());

	/* busqueda remota */
	try {
	    for (int i = 0; i < nodos_vecinos.size(); i++) {
		if (!visitados.contains(nodos_vecinos.elementAt(i))){
		    sock = new Socket(nodos_vecinos.elementAt(i),puerto);
		    salida = new ObjectOutputStream(sock.getOutputStream());
		    entrada = new ObjectInputStream(sock.getInputStream());
		    
		    visitados.add(nodos_vecinos.elementAt(i));		    

		    recibir(entrada);
		    
		    enviar(salida,"B " + busqueda);
		    enviar(salida,visitados);
		    
		    resultado = resultado + (String)recibir(entrada);
		    visitados = (Vector)recibir(entrada);
		    enviar(salida,"<bye/>");
		    salida.close();
		    entrada.close();
		    sock.close();
		}
	    }
	  
	}
	catch (Exception e){
	    System.err.println("here: " + e.getMessage());
	    e.printStackTrace();
	} finally {
	    enviar(out,resultado);
	    return visitados;
	} 
    }

    
    /** 
     * Hace una busqueda de una string dado en un archivo xml.
     * @param archivo Archivo donde se ejecutara la busqueda.
     * @param busqueda String a buscar en el archivo.
     * @return No en caso que no se haya conseguido el string en el archivo
     * en cualquier otro caso un string con ciertas caracteristicas.
     */
    public String match (String archivo, String busqueda) {

	XMLElement xml = new XMLElement();
	FileReader reader = null;
	Vector children = null;
	Vector sub_children = null;
	String nombre_elem;
	String contenido_elem;
	String tipo_busqueda = (busqueda.split("[\\s]+"))[0];
	String cadena = (busqueda.split("[\\s]+"))[1];
	String atributo = null;
	String titulo;
	String autor;
	String descripcion;
	String servidor;
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
			titulo = "- Titulo: " + contenido_elem + "\n";
			autor = "- Autor:\n\t" + ((XMLElement)children.elementAt(i+2)).getAttribute("name") + "\n";
			descripcion = "- Descripcion:" + ((XMLElement)children.elementAt(i+3)).getContent() + "\n";
			servidor = "- Servidor: \n\t" + mi_ip() + "\n===";
			return titulo + autor + descripcion + servidor;
		    }
		    return "<no/>";
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
		    sub_children = ((XMLElement)children.elementAt(i)).getChildren();
		    for (int j = 0; j < sub_children.size(); j++){
			contenido_elem = (String)((XMLElement)sub_children.elementAt(j)).getAttribute("palabra");
			aux = patron.matcher(contenido_elem);
			if (aux.find()){
			    titulo = "- Titulo: " + ((XMLElement)children.elementAt(i-4)) + "\n";
			    autor = "- Autor:\n\t" + ((XMLElement)children.elementAt(i-2)).getAttribute("name") + "\n";
			    descripcion = "- Descripcion:" + ((XMLElement)children.elementAt(i-1)).getContent() + "\n===";
			    servidor = "- Servidor: \n\t" + mi_ip() + "\n===";
			    return titulo + autor + descripcion + servidor;
			}
		    }
		    return "<no/>";
		}
	    }
	} 
	return "<no/>"; // no necesario si el xml esta bien hecho
    }

    public byte[] entero_a_arreglo(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }


    private void enviar_archivo(String archivo) {
	try {
	    File foto = new File (directorio + "/" + archivo);
	    byte [] bytes_foto  = new byte [(int)foto.length()];
	    int tam = bytes_foto.length;
	    FileInputStream fi = new FileInputStream(foto);
	    BufferedInputStream bi = new BufferedInputStream(fi);
	    ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
	    byte [] tamano = new byte[4];

	    bi.read(bytes_foto,0,bytes_foto.length);
	    tamano = entero_a_arreglo(tam);
	    System.out.println("sending");	    
	    salida.write(tamano,0,4);
	    salida.flush();


	    salida.write(bytes_foto,0,bytes_foto.length);
	    salida.flush();
	   
	    System.out.println("done sending");
	}
	catch (FileNotFoundException f) {
	    try {
		System.err.println("Foto no encontrada");
		byte [] nulo = new byte [4];
		for (int i = 0; i < 4; i++)
		    nulo[i] = 0x00;
		ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
		salida.write(nulo,0,4);
		salida.flush();
	    } 
	    catch (Exception e) {
		System.err.println("Error al escribir en socket");
	    }
	} catch (IOException i) {
	    System.err.println("Error E/S");
	} catch (Exception ex) {
	    System.err.println("Error");
	}
	
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
	//maquinas = dir_Act + "/"+maquinas;
	
	if (!(check[0] && check[1] && check[2] && check[3])) uso();
	//System.out.println("m"+ maquinas+ "\n traza " + traza);
	// Fin revision de parametros de llamada
	
	nodo servidor = new nodo(puerto,directorio);
	

	while(true){
	    servidor.run(puerto, maquinas, traza);
	}
    }
}

