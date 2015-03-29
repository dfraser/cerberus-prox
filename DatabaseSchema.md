# Introduction #

The database for cerberus-prox is fairly simple and easy to manipulate by hand.  This document will describe the various tables and explain what to put in them.

I recommend installing a database management tool such as PhpMyAdmin to manage configurations and users.  The provided database script creates appropriate foreign-key constraints, so if you install PhpMyAdmin properly and configure its metadata tables, you will get nice drop-down choosers where appropriate when editing the database.

## Doors Table ##

The doors table enumerates all of the doors in the system.  This table also includes a field which indicates whether or not the door should be unlocked by default.  If you change the value of this field, the state of the door lock will change the next time the cache is reloaded by the application.

## Access\_Groups Table ##

This table is a list of the access groups involved in the system.   An access group is a set of doors which can be opened by a specific person.  I would suggest making an "Admin" group which contains all doors, and then other groups as you see fit.

## Door\_Access  Table ##

This table represents the relationship between doors and access groups.  To give an Access Group access to a door, insert a row into this table.

## Cards Table ##

This table represents the authorized users of the system.  Each user has a `card_id` in the format `<premise code>-<card code>`.  You can enter the user's name, a start date, and an expiry date for your convenience.  The 'disabled' field will disable a card (disallow access) without removing it from the database.  The `magic` field will create a "magic" card, which can be used to toggle the locked/unlocked state of a door.

If the expiry date for a card is NULL, the card will be valid forever in the future.  If the valid\_from for a card is NULL, the card will be valid forever in the past.

To find the `card_id` of an unknown card, you can present it to a reader and then take a look at the end of the log file or the `access_log` table in the database.

## Access\_Log Table ##

This table shows all system activity.  An entry is added every time a card is read by the system, and what action was taken as a result.  This is useful for auditing.