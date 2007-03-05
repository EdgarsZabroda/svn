/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.ai;

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.concurrent.Future;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;

/**
 * This class manages AI of L2Attackable.<BR><BR>
 * 
 */
public class L2SiegeGuardAI extends L2CharacterAI implements Runnable
{
    private static final int MAX_ATTACK_TIMEOUT = 300; // int ticks, i.e. 30 seconds 

    /** The L2Attackable AI task executed every 1s (call onEvtThink method)*/
    private Future aiTask;

    /** The delay after wich the attacked is stopped */
    private int _attack_timeout;

    /** The L2Attackable aggro counter */
    private int _globalAggro;

    /** The flag used to indicate that a thinking action is in progress */
    private boolean thinking; // to prevent recursive thinking

    private int attackRange;

    /**
     * Constructor of L2AttackableAI.<BR><BR>
     * 
     * @param accessor The AI accessor of the L2Character
     * 
     */
    public L2SiegeGuardAI(L2Character.AIAccessor accessor)
    {
        super(accessor);

        _attack_timeout = Integer.MAX_VALUE;
        _globalAggro = -10; // 10 seconds timeout of ATTACK after respawn

        attackRange = ((L2Attackable) _actor).getPhysicalAttackRange();
    }

    public void run()
    {
        // Launch actions corresponding to the Event Think
        onEvtThink();

    }

    /**
     * Return True if the target is autoattackable (depends on the actor type).<BR><BR>
     * 
     * <B><U> Actor is a L2GuardInstance</U> :</B><BR><BR>
     * <li>The target isn't a Folk or a Door</li>
     * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
     * <li>The target is in the actor Aggro range and is at the same height</li>
     * <li>The L2PcInstance target has karma (=PK)</li>
     * <li>The L2MonsterInstance target is aggressive</li><BR><BR>
     * 
     * <B><U> Actor is a L2SiegeGuardInstance</U> :</B><BR><BR>
     * <li>The target isn't a Folk or a Door</li>
     * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
     * <li>The target is in the actor Aggro range and is at the same height</li>
     * <li>A siege is in progress</li>
     * <li>The L2PcInstance target isn't a Defender</li><BR><BR>
     * 
     * <B><U> Actor is a L2FriendlyMobInstance</U> :</B><BR><BR>
     * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
     * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
     * <li>The target is in the actor Aggro range and is at the same height</li>
     * <li>The L2PcInstance target has karma (=PK)</li><BR><BR>
     * 
     * <B><U> Actor is a L2MonsterInstance</U> :</B><BR><BR>
     * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
     * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
     * <li>The target is in the actor Aggro range and is at the same height</li>
     * <li>The actor is Aggressive</li><BR><BR>
     * 
     * @param target The targeted L2Object
     * 
     */
    private boolean autoAttackCondition(L2Character target)
    {
        // Check if the target isn't a Folk or a Door
        if (target instanceof L2FolkInstance || target instanceof L2DoorInstance) return false;

        // Check if the target isn't dead
        if (target.isAlikeDead()) return false;

        // Get the owner if the target is a summon
        if (target instanceof L2SummonInstance) target = ((L2SummonInstance) target).getOwner();

        // Check if the target isn't invulnerable
        if (target.isInvul()) return false;

        // Check if the target is a L2PcInstance
        if (target instanceof L2PcInstance)
        {
            // Check if the target isn't in silent move mode AND too far (>100)
            if (((L2PcInstance) target).isSilentMoving()
                && !_actor.isInsideRadius(target, 100, false, false)) return false;
        }
               // Los Check Here
        return (_actor.isAutoAttackable(target) && GeoData.getInstance().canSeeTarget(_actor, target));
        
    }

