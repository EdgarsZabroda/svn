# L2J_JP EDIT SANDMAN
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from net.sf.l2j.gameserver.instancemanager.grandbosses import AntharasManager

PORTAL_STONE    = 3865
HEART           = 13001
ANTHARAS_OLD    = 29019
ANTHARAS_WEAK   = 29066
ANTHARAS_NORMAL = 29067
ANTHARAS_STRONG = 29068

# Boss: Antharas
class antharas(JQuest):
  def __init__(self,id,name,descr):
    self.antharas = 29019
    JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
    st = player.getQuestState("antharas")
    if not st : return "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
    npcId = npc.getNpcId()
    if npcId == HEART:
      if player.isFlying() :
        return '<html><body>Heart of Muscai:<br>You may not enter while flying a wyvern</body></html>'
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
        return '<html><body>Heart of Muscai:<br><br>Antharas has already awoke!<br>You are not allowed to enter into Lair of Antharas.<br></body></html>'

  def onKill (self,npc,player,isPet):
    st = player.getQuestState("antharas")
    #give the antharas slayer circlet to ALL PARTY MEMBERS who help kill anthy,
    party = player.getParty()
    if party :
       for partyMember in party.getPartyMembers().toArray() :
           pst = partyMember.getQuestState("antharas")
           if pst :
               if pst.getQuestItemsCount(8568) < 1 :
                   pst.giveItems(8568,1)
                   pst.exitQuest(1)
    else :
       pst = player.getQuestState("antharas")
       if pst :
           if pst.getQuestItemsCount(8568) < 1 :
               pst.giveItems(8568,1)
               pst.exitQuest(1)
    if not st: return
    AntharasManager.getInstance().setCubeSpawn()
    st.exitQuest(1)
    return
# Quest class and state definition
QUEST = antharas(-1, "antharas", "ai")
# Quest NPC starter initialization
QUEST.addStartNpc(HEART)
QUEST.addTalkId(HEART)
QUEST.addKillId(ANTHARAS_OLD)
QUEST.addKillId(ANTHARAS_WEAK)
QUEST.addKillId(ANTHARAS_NORMAL)
QUEST.addKillId(ANTHARAS_STRONG)
