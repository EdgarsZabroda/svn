#
# Created by DraX on 2005.08.08 modified by Ariakas on 2005.09.19
#

import sys

from com.l2jfree.gameserver.model.quest        import State
from com.l2jfree.gameserver.model.quest        import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
qn = "30565_kakai_occupation_change"
KAKAI_LORD_OF_FLAME = 30565

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st):

   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"

   Race     = st.getPlayer().getRace()
   ClassId  = st.getPlayer().getClassId()
   Level    = st.getPlayer().getLevel()

   if event == "30565-01.htm":
     return "30565-01.htm"

   if event == "30565-02.htm":
     return "30565-02.htm"

   if event == "30565-03.htm":
     return "30565-03.htm"

   if event == "30565-04.htm":
     return "30565-04.htm"

   if event == "30565-05.htm":
     return "30565-05.htm"

   if event == "30565-06.htm":
     return "30565-06.htm"

   if event == "30565-07.htm":
     return "30565-07.htm"

   if event == "30565-08.htm":
     return "30565-08.htm"

   st.exitQuest(False)
   st.exitQuest(1)
   return htmltext

 def onTalk (Self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()

   Race    = st.getPlayer().getRace()
   ClassId = st.getPlayer().getClassId()

   # Orcs got accepted
   if npcId == KAKAI_LORD_OF_FLAME and Race in [Race.Orc]:
     if ClassId in [ClassId.orcFighter]:
       htmltext = "30565-01.htm"
       st.setState(State.STARTED)
       return htmltext
     if ClassId in [ClassId.orcRaider, ClassId.orcMonk, ClassId.orcShaman]:
       htmltext = "30565-09.htm"
       st.exitQuest(False)
       st.exitQuest(1)
       return htmltext
     if ClassId in [ClassId.destroyer, ClassId.tyrant, ClassId.overlord, ClassId.warcryer]:
       htmltext = "30565-10.htm"
       st.exitQuest(False)
       st.exitQuest(1)
       return htmltext
     if ClassId in [ClassId.orcMage]:
       htmltext = "30565-06.htm"
       st.setState(State.STARTED)
       return htmltext

   # All other Races must be out
   if npcId == KAKAI_LORD_OF_FLAME and Race in [Race.Dwarf, Race.Darkelf, Race.Elf, Race.Human, Race.Kamael]:
     st.exitQuest(False)
     st.exitQuest(1)
     return "30565-11.htm"

QUEST   = Quest(30565,qn,"village_master")

QUEST.addStartNpc(30565)

QUEST.addTalkId(30565)