    /**
     * Set the Intention of this L2CharacterAI and create an  AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR><BR>
     * 
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT><BR><BR>
     * 
     * @param intention The new Intention to set to the AI
     * @param arg0 The first parameter of the Intention
     * @param arg1 The second parameter of the Intention
     * 
     */
    synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
    {
        if (_log.isDebugEnabled())
            _log.info("L2SiegeAI.changeIntention(" + intention + ", " + arg0 + ", " + arg1 + ")");

        if (intention == AI_INTENTION_IDLE || intention == AI_INTENTION_ACTIVE)
        {
            // Check if actor is not dead
            if (!_actor.isAlikeDead())
            {
                L2Attackable npc = (L2Attackable) _actor;

                // If its _knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
                if (npc.getKnownList().getKnownPlayers().size() > 0) intention = AI_INTENTION_ACTIVE;
                else intention = AI_INTENTION_IDLE;
            }

            if (intention == AI_INTENTION_IDLE)
            {
                // Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
                super.changeIntention(AI_INTENTION_IDLE, null, null);

                // Stop AI task and detach AI from NPC
                if (aiTask != null)
                {
                    aiTask.cancel(true);
                    aiTask = null;
                }

                // Cancel the AI
                _accessor.detachAI();

                return;
            }
        }

        // Set the Intention of this L2AttackableAI to intention
        super.changeIntention(intention, arg0, arg1);

        // If not idle - create an AI task (schedule onEvtThink repeatedly)
        if (aiTask == null)
        {
            aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
        }
    }

    /**
     * Manage the Attack Intention : Stop current Attack (if necessary), Calculate attack timeout, Start a new Attack and Launch Think Event.<BR><BR>
     *
     * @param target The L2Character to attack
     *
     */
    protected void onIntentionAttack(L2Character target)
    {
        // Calculate the attack timeout
        _attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

        // Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
        //if (_actor.getTarget() != null)
        super.onIntentionAttack(target);
    }

