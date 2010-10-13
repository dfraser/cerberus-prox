--
-- Copyright 2010 Egeste
--
-- This file is part of the ATX Hackerspace implementation of Cerberus-Prox.
--
-- Cerberus-Prox is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- Cerberus-Prox is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with Cerberus-Prox.  If not, see <http://www.gnu.org/licenses/>.
-- 

--
-- This file will update the cerberus-prox database to support multiple access groups

ALTER TABLE card DROP access_group_id;

CREATE TABLE IF NOT EXISTS `card_group` (
  `id` int(11) NOT NULL auto_increment,
  `card_id` varchar(50) character set latin1 NOT NULL,
  `access_group_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;