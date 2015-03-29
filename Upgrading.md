This file describes how to upgrade your Cerberus-Prox installation to the
current release.

## From 1.1 ##
  * Run upgrade\_from\_1.1.sql script on the database.
  * Update /etc/init.d/doorsystem - startup class name has changed.
  * Update cerberus-prox.jar
  * Merge new entires in doorsystem.properties with your existing configuration file.
  * Merge new entries in log4j. properties to take advantage of the new log class.