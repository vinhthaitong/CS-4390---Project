JAVAC = javac
JAVA = java

CLASSPATH = .:lib/exp4j-0.4.8.jar
SOURCES = TCPServer.java TCPClient.java

.PHONY: all compile server client clean

all: compile

compile:
	$(JAVAC) -cp $(CLASSPATH) $(SOURCES)

server: compile
	$(JAVA) -cp $(CLASSPATH) TCPServer

client: compile
	$(JAVA) -cp $(CLASSPATH) TCPClient

clean:
	rm -f *.class
