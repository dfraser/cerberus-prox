
```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<door_event>
   <allowed>true</allowed> 
   <cardId>40-12345</cardId>
   <doorName>front</doorName>
   <magic>false</magic>
   <nickName>Optic</nickName>
   <realName>Dan Fraser</realName>
   <timeRead>2010-11-21T21:59:26.529-05:00</timeRead>
   <unknown>false</unknown>
</door_event>
```


---


**allowed** is true iff access was granted and the door was opened.

**magic** is true if the card used has the magic flag set

**unknown** is true if the card wasn't found in the database.  This probably means that 'allowed' and 'magic' will also be false, and it definitely means that the **nickName** and **firstName** fields will be undefined.