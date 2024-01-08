package radon.jujutsu_kaisen.item.cursed_tool;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.capability.data.ISorcererData;
import radon.jujutsu_kaisen.capability.data.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.client.render.item.DragonBoneRenderer;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.item.base.CursedToolItem;
import radon.jujutsu_kaisen.util.EntityUtil;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

public class DragonBoneItem extends CursedToolItem implements GeoItem {
    private static final float MAX_ENERGY = 100.0F;
    private static final double RANGE = 5.0D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DragonBoneItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public SorcererGrade getGrade() {
        return SorcererGrade.SPECIAL_GRADE;
    }

    public static float getEnergy(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        return nbt.getFloat("energy");
    }

    public static void addEnergy(ItemStack stack, float energy) {
        CompoundTag nbt = stack.getOrCreateTag();

        if (nbt.contains("energy")) {
            energy += nbt.getFloat("energy");
        }
        nbt.putFloat("energy", energy);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        float charge = getEnergy(stack) / MAX_ENERGY;

        if (charge > 0.0F) {
            if (RotationUtil.getLookAtHit(pPlayer, RANGE) instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity entity) {
                pPlayer.teleportTo(entity.getX(), entity.getY(), entity.getZ());
                ISorcererData cap = pPlayer.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                entity.hurt(JJKDamageSources.jujutsuAttack(pPlayer, null), this.getDamage() * cap.getRealPower() * charge);
                return InteractionResultHolder.consume(stack);
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        pTooltipComponents.add(Component.translatable(String.format("item.%s.%s.energy", JujutsuKaisen.MOD_ID, this.getDescriptionId()), (getEnergy(pStack) / MAX_ENERGY) * 100.0F));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DragonBoneRenderer renderer;

            @Override
            public DragonBoneRenderer getCustomRenderer() {
                if (this.renderer == null) this.renderer = new DragonBoneRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
