# By Evil33t
import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.tools.random import Rnd
from com.l2jfree.gameserver.datatables import ItemTable

KISS_OF_EVA = [1073,3141,3252]
BOX = 32342
REWARDS = [9692,9693]

def dropItem(npc,itemId,count):
	ditem = ItemTable.getInstance().createItem("Loot", itemId, count, None)
	ditem.dropMe(npc, npc.getX(), npc.getY(), npc.getZ()); 

class evabox(JQuest):
	def __init__(self,id,name,descr):
		self.isSpawned = False
		JQuest.__init__(self,id,name,descr)

	def onKill (self,npc,player,isPet):
		found = False
		for effect in player.getAllEffects():
			if effect.getSkill().getId() in KISS_OF_EVA:
				found = True
		if found:
			dropid = Rnd.get(len(REWARDS))
			dropItem(npc,REWARDS[dropid],1)
		return

QUEST = evabox(-1, "evabox", "ai")
QUEST.addKillId(BOX)
