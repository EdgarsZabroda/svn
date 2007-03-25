# Made by disKret
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

#NPC
VIRGIL = 31742
KASSANDRA = 31743
OGMAR = 31744
FALLEN_UNICORN = 31746
PURE_UNICORN = 31747
CORNERSTONE = 31748
MYSTERIOUS_KNIGHT = 31751
ANGEL_CORPSE = 31752
KALIS = 30759
MATILD = 30738

#QUEST ITEM
VIRGILS_LETTER = 7677
GOLDEN_HAIR = 7590
ORB_oF_BINDING = 7595
SORCERY_INGREDIENT = 7596
CARADINE_LETTER = 7678

#CHANCE FOR HAIR DROP
CHANCE_FOR_HAIR = 20

#MOB
RESTRAINER_OF_GLORY = 27317

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
   htmltext = event
   cond = st.getInt("cond")
   if event == "31742-3.htm" :
     if cond == 0 :
       st.setState(STARTED)
       st.takeItems(VIRGILS_LETTER,1)
       st.set("cond","1")
       st.playSound("ItemSound.quest_accept")
   if event == "31743-2.htm" :
     return htmltext
   if event == "31743-3.htm" :
     return htmltext
   if event == "31743-4.htm" :
     return htmltext
   if event == "31743-5.htm" :
     if cond == 1 :
       st.set("cond","2")
       st.setState(STARTED)
       st.playSound("ItemSound.quest_accept")
   if event == "31744-2.htm" :
     if cond == 2 :
       st.set("cond","3")
       st.playSound("ItemSound.quest_middle")
   if event == "31751-2.htm" :
     if cond == 3 :
       st.set("cond","4")
       st.playSound("ItemSound.quest_middle")
   if event == "30759-2.htm" :
     if cond == 6 :
       st.set("cond","7")
       st.playSound("ItemSound.quest_middle")
   if event == "30738-2.htm" :
     if cond == 7 :
       st.set("cond","8")
       st.giveItems(SORCERY_INGREDIENT,1)
       st.playSound("ItemSound.quest_middle")
   if event == "30759-5.htm" :
     if cond == 8 :
       st.set("cond","9")
       st.takeItems(GOLDEN_HAIR,1)
       st.takeItems(SORCERY_INGREDIENT,1)
       st.playSound("ItemSound.quest_middle")
   return htmltext

 def onTalk (Self,npc,st):
   htmltext = "<html><head><body>I have nothing to say you</body></html>"
   chance = st.getRandom(100)
   cornerstones = st.getInt("cornerstones")
   npcId = npc.getNpcId()
   id = st.getState()
   if id == CREATED :
     st.set("cond","0")
     st.set("cornerstones","0")
   cond = int(st.get("cond"))
   if st.getPlayer().isSubClassActive() :
     if npcId == VIRGIL and cond == 0 and st.getQuestItemsCount(VIRGILS_LETTER) == 1 :
       if id == COMPLETED :
         htmltext = "<html><head><body>This quest have already been completed.</body></html>"
       elif st.getPlayer().getLevel() < 60 : 
         htmltext = "31742-2.htm"
         st.exitQuest(1)
       elif st.getPlayer().getLevel() >= 60 :
         htmltext = "31742-1.htm"
     if npcId == VIRGIL and cond == 1 :
       htmltext = "31742-4.htm"
     if npcId == KASSANDRA and cond == 1 :
       htmltext = "31743-1.htm"
     if npcId == KASSANDRA and cond == 2 :
       htmltext = "31743-6.htm"
     if npcId == OGMAR and cond == 2 :
       htmltext = "31744-1.htm"
     if npcId == OGMAR and cond == 3 :
       htmltext = "31744-3.htm"
     if npcId == MYSTERIOUS_KNIGHT and cond == 3 :
       htmltext = "31751-1.htm"
     if npcId == MYSTERIOUS_KNIGHT and cond == 4 :
       htmltext = "31751-3.htm"
     if npcId == ANGEL_CORPSE and cond == 4 :
       npc.reduceCurrentHp(10000,npc) 
       if CHANCE_FOR_HAIR < chance :
         htmltext = "31752-2.htm"
       else :
         st.set("cond","5")
         st.giveItems(GOLDEN_HAIR,1)
         st.playSound("ItemSound.quest_middle")
         htmltext = "31752-1.htm"
     if npcId == ANGEL_CORPSE and cond == 5 :
       htmltext = "31752-2.htm"
     if npcId == MYSTERIOUS_KNIGHT and cond == 5 and st.getQuestItemsCount(GOLDEN_HAIR) == 1 :
       htmltext = "31751-4.htm"
       st.set("cond","6")
       st.playSound("ItemSound.quest_middle")
     if npcId == MYSTERIOUS_KNIGHT and cond == 6 :
       htmltext = "31751-5.htm"
     if npcId == KALIS and cond == 6 :
       htmltext = "30759-1.htm"
     if npcId == KALIS and cond == 7 :
       htmltext = "30759-3.htm"
     if npcId == MATILD and cond == 7 :
       htmltext = "30738-1.htm"
     if npcId == MATILD and cond == 8 :
       htmltext = "30738-3.htm"
     if npcId == KALIS and cond == 8 and st.getQuestItemsCount(SORCERY_INGREDIENT) == 1 :
       htmltext = "30759-4.htm"
     if npcId == KALIS and cond == 9 :
       htmltext = "30759-6.htm"
     if npcId == FALLEN_UNICORN and cond == 9 :
       htmltext = "31746-1.htm"
     if npcId == CORNERSTONE and cond == 9 and st.getQuestItemsCount(ORB_oF_BINDING) == 0 :
       htmltext = "31748-1.htm"
     if npcId == CORNERSTONE and cond == 9 and st.getQuestItemsCount(ORB_oF_BINDING) >= 1 :
       htmltext = "31748-2.htm"
       st.takeItems(ORB_oF_BINDING,1)
       npc.reduceCurrentHp(10000,npc)
       st.set("cornerstones",str(cornerstones+1))
       st.playSound("ItemSound.quest_middle")
       if cornerstones == 3 :
         st.set("cond","10")
         st.playSound("ItemSound.quest_middle")
     if npcId == FALLEN_UNICORN and cond == 10 :
       htmltext = "31746-2.htm"
       npc.reduceCurrentHp(10000,npc)
       st.getPcSpawn().addSpawn(PURE_UNICORN)
     if npcId == PURE_UNICORN and cond == 10 :
       st.set("cond","11")
       st.playSound("ItemSound.quest_middle")
       htmltext = "31747-1.htm"
     if npcId == PURE_UNICORN and cond == 11 :
       htmltext = "31747-2.htm"
     if npcId == KASSANDRA and cond == 11 :
       htmltext = "31743-7.htm"
     if npcId == VIRGIL and cond == 11 :
       htmltext = "31742-6.htm"
       st.set("cond","0")
       st.set("cornerstones","0")
       st.giveItems(CARADINE_LETTER,1)
       st.playSound("ItemSound.quest_finish")
       st.setState(COMPLETED)
   return htmltext

 def onKill (self,npc,st):
    if int(st.get("cond")) == 9 and st.getQuestItemsCount(ORB_oF_BINDING) <= 4 :
      st.giveItems(ORB_oF_BINDING,1)
      st.playSound("ItemSound.quest_itemget")
    return 

