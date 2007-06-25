# L2J_JP EDIT SANDMAN
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from net.sf.l2j.gameserver.instancemanager import AntharasManager

PORTAL_STONE    = 3865
HEART           = 13001
ANTHARAS_OLD    = 29019
ANTHARAS_WEAK   = 29066
ANTHARAS_NORMAL = 29067
ANTHARAS_STRONG = 29068

# Boss: Antharas
class antharas(JQuest):

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
    st = player.getQuestState("antharas")
    if not st : return "<html><head><body>I have no tasks for you</body></html>"
    npcId = npc.getNpcId()
    if npcId == HEART:
      if AntharasManager.getInstance().isEnableEnterToLair():
        if st.getQuestItemsCount(PORTAL_STONE) >= 1:
          st.takeItems(PORTAL_STONE,1)
          AntharasManager.getInstance().setAntharasSpawnTask()
          AntharasManager.getInstance().addPlayerToLair(st.player)
          st.player.teleToLocation(173826,115333,-7708)
          return
        else:
          st.exitQuest(1)
          return '<html><body>Heart of Muscai:<br><br>You do not have the proper stones needed for teleport.<br>It is for the teleport where does 1 stone to you need.<br></body></html>'
      else:
        st.exitQuest(1)
        return '<html><body>Heart of Muscai:<br><br>Antharas has already awoke!<br>You are not possible to enter into Lair of Antharas.<br></body></html>'

  def onKill (self,npc,player):
    st = player.getQuestState("antharas")
    if not st: return
    AntharasManager.getInstance().setCubeSpawn()
    st.exitQuest(1)

# Quest class and state definition
QUEST       = antharas(-1, "antharas", "ai")
CREATED     = State('Start', QUEST)

# Quest initialization
QUEST.setInitialState(CREATED)
# Quest NPC starter initialization
QUEST.addStartNpc(HEART)
QUEST.addTalkId(HEART)
QUEST.addKillId(ANTHARAS_OLD)
QUEST.addKillId(ANTHARAS_WEAK)
QUEST.addKillId(ANTHARAS_NORMAL)
QUEST.addKillId(ANTHARAS_STRONG)

print "AI: individuals: Antharas...loaded!"
