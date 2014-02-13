# 18-842 Distributed Systems
 
CLASSPATH = ../lib/snakeyaml-1.11.jar

all:
	javac -cp $(CLASSPATH) *.java
	cp sample_config.yml ../bin/
	cp sample_config.yml ../
	mv *.class ../bin/
clean:
	rm -rf ../bin/*.class
