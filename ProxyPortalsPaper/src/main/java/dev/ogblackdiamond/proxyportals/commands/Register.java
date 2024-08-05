package dev.ogblackdiamond.proxyportals.commands;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import dev.ogblackdiamond.proxyportals.ProxyPortals;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Register implements BasicCommand {

    private final Component success = Component.text()
        .content("Walk into the portal you wish to register.")
        .color(NamedTextColor.BLUE)
        .build();

    private final Component faliure = Component.text()
        .content("You must provide the server you wish to register as an argument!")
        .color(NamedTextColor.RED)
        .build();

    private ProxyPortals portals;

    public Register(ProxyPortals portals) {
        this.portals = portals; 
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        stack.getSender().sendMessage(args.length > 0 ? success : faliure);

        if (args.length < 1) return;

        portals.setRegistering(
            args[0],
            stack.getSender().getServer().getPlayer(stack.getSender().getName())
        );
    }

}
