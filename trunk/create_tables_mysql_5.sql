-- phpMyAdmin SQL Dump
-- version 2.11.8.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Aug 15, 2008 at 12:53 PM
-- Server version: 5.0.51
-- PHP Version: 5.2.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `door`
--

-- --------------------------------------------------------

--
-- Table structure for table `access_group`
--

CREATE TABLE IF NOT EXISTS `access_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

-- --------------------------------------------------------

--
-- Table structure for table `access_log`
--

CREATE TABLE IF NOT EXISTS `access_log` (
  `id` int(11) NOT NULL auto_increment,
  `logged` datetime NOT NULL,
  `card_id` varchar(50) default NULL,
  `door` varchar(50) default NULL,
  `action` enum('DENY','ALLOW') default NULL,
  `detail` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  KEY `logged` (`logged`,`card_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=857 ;

-- --------------------------------------------------------

--
-- Table structure for table `card`
--

CREATE TABLE IF NOT EXISTS `card` (
  `card_id` varchar(50) NOT NULL,
  `user` varchar(255) NOT NULL,
  `after_hours` enum('N','Y') NOT NULL,
  `access_group_id` int(11) NOT NULL,
  `expires` datetime NOT NULL,
  `valid_from` datetime NOT NULL,
  `disabled` enum('N','Y') NOT NULL,
  `magic` enum('N','Y') NOT NULL,
  PRIMARY KEY  (`card_id`),
  KEY `access_group_id` (`access_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `door`
--

CREATE TABLE IF NOT EXISTS `door` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL,
  `default_unlocked` enum('Y','N') NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

-- --------------------------------------------------------

--
-- Table structure for table `door_access`
--

CREATE TABLE IF NOT EXISTS `door_access` (
  `id` int(11) NOT NULL auto_increment,
  `door_id` int(11) NOT NULL,
  `access_group_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `door_id` (`door_id`),
  KEY `access_group_id` (`access_group_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=10 ;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `door_access`
--
ALTER TABLE `door_access`
  ADD CONSTRAINT `door_access_ibfk_2` FOREIGN KEY (`access_group_id`) REFERENCES `access_group` (`id`),
  ADD CONSTRAINT `door_access_ibfk_1` FOREIGN KEY (`door_id`) REFERENCES `door` (`id`);
