.PHONY: all compile server client clean

all: compile

compile:
	javac -cp . Parser.java TCPServer.java TCPClient.java

server: compile
	java -cp . TCPServer

client: compile
	java -cp . TCPClient

clean:
	rm -f *.class
