package dev.ogblackdiamond.proxyportals;


import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import dev.ogblackdiamond.proxyportals.commands.DeRegister;
import dev.ogblackdiamond.proxyportals.commands.Register;
import dev.ogblackdiamond.proxyportals.database.DataBase;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;


import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


/**
 *  Main class for ProxyPortals.
 */
public class ProxyPortals extends JavaPlugin implements Listener {

    private static final Logger LOGGER = Logger.getLogger("ProxyPortals");
    DataBase dataBase;

    boolean isRegistering = false;
    
    String toRegister = null;

    Player registeringPlayer = null;

    boolean lobbyMode;
    boolean noPlayerDie; 

    int portalTick = 0;

    int portalTickRate;

    @Override
    public void onEnable() {

        // register this plugin as an event listener
        Bukkit.getPluginManager().registerEvents(this, this);

        // register this plugin as a message sender on the "BungeeCord" channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        // save config file
        saveDefaultConfig(); 

        // set lobby and tick rate from config
        lobbyMode = getConfig().getBoolean("lobby-mode");
        noPlayerDie = getConfig().getBoolean("no-player-die");
        portalTickRate = getConfig().getInt("portal-tick-rate");

        // initialize the database
        dataBase = new DataBase();
        dataBase.connect();

        // register the "register-portal" command
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                "register-server",
                "prepares to register a portal",
                new Register(this)
            );

        });

        // register the "deregister-portal" command
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                "deregister-server",
                "deregisters a portal",
                new DeRegister(dataBase)
            );

        });



        Bukkit.getLogger().info("[ProxyPortals] Thank you for using ProxyPortals");

    }

    /**
     *  Teleports the player back to spawn when they join so there isn't an infinite teleport loop.
     */
    @EventHandler
    public void onPlayerConnect(PlayerJoinEvent event) {
        if (lobbyMode) {
            event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
        }

        if (noPlayerDie) {
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 255, false, false, false));
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, PotionEffect.INFINITE_DURATION, 255, false, false, false));
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, PotionEffect.INFINITE_DURATION, 255, false, false, false));
        }
    }

    /**
     *  Decides what to do when an entity enters a portal.
     *  Possibilites:
     *  - connect player to other server
     *  - register new server
     *  - do nothing
     */
    @EventHandler
    public void onPlayerPortal(EntityPortalEnterEvent event) {

        // ensures that a player is being detected
        if (!(event.getEntity() instanceof Player)) return;

        // registers a new portal
        if (isRegistering && registeringPlayer == event.getEntity()) {
            // null check
            if (toRegister == null) return;

            // register the portal here
        
            ArrayList<Location> portalBlocksList = new ArrayList<Location>();

            registerPortal(event.getLocation().getBlock(), portalBlocksList);

            dataBase.registerServer(portalBlocksList, toRegister);
           
            // output success confirmation
            event.getEntity().sendMessage("Successfully registered server " + toRegister);
            Bukkit.getLogger().info("registered portal " + toRegister);

            // return variables to defualts after registration
            isRegistering = false;
            toRegister = null;
            registeringPlayer = null;
            return;

        } else {

            portalTick++;

            String server = dataBase.checkPortal(event.getLocation());

            if (server != null) {
                // get playername
                String playerName = event.getEntity().getName();
                
                // initialize dataoutput
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(server);

                // gets the player in the portal and sets the teleport location
                Player player = Bukkit.getPlayer(playerName);
                Location tpLocation = player.getWorld().getSpawnLocation();

                // checks if the server is on a tick where it can safely send the player
                if (portalTick >= portalTickRate) portalTick = 0; else return;

                // teleports the player away to prevent repeat checks (and therefore errors)
                player.teleport(tpLocation);

                //logging message sent to Velociy / BungeeCord for troubleshooting
                LOGGER.info("Sending plugin message to BungeeCord: Connect to " + server);

                //sends player to new server
                player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
            }
        }
    }

    /**
     *  Sets registering mode.
     *  This is used by Register.java
     */
    public void setRegistering(String server, Player player) {
        isRegistering = true;
        toRegister = server;
        registeringPlayer = player;
    }

    /**
     * Recursive algorighm to gather all Location objects of every portal block in a registered portal.
     * This checks for portal blocks in layers.
     */
    private void registerPortal(Block block, ArrayList<Location> list) {

        Material portalBlock = Material.NETHER_PORTAL;

        // prevent overflow errors and rechecks
        if (block.getType() != portalBlock) return;
        for (Location loc : list) if (block.getLocation().equals(loc)) return;
        
        // adds this block as a portal block
        list.add(block.getLocation());

        // gets all nearby blocks in the x/z plane
        Block[] blocks = {
            block.getRelative(1, 0, 0),
            block.getRelative(-1, 0, 0),
            block.getRelative(0, 0, 1),
            block.getRelative(0, 0, -1)
        };

        // iterate through nearby blocks and their adjacent blocks to find all nearby portal blocks
        for (Block tempBlock : blocks) {
            while (tempBlock.getType() == portalBlock) {
                list.add(tempBlock.getLocation());
                tempBlock = tempBlock.getRelative(1, 0, 0);
            }
        }
        
        // recursivly check layers above and below current block
        registerPortal(block.getRelative(0, 1, 0), list);
        registerPortal(block.getRelative(0, -1, 0), list);
    }
}

