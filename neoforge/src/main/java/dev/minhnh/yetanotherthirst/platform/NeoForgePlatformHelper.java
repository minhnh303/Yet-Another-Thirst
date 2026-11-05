package dev.minhnh.yetanotherthirst.platform;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.NeoForgeHandDrinkClient;
import dev.minhnh.yetanotherthirst.NeoForgeNetwork;
import dev.minhnh.yetanotherthirst.platform.services.IPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Constants.MOD_ID);

    public static final Supplier<AttachmentType<CompoundTag>> THIRST_ATTACHMENT = ATTACHMENT_TYPES.register(
            "thirst",
            () -> AttachmentType.builder((Supplier<CompoundTag>) CompoundTag::new)
                    .serialize(new IAttachmentSerializer<CompoundTag>() {
                        @Override
                        public CompoundTag read(IAttachmentHolder holder, ValueInput input) {
                            return input.read("tag", CompoundTag.CODEC).orElseGet(CompoundTag::new);
                        }

                        @Override
                        public boolean write(CompoundTag attachment, ValueOutput output) {
                            output.store("tag", CompoundTag.CODEC, attachment);
                            return true;
                        }
                    })
                    .copyOnDeath()
                    .build()
    );

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.isProduction();
    }

    @Override
    public CompoundTag loadThirstData(Player player) {
        return player.getData(THIRST_ATTACHMENT.get());
    }

    @Override
    public void saveThirstData(Player player, CompoundTag tag) {
        player.setData(THIRST_ATTACHMENT.get(), tag);
    }

    @Override
    public CompoundTag loadPersistentData(Player player, String key) {
        return player.getPersistentData().getCompoundOrEmpty(key);
    }

    @Override
    public void sendThirstSync(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {
        NeoForgeNetwork.sendToPlayer(player, thirst, quenched, exhaustion, enabled);
    }

    @Override
    public boolean tryHandDrink() {
        if (FMLEnvironment.getDist().isClient()) {
            return NeoForgeHandDrinkClient.tryDrink();
        }
        return false;
    }
}
