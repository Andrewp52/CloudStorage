Cloud storage.

POM based project.

Requires: Java-11

Server based on NETTY.

Server configuring - config files: (netserver.conf and database.conf)

Config files format: param=value
* database.conf supported parameters and default values:
	- dbHost=localhost
	- dbPort=3306
	- dbName=cloudstor
	- dbLogin=test
	- dbPass=test
	- dbTimeout=10

* netserver.conf supported parameters and default values:
	- port=9999
	- srvRoot=server
	- maxFrameSize=1024
	
	'srvRoot' may be absolute or relative path (creates automatically if not exist).
	
Available database connectors: MySQL

* Build and start:

- Build: ...
- Start: (in `target` dir)  java -jar Server-1.0-SNAPSHOT-jar-with-dependencies.jar  

<hr>
Client - JAVAFX application.

Double-sided commander like file manager.
* Features:
	- User registration
	- Profile editing
	- Navigation
	- Basic file operations (copy, cut, paste...) including cross-panel
	- Search (local / remote)
	- Upload / Download
	- File preview (images & text).
	
* Build and start:

- Build: ...
- Start: (in `target` dir)  java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar  
  
