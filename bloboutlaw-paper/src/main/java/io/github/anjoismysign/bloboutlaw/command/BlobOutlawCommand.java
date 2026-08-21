package io.github.anjoismysign.bloboutlaw.command;

import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawProfile;
import io.github.anjoismysign.skeramidcommands.command.Command;
import io.github.anjoismysign.skeramidcommands.command.CommandTarget;
import io.github.anjoismysign.skeramidcommands.commandtarget.CommandTargetBuilder;
import io.github.anjoismysign.skeramidcommands.server.bukkit.BukkitAdapter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum BlobOutlawCommand {
    INSTANCE;

    private static final String COMMAND_NAME = "bloboutlaw";
    private static final String COMMAND_PERMISSION = "bloboutlaw";
    private static final String COMMAND_DESCRIPTION = "Base command for bloboutlaw plugin";

    private static final Command COMMAND = BukkitAdapter.getInstance().createCommand(COMMAND_NAME, COMMAND_PERMISSION, COMMAND_DESCRIPTION);

    private static final BlobLibMessageAPI MESSAGE_API = BlobLibMessageAPI.getInstance();

    public void load(){
        CommandTarget<BukkitOutlawProfile> outlaws = CommandTargetBuilder.fromMap(()->{
           var profiles = BlobOutlaw.getInstance().getAccountCruder().getAccounts();
            Map<String, BukkitOutlawProfile> warranted = new HashMap<>();
            profiles.forEach(outlaw->{
                @Nullable Player player = outlaw.player();
                if (player == null){
                    return;
                }
                warranted.put(player.getName(), outlaw);
            });
            return warranted;
        });
        Command charges = COMMAND.child("charges");

        Command clearCharges = charges.child("clear");
        clearCharges.setParameters(outlaws);
        clearCharges.onExecute((permissionMessenger, args) -> {
            if (args.length < 1){
                return;
            }
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            @Nullable BukkitOutlawProfile outlaw = outlaws.parse(args[0]);
            if (outlaw == null) {
                MESSAGE_API
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            @Nullable Player player = outlaw.player();
            if (player == null){
                MESSAGE_API
                    .getMessage("Player.Not-Found", sender)
                    .toCommandSender(sender);
                return;
            }
            if (!outlaw.isWanted()){
                MESSAGE_API
                        .getMessage("BlobOutlaw.Player-Has-No-Crimes", sender)
                        .modder()
                        .replace("%player%", player.getName())
                        .get()
                        .toCommandSender(sender);
                return;
            }
            outlaw.clearCharges();
            MESSAGE_API
                    .getMessage("BlobOutlaw.Other-Clear-Charges", sender)
                    .modder()
                    .replace("%player%", player.getName())
                    .get()
                    .toCommandSender(sender);
        });
    }
}
