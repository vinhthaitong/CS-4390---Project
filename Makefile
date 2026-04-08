JAVAC = javac
JAVA = java

CLASSPATH = .
SOURCES = Parser.java TCPServer.java TCPClient.java

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
