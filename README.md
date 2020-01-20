# Jay's Simple Server
#### A basic/static web/HTTP server in Java

## How to run
Main method is in the Program.java file

## Command line args
 1) ``--port <port>``: sets the hosting port to ``<port>`` (default is 4589)  
 1) ``--name <name>``: sets the server's name to ``<name>`` (default is the pc's hostname; which, on Linux can be determined with the command ``hostname``).  
 1) ``--home <dir>``: sets the server's root directory to ``<dir>`` (default is the home folder)  

## Config files
Two files can be created to manipulate some data:  
 1) ``<root>/.filetable`` contains a translation table for HTTP paths, formatted as ``http-path=physical-path`` (relative to the server's root directory).  
 1) ``<root>/sev.conf`` contains the severity of some status codes, formatted as ``http-status=severity``. Higher severity statusses have precedence over lower ones.  

## Logs
The server keeps three log files, all in the (newly created) directory ``<user home folder>/.jserv/``:  
 1) ``~/.jserv/logFile`` keeps standard logs with events, errors and warnings.  
 1) ``~/.jserv/visitors`` keeps the IP addresses which visited the server, as well as their timestamp.
 1) ``~/.jserv/userAgents`` keeps track of which browsers (user-agents) were used to visit the server's pages.

## Terminal output
On stdout, the server writes the requests coming in and the responses going out.  
On stderr, the server shows the data written to ``~/.jserv/visitors`` and ``~/.jserv/userAgents`` as well as any (fatal) errors which might happen.
