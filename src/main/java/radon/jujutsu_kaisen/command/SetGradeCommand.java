package radon.jujutsu_kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.command.EnumArgument;
import radon.jujutsu_kaisen.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.data.JJKAttachmentTypes;
import radon.jujutsu_kaisen.data.capability.IJujutsuCapability;
import radon.jujutsu_kaisen.data.capability.JujutsuCapabilityHandler;
import radon.jujutsu_kaisen.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class SetGradeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(Commands.literal("setgrade")
                .requires(player -> player.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.entity()).then(Commands.argument("grade", EnumArgument.enumArgument(SorcererGrade.class))
                        .executes(ctx -> setGrade(EntityArgument.getPlayer(ctx, "player"), ctx.getArgument("grade", SorcererGrade.class))))));

        dispatcher.register(Commands.literal("setgrade").requires((player) -> player.hasPermission(2)).redirect(node));
    }

    public static int setGrade(ServerPlayer player, SorcererGrade grade) {
        IJujutsuCapability cap = player.getCapability(JujutsuCapabilityHandler.INSTANCE);

        if (cap == null) return 0;

        ISorcererData data = cap.getSorcererData();

        data.setExperience(grade.getRequiredExperience());

        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(data.serializeNBT()), player);

        return 1;
    }
}
