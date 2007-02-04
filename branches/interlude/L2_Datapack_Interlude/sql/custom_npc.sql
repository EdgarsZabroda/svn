-- 
-- Table structure for table `npc`
-- 
DROP TABLE IF EXISTS `npc`;
CREATE TABLE `npc` (
  `id` decimal(11,0) NOT NULL default '0',
  `idTemplate` int(11) NOT NULL default '0',
  `name` varchar(200) default NULL,
  `serverSideName` int(1) default '0',
  `title` varchar(45) default '',
  `serverSideTitle` int(1) default '0',
  `class` varchar(200) default NULL,
  `collision_radius` decimal(5,2) default NULL,
  `collision_height` decimal(5,2) default NULL,
  `level` decimal(2,0) default NULL,
  `sex` varchar(6) default NULL,
  `type` varchar(20) default NULL,
  `attackrange` int(11) default NULL,
  `hp` decimal(8,0) default NULL,
  `mp` decimal(5,0) default NULL,
  `str` decimal(7,0) default NULL,
  `con` decimal(7,0) default NULL,
  `dex` decimal(7,0) default NULL,
  `int` decimal(7,0) default NULL,
  `wit` decimal(7,0) default NULL,
  `men` decimal(7,0) default NULL,
  `exp` decimal(9,0) default NULL,
  `sp` decimal(8,0) default NULL,
  `patk` decimal(5,0) default NULL,
  `pdef` decimal(5,0) default NULL,
  `matk` decimal(5,0) default NULL,
  `mdef` decimal(5,0) default NULL,
  `atkspd` decimal(3,0) default NULL,
  `aggro` decimal(6,0) default NULL,
  `matkspd` decimal(4,0) default NULL,
  `rhand` decimal(4,0) default NULL,
  `lhand` decimal(4,0) default NULL,
  `armor` decimal(1,0) default NULL,
  `walkspd` decimal(3,0) default NULL,
  `runspd` decimal(3,0) default NULL,
  `faction_id` varchar(40) default NULL,
  `faction_range` decimal(4,0) default NULL,
  `isUndead` int(11) default '0',
  `absorb_level` decimal(2,0) default '0',
  `hpreg` decimal(8,2) default NULL,
  `mpreg` decimal(5,2) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
#----------------------------
# Records for table npc
#----------------------------

insert  into npc values 
('6001', 31774, 'Beryl the Cat', 0, 'ItemMall', 1, 'Monster2.queen_of_cat', '8.00', '15.00', '70', 'female', 'L2Npc', 40, '3862', '1493', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '253', '0', '0', '0', '80', '120', null, '0', 0, '0', null, null),
('6002', 35461, 'Caska', 1, 'NPC Buffer', 1, 'NPC.a_teleporter_FHuman', '8.00', '25.00', '70', 'female', 'L2Npc', 40, '3862', '1494', '40', '43', '30', '21', '20', '10', '5879', '590', '1444', '514', '760', '381', '253', '0', '253', '0', '0', '0', '80', '120', null, null, 0, '0', null, null),
('7077', 31275, 'Tinkerbell', 1, 'Luxary Gatekeeper', 1, 'NPC.a_teleporter_FHuman', '8.00', '25.00', '70', 'female', 'L2Teleporter', 40, '3862', '1494', '40', '43', '30', '21', '20', '10', '5879', '590', '1444', '514', '760', '381', '253', '0', '253', '0', '0', '0', '80', '120', null, null, 0, '0', null, null),
('2001', 29020, 'Baium', 1, 'Event', 1, 'Monster.baium', '65.00', '174.00', '75', 'male', 'L2Boss', 40, '790857', '3347', '60', '57', '73', '76', '35', '80', '10253400', '1081544', '6559', '6282', '4378', '4601', '333', '0', '2362', '0', '0', '0', '80', '120', null, '0', 0, '12', '668.78', '3.09'),
('2002', 25319, 'Ember', 1, 'Event', 1, 'Monster2.inferno_drake_100_bi', '48.00', '73.00', '85', 'male', 'L2RaidBoss', 40, '257725', '3718', '60', '57', '73', '76', '35', '80', '2535975', '1356048', '11906', '5036', '18324', '2045', '409', '0', '2901', '0', '0', '0', '80', '120', null, '0', 0, '13', '823.48', '9.81'), 
('2003', 29022, 'Zaken', 1, 'Event', 1, 'Monster.zaken', '16.00', '32.00', '60', 'male', 'L2Boss', 40, '858518', '1975', '60', '57', '73', '76', '35', '80', '4879745', '423589', '7273', '2951', '19762', '1197', '333', '0', '2362', '0', '0', '0', '80', '120', null, '0', 1, '12', '799.68', '2.45');