    /**
     * Manage AI standard thinks of a L2Attackable (called by onEvtThink).<BR><BR>
     * 
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Update every 1s the _globalAggro counter to come close to 0</li>
     * <li>If the actor is Aggressive and can attack, add all autoAttackable L2Character in its Aggro Range to its _aggroList, chose a target and order to attack it</li>
     * <li>If the actor  can't attack, order to it to return to its home location</li>
     * 
     */
    private void thinkActive()
    {
        L2Attackable npc = (L2Attackable) _actor;

        // Update every 1s the _globalAggro counter to come close to 0
        if (_globalAggro != 0)
        {
            if (_globalAggro < 0) _globalAggro++;
            else _globalAggro--;
        }

        // Add all autoAttackable L2Character in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
        // A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
        if (_globalAggro >= 0)
        {
            for (L2Character target : npc.getKnownList().getKnownCharactersInRadius(attackRange))
            {
                if (target == null) continue;
                if (autoAttackCondition(target)) // check aggression
                {
                    // Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
                    int hating = npc.getHating(target);

                    // Add the attacker to the L2Attackable _aggroList with 0 damage and 1 hate
                    if (hating == 0)
                    {
                        npc.addDamageHate(target, 0, 1);
                        npc.addBufferHate();
                    }
                }
            }

            // Chose a target from its aggroList
            L2Character hated;
            if (_actor.isConfused()) hated = _attack_target; // Force mobs to attak anybody if confused
            else hated = npc.getMostHated();

            // Order to the L2Attackable to attack the target
            if (hated != null)
            {
                // Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
                int aggro = npc.getHating(hated);

                if (aggro + _globalAggro > 0)
                {
                    // Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
                    if (!_actor.isRunning()) _actor.setRunning();

                    // Set the AI Intention to AI_INTENTION_ATTACK
                    setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null);
                }

                return;
            }

        }
        // Order to the L2SiegeGuardInstance to return to its home location because there's no target to attack
        ((L2SiegeGuardInstance) _actor).returnHome();
        return;

    }

    private void AttackPrepare()
    {
        // Get all information needed to chose between physical or magical attack
        L2Skill[] skills = null;
        double dist_2 = 0;
        int range = 0;

        try
        {
            _actor.setTarget(_attack_target);
            skills = _actor.getAllSkills();
            dist_2 = _actor.getPlanDistanceSq(_attack_target.getX(), _attack_target.getY());
            range = _actor.getPhysicalAttackRange();
        }
        catch (NullPointerException e)
        {
            //_log.warning("AttackableAI: Attack target is NULL.");
            _actor.setTarget(null);
            setIntention(AI_INTENTION_IDLE, null, null);
            return;
        }

        // Check if the actor isn't muted and if it is far from target
        if (!_actor.isMuted() && dist_2 > (range + 20) * (range + 20))
        {
            // check for long ranged skills and heal/buff skills
            if (!Config.ALT_GAME_MOB_ATTACK_AI
                || (_actor instanceof L2MonsterInstance && Rnd.nextInt(100) <= 5))
                for (L2Skill sk : skills)
                {
                    int castRange = sk.getCastRange();
                    
                    if (((sk.getSkillType() == L2Skill.SkillType.BUFF || sk.getSkillType() == L2Skill.SkillType.HEAL) || (dist_2 >= castRange * castRange / 9)
                        && (dist_2 <= castRange * castRange) && (castRange > 70))
                        && !_actor.isSkillDisabled(sk.getId())
                        && _actor.getCurrentMp() >= _actor.getStat().getMpConsume(sk) && !sk.isPassive())
                    {
                        L2Object OldTarget = _actor.getTarget();
                        if (sk.getSkillType() == L2Skill.SkillType.BUFF
                            || sk.getSkillType() == L2Skill.SkillType.HEAL)
                        {
                            boolean useSkillSelf = true;
                            if (sk.getSkillType() == L2Skill.SkillType.HEAL
                                && _actor.getCurrentHp() > (int) (_actor.getMaxHp() / 1.5))
                            {
                                useSkillSelf = false;
                                break;
                            }
                            if (sk.getSkillType() == L2Skill.SkillType.BUFF)
                            {
                                L2Effect[] effects = _actor.getAllEffects();
                                for (int i = 0; effects != null && i < effects.length; i++)
                                {
                                    L2Effect effect = effects[i];
                                    if (effect.getSkill() == sk)
                                    {
                                        useSkillSelf = false;
                                        break;
                                    }
                                }
                            }
                            if (useSkillSelf) _actor.setTarget(_actor);
                        }

                        clientStopMoving(null);
                        _accessor.doCast(sk);
                        _actor.setTarget(OldTarget);
                        return;
                    }
                }

            // Check if the L2SiegeGuardInstance is attacking, knows the target and can't run
            if (!(_actor.isAttackingNow()) && (_actor.getRunSpeed() == 0)
                && (_actor.getKnownList().knowsObject(_attack_target)))
            {
                // Cancel the target
                _actor.getKnownList().removeKnownObject(_attack_target);
                _actor.setTarget(null);
            }
            else
            {
                L2SiegeGuardInstance sGuard = (L2SiegeGuardInstance) _actor;
                double dx = _actor.getX() - _attack_target.getX();
                double dy = _actor.getY() - _attack_target.getY();
                double homeX = _attack_target.getX() - sGuard.getHomeX();
                double homeY = _attack_target.getY() - sGuard.getHomeY();

                // Check if the L2SiegeGuardInstance isn't too far from it's home location
                if ((dx * dx + dy * dy > 10000) && (homeX * homeX + homeY * homeY > 3240000) // 1800 * 1800
                    && (_actor.getKnownList().knowsObject(_attack_target)))
                {
                    // Cancel the target
                    _actor.getKnownList().removeKnownObject(_attack_target);
                    _actor.setTarget(null);
                    setIntention(AI_INTENTION_IDLE, null, null);
                }
                else
                {
                    // Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
                    moveToPawn(_attack_target, range);
                }
            }

            return;

        }
        // Else, if the actor is muted and far from target, just "move to pawn"
        else if (_actor.isMuted() && dist_2 > (range + 20) * (range + 20))
        {
            moveToPawn(_attack_target, range);
            return;
        }
        // Else, if this is close enough to attack
        else if (dist_2 <= (range + 20) * (range + 20))
        {
            // Force mobs to attak anybody if confused
            L2Character hated = null;
            if (_actor.isConfused()) hated = _attack_target;
            else hated = ((L2Attackable) _actor).getMostHated();

            if (hated == null)
            {
                setIntention(AI_INTENTION_ACTIVE, null, null);
                return;
            }
            if (hated != _attack_target) _attack_target = hated;

            _attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

            // check for close combat skills && heal/buff skills
            if (!_actor.isMuted() && Rnd.nextInt(100) <= 5)
            {
                for (L2Skill sk : skills)
                {
                    int castRange = sk.getCastRange();
                    
                    if (castRange * castRange >= dist_2 && castRange <= 70 && !sk.isPassive()
                        && _actor.getCurrentMp() >= _actor.getStat().getMpConsume(sk)
                        && !_actor.isSkillDisabled(sk.getId()))
                    {
                        L2Object OldTarget = _actor.getTarget();
                        if (sk.getSkillType() == L2Skill.SkillType.BUFF
                            || sk.getSkillType() == L2Skill.SkillType.HEAL)
                        {
                            boolean useSkillSelf = true;
                            if (sk.getSkillType() == L2Skill.SkillType.HEAL
                                && _actor.getCurrentHp() > (int) (_actor.getMaxHp() / 1.5))
                            {
                                useSkillSelf = false;
                                break;
                            }
                            if (sk.getSkillType() == L2Skill.SkillType.BUFF)
                            {
                                L2Effect[] effects = _actor.getAllEffects();
                                for (int i = 0; effects != null && i < effects.length; i++)
                                {
                                    L2Effect effect = effects[i];
                                    if (effect.getSkill() == sk)
                                    {
                                        useSkillSelf = false;
                                        break;
                                    }
                                }
                            }
                            if (useSkillSelf) _actor.setTarget(_actor);
                        }

                        clientStopMoving(null);
                        _accessor.doCast(sk);
                        _actor.setTarget(OldTarget);
                        return;
                    }
                }
            }
            // Finally, do the physical attack itself
            _accessor.doAttack(_attack_target);
        }
    }

    /**
     * Manage AI attack thinks of a L2Attackable (called by onEvtThink).<BR><BR>
     * 
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Update the attack timeout if actor is running</li>
     * <li>If target is dead or timeout is expired, stop this attack and set the Intention to AI_INTENTION_ACTIVE</li>
     * <li>Call all L2Object of its Faction inside the Faction Range</li>
     * <li>Chose a target and order to attack it with magic skill or physical attack</li><BR><BR>
     * 
     * TODO: Manage casting rules to healer mobs (like Ant Nurses)
     * 
     */
    private void thinkAttack()
    {
        if (_log.isDebugEnabled())
            _log.info("L2SiegeGuardAI.thinkAttack(); timeout="
                + (_attack_timeout - GameTimeController.getGameTicks()));

        if (_attack_timeout < GameTimeController.getGameTicks())
        {
            // Check if the actor is running
            if (_actor.isRunning())
            {
                // Set the actor movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance
                _actor.setWalking();

                // Calculate a new attack timeout
                _attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
            }
        }

        // Check if target is dead or if timeout is expired to stop this attack
        if (_attack_target == null || _attack_target.isAlikeDead()
            || _attack_timeout < GameTimeController.getGameTicks())
        {
            // Stop hating this target after the attack timeout or if target is dead
            if (_attack_target != null)
            {
                L2Attackable npc = (L2Attackable) _actor;
                int hate = npc.getHating(_attack_target);
                if (hate > 0)
                {
                    npc.addDamageHate(_attack_target, 0, -hate);
                    npc.addBufferHate();
                }
            }

            // Cancel target and timeout
            _attack_timeout = Integer.MAX_VALUE;
            _attack_target = null;

            // Set the AI Intention to AI_INTENTION_ACTIVE
            setIntention(AI_INTENTION_ACTIVE, null, null);

            _actor.setWalking();
            return;
        }

        AttackPrepare();
        FactionNotify();
    }

    private final void FactionNotify()
    {
        // Call all L2Object of its Faction inside the Faction Range
        if (((L2NpcInstance) _actor).getFactionId() == null || _attack_target == null || _actor == null)
            return;

        String faction_id = ((L2NpcInstance) _actor).getFactionId();

        // Go through all L2Object that belong to its faction
        for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(1200))
        {
            if (cha == null) continue;

            if (!(cha instanceof L2NpcInstance)) continue;

            L2NpcInstance npc = (L2NpcInstance) cha;

            if (faction_id != npc.getFactionId()) continue;

            // Check if the L2Object is inside the Faction Range of the actor
            if ((npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE || npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
                && _actor.isInsideRadius(npc, npc.getFactionRange(), false, true) 
                && npc.getTarget() == null
                && _attack_target.isInsideRadius(npc, npc.getFactionRange(), false, true) 
                && Math.abs(_attack_target.getZ() - npc.getZ()) < 600)
            {
                // Notify the L2Object AI with EVT_AGGRESSION
                npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _attack_target, 1);
            }
        }
    }

    /**
     * Manage AI thinking actions of a L2Attackable.<BR><BR>
     */
    protected void onEvtThink()
    {
        //      if(getIntention() != AI_INTENTION_IDLE && (!_actor.isVisible() || !_actor.hasAI() || !_actor.isKnownPlayers()))
        //          setIntention(AI_INTENTION_IDLE);

        // Check if the actor can't use skills and if a thinking action isn't already in progress
        if (thinking || _actor.isAllSkillsDisabled()) return;

        // Start thinking action
        thinking = true;

        try
        {
            // Manage AI thinks of a L2Attackable
            if (getIntention() == AI_INTENTION_ACTIVE) thinkActive();
            else if (getIntention() == AI_INTENTION_ATTACK) thinkAttack();
        }
        finally
        {
            // Stop thinking action
            thinking = false;
        }
    }

    /**
     * Launch actions corresponding to the Event Attacked.<BR><BR>
     * 
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li>
     * <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance</li>
     * <li>Set the Intention to AI_INTENTION_ATTACK</li><BR><BR>
     * 
     * @param attacker The L2Character that attacks the actor
     * 
     */
    protected void onEvtAttacked(L2Character attacker)
    {
        // Calculate the attack timeout
        _attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

        // Set the _globalAggro to 0 to permit attack even just after spawn
        if (_globalAggro < 0) _globalAggro = 0;

        // Add the attacker to the _aggroList of the actor
        ((L2Attackable) _actor).addDamageHate(attacker, 0, 1);
        ((L2Attackable) _actor).addBufferHate();

        // Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
        if (!_actor.isRunning()) _actor.setRunning();

        // Set the Intention to AI_INTENTION_ATTACK
        if (getIntention() != AI_INTENTION_ATTACK)
        {
            setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
        }

        super.onEvtAttacked(attacker);
    }

    /**
     * Launch actions corresponding to the Event Aggression.<BR><BR>
     * 
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Add the target to the actor _aggroList or update hate if already present </li>
     * <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li><BR><BR>
     * 
     * @param attacker The L2Character that attacks
     * @param aggro The value of hate to add to the actor against the target
     * 
     */
    protected void onEvtAggression(L2Character target, int aggro)
    {
        if (target != null && _actor != null) 
        {
            L2Attackable me = (L2Attackable) _actor;

            // Add the target to the actor _aggroList or update hate if already present
            me.addDamageHate(target, 0, aggro);
            me.addBufferHate();

            // Get the hate of the actor against the target
            aggro = me.getHating(target);

            if (aggro <= 0) return;

            // Set the actor AI Intention to AI_INTENTION_ATTACK
            if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
            {
                // Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
                if (!_actor.isRunning()) _actor.setRunning();

                L2SiegeGuardInstance sGuard = (L2SiegeGuardInstance) _actor;
                double homeX = target.getX() - sGuard.getHomeX();
                double homeY = target.getY() - sGuard.getHomeY();

                // Check if the L2SiegeGuardInstance is not too far from its home location
                if (homeX * homeX + homeY * homeY < 3240000) // 1800 * 1800 
                    setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
            }
        }
        else
        {
            _globalAggro += aggro;
        }
    }
    
    protected void onEvtDead()
    {
        stopAITask();
        super.onEvtDead();
    }
    
    public void stopAITask()
    {
        if (aiTask != null)
        {
            aiTask.cancel(false);
            aiTask = null;
        }
        _accessor.detachAI();
    }

}
