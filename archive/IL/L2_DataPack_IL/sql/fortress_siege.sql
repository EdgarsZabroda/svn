--
-- Table structure for table `fortress_siege`
-- Done by Darki699
--

CREATE TABLE IF NOT EXISTS `fortress_siege` (
  `eventName` varchar(255) NOT NULL default '',
  `eventDesc` varchar(255) NOT NULL default '',
  `joiningLocation` varchar(255) NOT NULL default '',
  `minlvl` int(4) NOT NULL default '0',
  `maxlvl` int(4) NOT NULL default '0',
  `npcId` int(8) NOT NULL default '0',
  `npcX` int(11) NOT NULL default '0',
  `npcY` int(11) NOT NULL default '0',
  `npcZ` int(11) NOT NULL default '0',
  `npcHeading` int(11) NOT NULL default '0',
  `rewardId` int(11) NOT NULL default '0',
  `rewardAmount` int(11) NOT NULL default '0',
  `joinTime` int(11) NOT NULL default '0',
  `eventTime` int(11) NOT NULL default '0',
  `minPlayers` int(4) NOT NULL default '0',
  `maxPlayers` int(4) NOT NULL default '0',
  `centerX` int(11) NOT NULL default '0',
  `centerY` int(11) NOT NULL default '0',
  `centerZ` int(11) NOT NULL default '0',
  `team1Name` varchar(255) NOT NULL default '',
  `team1X` int(11) NOT NULL default '0',
  `team1Y` int(11) NOT NULL default '0',
  `team1Z` int(11) NOT NULL default '0',
  `team1Color` int(11) NOT NULL default '0',
  `team2Name` varchar(255) NOT NULL default '',
  `team2X` int(11) NOT NULL default '0',
  `team2Y` int(11) NOT NULL default '0',
  `team2Z` int(11) NOT NULL default '0',
  `team2Color` int(11) NOT NULL default '0',
  `flagX` int(11) NOT NULL default '0',
  `flagY` int(11) NOT NULL default '0',
  `flagZ` int(11) NOT NULL default '0',
  `innerDoor1` int(11) NOT NULL default '0',
  `innerDoor2` int(11) NOT NULL default '0',
  `innerDoor3` int(11) NOT NULL default '0',
  `innerDoor4` int(11) NOT NULL default '0',
  `outerDoor1` int(11) NOT NULL default '0',
  `outerDoor2` int(11) NOT NULL default '0',
  PRIMARY KEY  (`eventName`)
) DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fortress_siege`
--

INSERT INTO `fortress_siege` VALUES
('Abandoned Camp Fortress Siege', 'Siege the Abandoned Fortress', 'Gludin Village', 71, 81, 30373, -84801, 150939, -3129, 0, 57, 1000, 15, 45, 40, 200, -52766, 156487, -2079, 'Blue', -45494, 156091, -1489, 16711680, 'Red', -53000, 156581, -1897, 6684927, -52767, 156503, -1131, 18220003, 18220002, 18220005, 18220004, 18220008, 18220001),
('Alligator Beach Fortress Siege', 'Siege the Alligator Isle  Fortress', 'Town of Heine', 71, 81, 30290, 111372, 218705, -3466, 0, 57, 1000, 15, 45, 40, 200, 118417, 204933, -3333, 'Blue', 124978, 210106, -1855, 16711680, 'Red', 118622, 205071, -3176, 6684927, 118425, 204922, -2410, 23240002, 23240003, 23240007, 23240006, 23240008, 23240001),
('Border Outpost Fortress Siege', 'Siege the Outpost Fortress', 'Town of Oren', 71, 81, 31285, 81504, 53674, -1487, 0, 57, 1000, 15, 45, 40, 200, 111364, -15154, -1021, 'Blue', 110369, -20863, -462, 16711680, 'Red', 111371, -14834, -838, 6684927, 111359, -15120, -72, 23170004, 23170005, 23170009, 23170008, 23170001, 23170012),
('Cruma Tower Fortress Siege', 'Siege the Cruma Fortress', 'Elven Village', 71, 81, 30109, 45656, 50440, -3016, 0, 57, 1000, 15, 45, 40, 200, 11546, 94970, -3426, 'Blue', 8802, 101977, -2489, 16711680, 'Red', 11548, 95305, -3269, 6684927, 11518, 95004, -2503, 20200002, 20200003, 20200007, 20200006, 20200008, 20200001),
('Devastated Castle', 'Siege the Devastated Castle', 'Town of Aden', 71, 81, 35421, 147473, 26014, -2039, 0, 57, 1000, 15, 45, 40, 200, 177839, -18598, -2240, 'Blue', 184872, -15882, -1522, 16711680, 'Red', 177856, -18615, -2240, 6684927, 178298, -17623, -2201, 25170006, 25170005, 25170004, 25170003, 25170002, 25170001),
('Devils Pass Fortress Siege', 'Siege the Devils Pass Fortress', 'Town of Goddard', 71, 81, 30849, 147694, -55540, -2734, 0, 57, 1000, 15, 45, 40, 200, 100708, -55336, -645, 'Blue', 95458, -63046, -174, 16711680, 'Red', 100426, -55292, -514, 6684927, 100720, -55334, 277, 23160006, 23160007, 23160005, 23160004, 23160001, 23160008),
('Devotion Fortress Siege', 'Siege the Fortress of Devotion', 'Dark Elf Village', 71, 81, 30462, 12116, 16656, -4584, 0, 57, 1000, 15, 45, 40, 200, -53247, 91214, -2821, 'Blue', -55768, 84623, -1925, 16711680, 'Red', -53240, 91548, -2664, 6684927, -53229, 91221, -1898, 18200002, 18200003, 18200012, 18200011, 18200010, 18200001),
('Field of Silence Fortress Siege', 'Siege the Silenced Field Fortress', 'Heine', 71, 81, 30288, 111389, 219491, -3546, 0, 57, 1000, 15, 45, 40, 200, 73112, 185973, -2607, 'Blue', 65929, 175231, -2138, 16711680, 'Red', 72945, 186225, -2450, 6684927, 73121, 185988, -1658, 22230004, 22230005, 22230007, 22230006, 22230008, 22230001),
('Floran Agricultural Fortress Siege', 'Siege the Floran Fortress', 'Town of Dion', 71, 81, 30187, 15820, 142833, -2706, 0, 57, 1000, 15, 45, 40, 200, 5615, 149754, -2889, 'Blue', 338, 140472, -1608, 16711680, 'Red', 5332, 149737, -2732, 6684927, 5607, 149760, -1966, 20220024, 20220025, 20220023, 20220022, 20220021, 20220026),
('Forsaken Plains Fortress Siege', 'Siege the Forsaken Fortress', 'Town of Aden', 71, 81, 30474, 147141, 26170, -2048, 0, 57, 1000, 15, 45, 40, 200, 189926, 39731, -3410, 'Blue', 184375, 43263, -2884, 16711680, 'Red', 189935, 40059, -3253, 6684927, 189928, 39748, -2487, 25190006, 25190007, 25190009, 25190008, 25190001, 25190012),
('Giran Arena Fortress Siege', 'Siege the Giran Fortress', 'Town of Dion', 71, 81, 31321, 15788, 142848, -2706, 0, 57, 1000, 15, 45, 40, 200, 60271, 139432, -1754, 'Blue', 52771, 138768, -1235, 16711680, 'Red', 60332, 139719, -1623, 6684927, 60280, 139448, -857, 21220006, 21220007, 21220002, 21220003, 21220001, 21220008),
('Giran DVC Fortress Siege', 'Siege the Giran DVC Fortress', 'Town of Giran', 71, 81, 30066, 83360, 147903, -3405, 0, 57, 1000, 15, 45, 40, 200, 126085, 123330, -2585, 'Blue', 122293, 118941, -2241, 16711680, 'Red', 126089, 123591, -2429, 6684927, 126085, 123342, -1662, 23210007, 23210006, 23210011, 23210010, 23210001, 23210012),
('Hunters Fortress Siege', 'Siege the Hunters Village Fortress', 'Hunters Village', 71, 81, 30026, 117817, 76627, -2600, 0, 57, 1000, 15, 45, 40, 200, 125241, 95126, -2140, 'Blue', 133079, 100462, -991, 16711680, 'Red', 125237, 95416, -1984, 6684927, 125250, 95162, -1217, 23200003, 23200002, 23200009, 23200008, 23200001, 23200012),
('Ketra Orcs Fortress Siege', 'Siege the Ketra Fortress', 'Town of Goddard', 71, 81, 30862, 147715, -55515, -2734, 0, 57, 1000, 15, 45, 40, 200, 159155, -70315, -2864, 'Blue', 156275, -66893, -1696, 16711680, 'Red', 158901, -70140, -2707, 6684927, 159127, -70286, -1942, 24150004, 24150005, 24150006, 24150007, 24150003, 24150008),
('Langk Fortress Siege', 'Siege the Langk Fortress', 'Gludin Village', 71, 81, 30297, -82112, 150625, -3129, 0, 57, 1000, 15, 45, 40, 200, -22707, 219807, -3236, 'Blue', -15316, 205368, -2362, 16711680, 'Red', -22401, 219796, -3079, 6684927, -22693, 219801, -2313, 19240006, 19240007, 19240008, 19240009, 19240005, 19240010),
('Narsell Lake Fortress Siege', 'Siege the Lake Fortress', 'Town of Aden', 71, 81, 30689, 147422, 26009, -2013, 0, 57, 1000, 15, 45, 40, 200, 154908, 55311, -3254, 'Blue', 162137, 61611, -1945, 16711680, 'Red', 154700, 55473, -3098, 6684927, 154863, 55320, -2331, 24190009, 24190008, 24190007, 24190006, 24190010, 24190005),
('Northern Waterfall Fortress Siege', 'Siege the Waterfall Fortress', 'Town of Oren', 71, 81, 30699, 83114, 53833, -1465, 0, 57, 1000, 15, 45, 40, 200, 72859, 4267, -3045, 'Blue', 71847, -3991, -2711, 16711680, 'Red', 72695, 4468, -2888, 6684927, 72840, 4283, -2122, 22180004, 22180005, 22180002, 22180003, 22180008, 22180001),
('Oren Fortress Siege', 'Siege the Oren Fortress', 'Town of Oren', 71, 81, 30008, 83130, 53329, -1440, 0, 57, 1000, 15, 45, 40, 200, 79232, 91045, -2884, 'Blue', 86997, 104410, -1259, 16711680, 'Red', 79504, 91195, -2727, 6684927, 79262, 91069, -1961, 22200005, 22200004, 22200007, 22200006, 22200001, 22200012),
('Plunderous Plain Fortress Siege', 'Siege the Plunderous Fortress', 'Schuttgart', 71, 81, 30910, 87416, -143124, -1293, 0, 57, 1000, 15, 45, 40, 200, 109483, -141223, -2983, 'Blue', 90058, -140574, -911, 16711680, 'Red', 109213, -141148, -2800, 6684927, 109469, -141216, -2034, 23130005, 23130004, 23130003, 23130002, 23130006, 23130001),
('Race Track Fortress Siege', 'Siege the Race Track  Fortress', 'Town of Dion', 71, 81, 30195, 16218, 144166, -2981, 0, 57, 1000, 15, 45, 40, 200, 16582, 188047, -2924, 'Blue', 23365, 179858, -1839, 16711680, 'Red', 16757, 188340, -2767, 6684927, 16587, 188072, -2002, 20230005, 20230004, 20230009, 20230008, 20230003, 20230003),
('Swamp of Screams Fortress Siege', 'Siege the Screaming Fortress', 'Rune Township', 71, 81, 30900, 43957, -47711, -823, 0, 57, 1000, 15, 45, 40, 200, 69833, -61430, -2786, 'Blue', 76854, -50916, -1971, 16711680, 'Red', 69964, -61124, -2630, 6684927, 69834, -61434, -1863, 22160005, 22160004, 22160009, 22160008, 22160001, 22160012),
('Valley of Saints Fortress Siege', 'Siege the Saint Fortress', 'Rune Township', 71, 81, 31276, 43561, -48758, -798, 0, 57, 1000, 15, 45, 40, 200, 72160, -94767, -1428, 'Blue', 69088, -85022, -1372, 16711680, 'Red', 72301, -94461, -1272, 6684927, 72169, -94737, -505, 22150006, 22150007, 22150002, 22150003, 22150008, 22150001),
('Bandit Stronghold', 'Siege the Bandit Stronghold', 'Town of Oren', 71, 81, 30990, 83137, 53328, -1440, 0, 57, 1000, 15, 40, 10, 200, 80428, -15418, -704, 'Blue', 87500, -23341, -995, 16711680, 'Red', 80428, -15418, -704, 6684927, 80127, -15404, -1805, 22170004, 22170003, 22170004, 22170003, 22170001, 22170002),
('Fortress of the Dead', 'Siege the Fortress of the Dead', 'Rune Township', 71, 81, 31538, 44104, -48539, -797, 0, 57, 1000, 15, 40, 10, 200, 58149, -27479, 578, 'Blue', 59555, -37667, 403, 16711680, 'Red', 58153, -27485, 578, 6684927, 57953, -25830, 592, 21170004, 21170003, 21170004, 21170003, 21170002, 21170006);