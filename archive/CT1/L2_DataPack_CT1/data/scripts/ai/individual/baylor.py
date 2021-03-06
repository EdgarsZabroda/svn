# By Umbrella HanWik
import sys
from net.sf.l2j.gameserver.instancemanager.grandbosses import BaylorManager
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from net.sf.l2j.gameserver.network.serverpackets import SocialAction

#ENTRY_SATAT 0 = Baylor is not spawned
#ENTRY_SATAT 1 = Baylor is already dead
#ENTRY_SATAT 2 = Baylor is already entered by a other party
#ENTRY_SATAT 3 = Baylor is in interval
#ENTRY_SATAT 4 = You have no Party

#NPC
STATUE          =   32109
CRYSTALINE   =   29100
BAYLOR         =   29099

# Boss: baylor
class baylor (JQuest):

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
    st = player.getQuestState("baylor")
    if not st : return "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
    npcId = npc.getNpcId()
    if npcId == STATUE :
        ENTRY_SATAT = BaylorManager.getInstance().canIntoBaylorLair(player)
        if ENTRY_SATAT == 1 or ENTRY_SATAT == 2 :
          st.exitQuest(1)
          return "<html><body>Shilen's Stone Statue:<br>Another adventurers have already fought against the baylor. Do not obstruct them.</body></html>"
        elif ENTRY_SATAT == 3 :
          st.exitQuest(1)
          return "<html><body>Shilen's Stone Statue:<br>The baylor is very powerful now. It is not possible to enter the inside.</body></html>"
        elif ENTRY_SATAT == 4 :
          st.exitQuest(1)
          return "<html><body>Shilen's Stone Statue:<br>You seal the baylor alone? You should not do so! Bring the companion.</body></html>"
        elif ENTRY_SATAT == 0 :
          BaylorManager.getInstance().setBaylorSpawnTask(CRYSTALINE)
          BaylorManager.getInstance().setBaylorSpawnTask(BAYLOR)
          BaylorManager.getInstance().entryToBaylorLair(player)
          return "<html><body>Shilen's Stone Statue:<br>Please seal the baylor by your ability.</body></html>"

  def onKill (self,npc,player,isPet):
    st = player.getQuestState("baylor")
    if not st: return
    npcId = npc.getNpcId()
    if npcId == BAYLOR :
      BaylorManager.getInstance().setCubeSpawn()
      st.exitQuest(1)
    return

# Quest class and state definition
QUEST = baylor(-1, "baylor", "ai")

# Quest NPC starter initialization
QUEST.addStartNpc(STATUE)
QUEST.addTalkId(STATUE)
QUEST.addKillId(CRYSTALINE)
QUEST.addKillId(BAYLOR)