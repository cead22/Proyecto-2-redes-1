all: nodo.class fotos.class explorador.class
 
Nodo.class : nodo.java explorador.java
	javac -classpath nanoxml.jar:. nodo.java
 
Fotos.class : fotos.java 
	javac -classpath nanoxml.jar:. fotos.java
 
explorador.class : explorador.java
	javac -classpath nanoxml.jar:. explorador.java