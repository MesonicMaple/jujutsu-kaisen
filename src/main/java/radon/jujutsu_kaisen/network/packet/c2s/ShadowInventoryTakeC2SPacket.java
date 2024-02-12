package radon.jujutsu_kaisen.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.data.JJKAttachmentTypes;
import radon.jujutsu_kaisen.data.ten_shadows.ITenShadowsData;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncTenShadowsDataS2CPacket;

public class ShadowInventoryTakeC2SPacket implements CustomPacketPayload {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(JujutsuKaisen.MOD_ID, "shadow_inventory_take_serverbound");

    private final int index;

    public ShadowInventoryTakeC2SPacket(int index) {
        this.index = index;
    }

    public ShadowInventoryTakeC2SPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            if (!(ctx.player().orElseThrow() instanceof ServerPlayer sender)) return;

            ITenShadowsData data = sender.getData(JJKAttachmentTypes.TEN_SHADOWS);

            ItemStack stack = data.getShadowInventory(this.index);

            if (sender.getMainHandItem().isEmpty()) {
                sender.setItemSlot(EquipmentSlot.MAINHAND, stack);
            } else {
                if (!sender.addItem(stack)) return;
            }
            data.removeShadowInventory(this.index);

            PacketHandler.sendToClient(new SyncTenShadowsDataS2CPacket(data.serializeNBT()), sender);
        });
    }

    @Override
    public void write(FriendlyByteBuf pBuffer) {

    }

    @Override
    public ResourceLocation id() {
        return null;
    }
}