package dev.ogblackdiamond.proxyportals;

import dev.ogblackdiamond.proxyportals.commands.Register;
import dev.ogblackdiamond.proxyportals.database.DataBase;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;


public class ProxyPortals extends JavaPlugin implements Listener {


    DataBase dataBase;

    boolean isRegistering = false;
    
    String toRegister = null;

    Player registeringPlayer = null;

    boolean lobbyMode;

    int portalTick = 0;

    int portalTickRate;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        saveDefaultConfig(); 

        lobbyMode = getConfig().getBoolean("lobby-mode");
        portalTickRate = getConfig().getInt("portal-tick-rate");

        dataBase = new DataBase();
        dataBase.connect();

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                "register-portal",
                "prepares to register a portal",
                new Register(this)
            );

        });

    }

    @EventHandler
    public void onPlayerConnect(PlayerJoinEvent event) {
        if (lobbyMode) {
            event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlayerPortal(EntityPortalEnterEvent event) {

        if (!(event.getEntity() instanceof Player)) return;

        if (isRegistering && registeringPlayer == event.getEntity()) {
            if (toRegister == null) return;
            // register the portal here
            
            ArrayList<Location> portalBlocksList = new ArrayList<Location>();

            registerPortal(event.getLocation().getBlock(), portalBlocksList);

            dataBase.registerServer(portalBlocksList, toRegister);
           
            // output success confirmation
            event.getEntity().sendMessage("Successfully registered server " + toRegister);
            Bukkit.getLogger().info("registered portal " + toRegister);

            isRegistering = false;
            toRegister = null;
            registeringPlayer = null;
            return;

        } else {

            portalTick++;

            String server = dataBase.checkPortal(event.getLocation());

            if (server != null) {
                String playerName = event.getEntity().getName();
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(server);
                Player player = Bukkit.getPlayer(playerName);
                Location tpLocation = new Location(player.getWorld(), 0, 0, 0);
                if (portalTick >= portalTickRate) portalTick = 0; else return;
                player.teleport(tpLocation);
                player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
            } else {
                Bukkit.getLogger().warning("[proxyportals] Teleport event returned null!");
            }
        }

    }

    public void setRegistering(String server, Player player) {
        isRegistering = true;
        toRegister = server;
        registeringPlayer = player;
    }

    private void registerPortal(Block block, ArrayList<Location> list) {

        Material portalBlock = Material.NETHER_PORTAL;

        // prevent overflow errors and rechecks
        if (block.getType() != portalBlock) return;
        for (Location loc : list) if (block.getLocation().equals(loc)) return;
        
        list.add(block.getLocation());

        Block[] blocks = {
            block.getRelative(1, 0, 0),
            block.getRelative(-1, 0, 0),
            block.getRelative(0, 0, 1),
            block.getRelative(0, 0, -1)
        };

        for (Block tempBlock : blocks) {
            while (tempBlock.getType() == portalBlock) {
                list.add(tempBlock.getLocation());
                tempBlock = tempBlock.getRelative(1, 0, 0);
            }
        }
        
        
        registerPortal(block.getRelative(0, 1, 0), list);
        registerPortal(block.getRelative(0, -1, 0), list);
            
    }
}