QUEST       = Quest(242,"242_PossessorOfAPreciousSoul_2","Possessor Of A Precious Soul - 2")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST,True)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(VIRGIL)
CREATED.addTalkId(VIRGIL)
STARTED.addTalkId(VIRGIL)
STARTED.addTalkId(KASSANDRA)
STARTED.addTalkId(OGMAR)
STARTED.addTalkId(MYSTERIOUS_KNIGHT)
STARTED.addTalkId(ANGEL_CORPSE)
STARTED.addTalkId(KALIS)
STARTED.addTalkId(MATILD)
STARTED.addTalkId(FALLEN_UNICORN)
STARTED.addTalkId(CORNERSTONE)
STARTED.addTalkId(PURE_UNICORN)

STARTED.addKillId(RESTRAINER_OF_GLORY)

STARTED.addQuestDrop(RESTRAINER_OF_GLORY,GOLDEN_HAIR,1)
STARTED.addQuestDrop(RESTRAINER_OF_GLORY,ORB_oF_BINDING,1)
STARTED.addQuestDrop(RESTRAINER_OF_GLORY,SORCERY_INGREDIENT,1)
STARTED.addQuestDrop(RESTRAINER_OF_GLORY,CARADINE_LETTER,1)

print "importing quests: 242: Possessor Of A Precious Soul - 2"
