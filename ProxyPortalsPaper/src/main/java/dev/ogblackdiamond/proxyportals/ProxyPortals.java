package dev.ogblackdiamond.proxyportals;

import dev.ogblackdiamond.proxyportals.commands.Register;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.mojang.brigadier.Command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;


public class ProxyPortals extends JavaPlugin implements Listener {


    boolean isRegistering = false;
    
    String toRegister = null;

    Player registeringPlayer = null;


    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "proxyportals:main");

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
    public void onPlayerPortal(EntityPortalEnterEvent event) {

        if (isRegistering && registeringPlayer == event.getEntity()) {
            if (toRegister == null) return;
            // register the portal here
            Bukkit.getLogger().info("registered portal " + toRegister);
            
            ArrayList<Location> portalBlocksList = new ArrayList<Location>();

            registerPortal(event.getLocation().getBlock(), portalBlocksList);

            event.getEntity().sendMessage("Successfully registered server " + toRegister);

            isRegistering = false;
            toRegister = null;
            registeringPlayer = null;
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

