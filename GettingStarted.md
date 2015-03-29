# Introduction #

This page will help you get the Cerberus-Prox Java software up and running.


## Prerequisites ##

Cerberus-Prox depends on several third-party Java libraries, which should be available in your classpath.  Versions in parenthesis are the ones used in development, but newer versions should work fine.

  * [Log4J](http://logging.apache.org/log4j/1.2/index.html) (1.2.15)
  * [RXTX](http://users.frii.com/jarvi/rxtx/) -- plus installed JNI binary (2.1-7)
  * [Mysql Connector/J](http://www.mysql.com/products/connector-j) (5.1.6)
  * [Commons-Daemon](http://commons.apache.org/daemon/) (1.0.1) -- jar plus compiled jsvc binary **OPTIONAL**
  * [Jetty](http://www.mortbay.org/jetty-6/) (6.1.11) **OPTIONAL**
  * [Redstone XML-RPC](http://xmlrpc.sourceforge.net/) (1.1) **OPTIONAL**

You should end up with the following jars in your classpath:

  * log4j-1.2.15.jar
  * RXTXcomm.jar
  * mysql-connector-java-5.1.6-bin.jar
  * commons-daemon.jar **OPTIONAL**
  * jetty-6.1.11.jar  **OPTIONAL**
  * jetty-util-6.1.11.jar **OPTIONAL**
  * servlet-api-2.5-6.1.11.jar  **OPTIONAL**
  * xmlrpc-1.1.jar **OPTIONAL**

Dependencies marked as **OPTIONAL** are only required if their respective features are used. Commons-Daemon is required if you intend to use jsvc and the Daemon class to control the server.  Jetty and XML-RPC are only required if you wish to enable the xml-rpc server.

## Installation on Linux-Style Systems ##

This installation instruction will cover RedHat-style systems.  Your system may vary.  This code has been tested to work on Fedora and Windows.

### Install Java and set your JAVA\_HOME ###

  * Cerberus-Prox requires at least Java5 JRE.
  * It can be anywhere on your system, but point JAVA\_HOME appropriately.

### Setup MySQL ###

  * Set up MySQL (or other SQL database, but only MySQL has been tested).
  * Create a database and install the Cerberus-Prox schema from the included SQL script.

```
mysql -u root -p door_database < sql/create_tables_mysql_5.sql 
```

  * Set up a MySQL user and password with access to insert, update and select from your new database.

  * Double-check your settings by connecting as this user from another MySQL client.

  * Add one or more Door definitions into the 'door' table, add an access group, and grant access to your door.

```
mysql> insert into door (id, name,default_unlocked) values (1,'front','N');

mysql> insert into access_group (id,name) values (1,'General');

mysql> insert info door_access (door_id, access_group_id) values (1,1);
```

### Unpack the Distribution Tarball ###

```
cd /usr/local
tar xf cerberus-prox-1.0.tar.gz
```

### Prepare the Prerequisites ###

  * Copy the compiled jsvc binary into the distribution 'bin' directory and make it executable.
  * Copy the librxtxSerial.so from the RXTX distribution into ${JAVA\_HOME}/lib/i386
  * Please the prerequisite jar files (see above) into the distribution 'lib' directory.
  * Copy the distribution init.d/doorsystem file to /etc/init.d, and check to make sure the variables at the top of the file are sane.  Make sure it's executable.

```
cp /usr/local/src/commons-daemon/bin/jsvc bin/
cp /usr/local/src/rxtx-2.1-7/linux-i386/rxtxSerial.so ${JAVA_HOME}/lib/i386
cp init.d/doorsystem /etc/init.d/
chmod 755 /etc/init.d/doorsystem
```

### Edit the Properties File ###

  * open doorsystem.properties in your favourite editor.
  * define door0 and port0 for the door you created in the database and the serial port connected to your Cerberus-Prox board.  The door name must match exactly or things won't work for you.
  * define the MySQL connection information for your new database, user and password.

### Run it! ###

```
service doorsystem start
```

Everything will work perfectly the first time.

(just kidding)

### Troubleshoot It ###

This is a complex system with a lot of moving parts, so it probably won't work for you the first time.  Don't be alarmed.  We will help you through it.

Start by editing /etc/init.d/doorsystem and adding '-debug' to the beginning of the jsvc command line in the start() section.  Try the startup again, and this time you will get much more debug output.  This will show you if you have any problems with rxtx, missing libraries, or that sort of thing.