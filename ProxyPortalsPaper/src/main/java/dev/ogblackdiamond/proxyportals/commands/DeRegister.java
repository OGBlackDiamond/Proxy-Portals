package dev.ogblackdiamond.proxyportals.commands;

import dev.ogblackdiamond.proxyportals.database.DataBase;

import org.jetbrains.annotations.NotNull;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DeRegister implements BasicCommand {

    private final Component success = Component.text()
        .content("Successfully deregistered portal.")
        .color(NamedTextColor.BLUE)
        .build();

    private final Component faliure = Component.text()
        .content("You must provide the server you wish to deregister as an argument!")
        .color(NamedTextColor.RED)
        .build();

    DataBase dataBase;

    public DeRegister(DataBase dataBase) {
        this.dataBase = dataBase;
    }


    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        stack.getSender().sendMessage(args.length > 0 ? success : faliure);

        if (args.length < 1) return;

        dataBase.deregisterServer(args[0]);
        
    }
}
