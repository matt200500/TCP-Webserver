# TCP-Webserver
Creates a Local TCP server, allowing clients to connect and create GET requests

## How to run:
```
Javac .\WebServer.java
```
```
Javac .\ServerDriver.java
```
```
Javac .\ServerUtils.java 
```

```
Java .\ServerDriver -p "port number (default = 2025)" -t "idle connection timeout (in milliseconds) (0 = default = no timeout)" -r "directory of web server (default is current working directory)"
```

Example way to run server with default parameters (no arguments): 
```
Java .\ServerDriver 
```
