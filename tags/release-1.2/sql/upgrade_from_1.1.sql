--
-- Copyright 2008 Dan Fraser
--
-- This file is part of Cerberus-Prox.
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
-- This file will update the cerberus-prox database from version 1.1 to
-- to be compatible with the current release.

ALTER TABLE card ADD nick VARCHAR(255);

ALTER TABLE  `card` CHANGE  `expires`  `expires` DATETIME NULL,
	CHANGE  `valid_from`  `valid_from` DATETIME NULL;
