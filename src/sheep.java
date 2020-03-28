import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Production;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.util.Arrays;

@ScriptMeta(name = "Sheep",  desc = "Script description", developer = "Developer's Name", category = ScriptCategory.MAGIC)
public class sheep extends Script {
    private static final int VARP = 179;
    private static final Area farm = Area.rectangular(3184, 3278, 3192, 3270);
    private static final Area sheepPen = Area.rectangular(3211, 3268, 3193, 3257);
    private static Position spinningRoom = new Position(3209, 3213, 1);
    private static final Position shearsLoc = new Position(3191, 3272);
    private boolean needToSpin = false;

    @Override
    public int loop()
    {
        if(Varps.get(VARP) == 21)
            return -1;
        else if(Varps.get(VARP) == 20)
        {
            Dialog.processContinue();
        }
        else if(Varps.get(VARP) == 0) // start the quest
        {
            if(farm.contains(Players.getLocal()))
            {
                if(Dialog.isOpen())
                {
                    if(Dialog.canContinue())
                    {
                        Dialog.processContinue();
                    }
                    else if(Dialog.isViewingChatOptions())
                    {
                        Dialog.process(0);
                    }
                }
                else
                {
                    Npc farmer = Npcs.getNearest("Fred the Farmer");
                    if(farmer != null)
                    {
                        if(farmer.isPositionWalkable())
                        {
                            farmer.interact("Talk-to");
                            Time.sleepUntil(()-> Dialog.isOpen(), 3000);
                        }
                        else
                        {
                            Movement.walkToRandomized(farmer);
                            Time.sleepUntil(()-> farmer.isPositionWalkable() || !Players.getLocal().isMoving(),3000);
                        }

                    }
                    else
                        Log.info("Cant find farmer");

                }
            }
            else
            {
                Movement.walkToRandomized(farm.getCenter());
                Time.sleepUntil(()-> farm.contains(Players.getLocal()) || !Players.getLocal().isMoving(), 3000);
            }


        }
        else if(Varps.get(VARP) == 1)
        {
            Log.info("Varp == 1");
            if(!Inventory.contains("Shears"))
            {
                if(Players.getLocal().getPosition().equals(shearsLoc))
                {
                    Log.info("We are on square");
                    Pickables.getNearest("Shears").interact("Take");
                    Time.sleepUntil(()-> Inventory.contains("Shears"),3000);

                }
                else
                {
                    Movement.walkTo(shearsLoc);
                    Time.sleepUntil(()-> !Players.getLocal().isMoving()
                            || Players.getLocal().getPosition().equals(shearsLoc),3000);
                }
            }
            else if(Inventory.getCount("Ball of wool") == 20)
            {
                if(farm.contains(Players.getLocal()))
                {
                    if(Dialog.isOpen())
                    {
                        if(Dialog.canContinue())
                        {
                            Dialog.processContinue();
                        }
                        else if(Dialog.isViewingChatOptions())
                        {
                            Dialog.process(1);
                        }
                    }
                    else
                    {
                        Npc farmer = Npcs.getNearest("Fred the Farmer");
                        if(farmer != null)
                        {
                            if(farmer.isPositionWalkable())
                            {
                                farmer.interact("Talk-to");
                                Time.sleepUntil(()-> Dialog.isOpen(), 3000);
                            }
                            else
                            {
                                Movement.walkToRandomized(farmer);
                                Time.sleepUntil(()-> farmer.isPositionWalkable() || !Players.getLocal().isMoving(),3000);
                            }

                        }
                        else
                            Log.info("Cant find farmer");

                    }
                }
                else
                {
                    Movement.walkToRandomized(farm.getCenter());
                    Time.sleepUntil(()-> farm.contains(Players.getLocal()) || !Players.getLocal().isMoving(), 3000);
                }

            }
            else if(needToSpin)
            {
                if(Players.getLocal().getPosition().equals(spinningRoom))
                {
                    if(Players.getLocal().isAnimating())
                    {
                        Time.sleepUntil(()-> !Inventory.contains("Wool"), 5000);
                    }
                    else if(Production.isOpen())
                    {
                        Production.initiate(0);
                        Time.sleepUntil(()-> !Inventory.contains("Wool"), 15000);
                    }
                    else
                    {
                        SceneObjects.getNearest("Spinning wheel").interact("Spin");
                        Time.sleepUntil(()-> Production.isOpen(),3000);
                    }
                }
                else
                {
                    Movement.walkTo(spinningRoom);
                    Time.sleepUntil(()-> !Players.getLocal().isMoving() || Players.getLocal().getPosition().equals(spinningRoom), 3000);
                }
            }
            else
            {
                if(Inventory.getCount("Wool") + Inventory.getCount("Ball of wool") == 20)
                {
                    needToSpin = true;
                }
                else if(sheepPen.contains(Players.getLocal()))
                {
                    int woolCount = Inventory.getCount("Wool");
                    Npcs.getNearest(a-> a.getName().equals("Sheep") && a.getId() != 731 && a.containsAction("Shear") && sheepPen.contains(a)).interact("Shear");
                    Time.sleepUntil(()-> woolCount < Inventory.getCount("Wool"),3000);
                }
                else
                {
                    Movement.walkToRandomized(sheepPen.getCenter());
                    Time.sleepUntil(()-> sheepPen.contains(Players.getLocal()) || !Players.getLocal().isMoving(), 3000);
                }
            }
        }

        return Random.nextInt(333,999);
    }
}
