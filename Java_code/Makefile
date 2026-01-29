MAIN_CLASS = Main.class

JAVAC = javac
JAVA = java
SOURCES = %.java

PATH = ../Java_execute

all: run

compile:
	javac -d ../Java_execute *.java

run: compile
	java -cp ../Java_execute Main

clean:
	rm -f $(PATH)/Java_code/*.class