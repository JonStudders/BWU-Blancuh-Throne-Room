package net.botwithus;

import java.util.*;
import java.util.concurrent.*;
import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.hud.inventories.Inventory;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.game.*;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.minimenu.*;
import net.botwithus.rs3.game.minimenu.actions.*;
import net.botwithus.rs3.game.queries.builders.items.*;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.game.Item;

public class BlancuhThroneRoom extends LoopingScript {

    private BotState botState = BotState.DEPOSITING;
    private boolean someBool = true;
    private String empoweredCrystal = "";
    private Random random = new Random();

    enum BotState {
        //define your own states here
        IDLE,
        WITHDRAWING,
        TRANSMUTING,
        DEPOSITING,
    }

    public BlancuhThroneRoom(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new BlancuhThroneRoomGraphicsContext(getConsole(), this);
    }

    @Override
    public void onLoop() {
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
            Execution.delay(random.nextLong(3000,7000));
            return;
        }
        switch (botState) {
            case IDLE -> {
                println("We're idle!");
                Execution.delay(random.nextLong(1000,3000));
            }
            case WITHDRAWING -> {
                Execution.delay(handleWithdrawing(player));
            }
            case TRANSMUTING -> {
                Execution.delay(handleTransmuting(player));
            }
            case DEPOSITING -> {
                Execution.delay(handleDepositing(player));
            }
        }
    }

    public long handleWithdrawing(LocalPlayer player) {
        SceneObject storageBin = SceneObjectQuery.newQuery().name("Crystal storage bin").option("Withdraw").results().nearest();
        if (storageBin != null ) {
            println("Found storage bin.");
            println("Interacted storage bin: " + storageBin.interact("Withdraw"));
            Execution.delayUntil(3000, () -> Backpack.isFull());
            setBotState(BotState.TRANSMUTING);
        } else {
            println("Did not find storage bin.");
        }
        return random.nextLong(1500,3000);
    }

    public long handleTransmuting(LocalPlayer player) {
        // Check we have senntisten crystals
        // If we do not have any, go to deposit
        // if we do, continue transmuting
        println("Checking inventory has crystals to transmute.");
        Item senntistenCrystal = InventoryItemQuery.newQuery().name("Senntisten crystal").results().first();
        if (senntistenCrystal == null) {
            println("No crystals found. Depositing transmuted crystals.");
            setBotState(BotState.DEPOSITING);
            return random.nextLong(1500,3000);
        }


        // Check which is empowered
        SceneObject currentEmpowered = SceneObjectQuery.newQuery().contains("(Empowered)").option("Deposit").results().nearest();
        if (currentEmpowered == null) {
            println("Empowered crate not found");
            return random.nextLong(1500,3000);
        }

        String currentEmpoweredName = currentEmpowered.getName();
        println("Current empowered: " + currentEmpoweredName);
        if (currentEmpoweredName == null) {
            println("Empowered crate not found");
            return random.nextLong(1500,3000);
        }

        if (currentEmpoweredName.equals(getEmpoweredCrystal()) && Interfaces.isOpen(1251)) {
            println("Already transmuting empowered colour.");
            return random.nextLong(1500,3000);
        } else {
            setEmpoweredCrystal(currentEmpoweredName);
            println("Transmuting");
            boolean interacted = Backpack.interact(senntistenCrystal.getName(), "Transmute");
            println("Interacted: " + interacted);
            if (interacted) {
                println("Sennisten crystal clicked");
                Execution.delayUntil(3000, () -> Interfaces.isOpen(1371));
                if (Interfaces.isOpen(1371)) {
                    println("Transmute window open");
                    if(MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 89784350)){
                        println("Transmute option pressed");
                        return random.nextLong(1500,3000);
                    } else {
                        println("Transmutes option failed.");
                    }
                } else {
                    print("Transmute window failed to open");
                }
            } else {
                println("No sennisten crystals.");
            }
        }
        return random.nextLong(1500,3000);
    }

    public long handleDepositing(LocalPlayer player) {
        println("Checking inventory empty");
        SceneObject currentEmpowered = SceneObjectQuery.newQuery().contains("(Empowered)").option("Deposit").results().nearest();
        if (Backpack.isEmpty()) {
            println("Inventory empty, withdrawing");
            setBotState(BotState.WITHDRAWING);
            return random.nextLong(500,1500);
        } else {
            println("Inventory not empty.");
            if (Backpack.contains("Yellow crystal")) {
                println("Got yellow crystals");
                SceneObject yellowCrate;
                if (currentEmpowered.getName().contains("yellow")) {
                    yellowCrate = currentEmpowered;
                    println("Deposit crate is empowered");
                } else {
                    yellowCrate = SceneObjectQuery.newQuery().contains("Crate of yellow crystals").option("Deposit").results().nearest();
                }
                if (yellowCrate == null ) {
                    println("Yellow crate not found.");
                } else {
                    if (yellowCrate.interact("Deposit")) {
                        println("Deposited");
                    } else {
                        println("Failed to deposit");
                    }
                }
            } else if (Backpack.contains("Green crystal")) {
                println("Got green crystals");
                SceneObject greenCrate;
                if (currentEmpowered.getName().contains("green")) {
                    greenCrate = currentEmpowered;
                    println("Deposit crate is empowered");
                } else {
                    greenCrate = SceneObjectQuery.newQuery().contains("Crate of green crystals").option("Deposit").results().nearest();
                }
                if (greenCrate == null ) {
                    println("Green crate not found.");
                } else {
                    if (greenCrate.interact("Deposit")) {
                        println("Deposited");
                    } else {
                        println("Failed to deposit");
                    }
                }
            } else if (Backpack.contains("Blue crystal")) {
                println("Got blue crystals");
                SceneObject blueCrate;
                if (currentEmpowered.getName().contains("blue")) {
                    blueCrate = currentEmpowered;
                    println("Deposit crate is empowered");
                } else {
                    blueCrate = SceneObjectQuery.newQuery().contains("Crate of blue crystals").option("Deposit").results().nearest();
                }
                if (blueCrate == null ) {
                    println("Blue crate not found.");
                } else {
                    if (blueCrate.interact("Deposit")) {
                        println("Deposited");
                    } else {
                        println("Failed to deposit");
                    }
                }
            } else if (Backpack.contains("Red crystal")) {
                println("Got red crystals");
                SceneObject redCrate;
                if (currentEmpowered.getName().contains("red crystals")) {
                    redCrate = currentEmpowered;
                    println("Deposit crate is empowered");
                } else {
                    redCrate = SceneObjectQuery.newQuery().contains("Crate of red crystals").option("Deposit").results().nearest();
                }
                if (redCrate == null ) {
                    println("Red crate not found.");
                } else {
                    if (redCrate.interact("Deposit")) {
                        println("Deposited");
                    } else {
                        println("Failed to deposit");
                    }
                }
            }
        }
        return random.nextLong(1500,3000);
    }

    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public boolean isSomeBool() {
        return someBool;
    }

    public void setSomeBool(boolean someBool) {
        this.someBool = someBool;
    }

    public String getEmpoweredCrystal() {
        return empoweredCrystal;
    }

    public void setEmpoweredCrystal(String empoweredCrystal) {
        this.empoweredCrystal = empoweredCrystal;
    }
}
