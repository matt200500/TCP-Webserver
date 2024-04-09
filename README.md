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
Java .\ServerDriver 
```
Using the above command, you can iniltialize the server with its default parameters. You can specify your own parameters using the command line options described below. You do not need to change the default values for options -p, -t and -r.
```
-p port number (default = 2025)
-t idle connection timeout (in milliseconds) (0 = default = no timeout)
-r directory of web server (default is current working directory)
```
Example way to run server with default parameters (no arguments): 
```
Java .\ServerDriver 
```
