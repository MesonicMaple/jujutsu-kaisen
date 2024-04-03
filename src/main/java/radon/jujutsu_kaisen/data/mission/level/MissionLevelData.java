package radon.jujutsu_kaisen.data.mission.level;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import radon.jujutsu_kaisen.data.mission.Mission;
import radon.jujutsu_kaisen.data.mission.MissionGrade;
import radon.jujutsu_kaisen.data.mission.MissionType;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncMissionLevelDataS2CPacket;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class MissionLevelData implements IMissionLevelData {
    private final Set<Mission> missions;

    private final Level level;

    public MissionLevelData(Level level) {
        this.level = level;

        this.missions = new LinkedHashSet<>();
    }

    @Override
    public void tick() {
        // Remove missions that dont't have any curses
        Iterator<Mission> missionsIter = this.missions.iterator();

        while (missionsIter.hasNext()) {
            Mission mission = missionsIter.next();

            Set<UUID> curses = mission.getCurses();

            if (curses.isEmpty()) {
                missionsIter.remove();
                continue;
            }

            Iterator<UUID> cursesIter = curses.iterator();

            while (cursesIter.hasNext()) {
                UUID identifier = cursesIter.next();

                Entity curse = ((ServerLevel) this.level).getEntity(identifier);

                if (curse == null) cursesIter.remove();
            }
        }
    }

    @Override
    public void register(MissionType type, MissionGrade grade, BlockPos pos) {
        this.missions.add(new Mission(this.level.dimension(), type, grade, pos));

        PacketHandler.broadcast(new SyncMissionLevelDataS2CPacket(this.level.dimension(), this.serializeNBT()));
    }

    @Override
    public Set<Mission> getMissions() {
        return this.missions;
    }

    @Override
    public boolean isRegistered(BlockPos pos) {
        for (Mission mission : this.missions) {
            if (mission.getPos().equals(pos)) return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Mission getMission(BlockPos pos) {
        for (Mission mission : this.missions) {
            if (mission.getPos().equals(pos)) return mission;
        }
        return null;
    }

    @Override
    public void removeMission(Mission mission) {
        this.missions.remove(mission);
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        ListTag missionsTag = new ListTag();

        for (Mission mission : this.missions) {
            missionsTag.add(mission.serializeNBT());
        }
        nbt.put("missions", missionsTag);

        return nbt;
    }

    @Override
    public void deserializeNBT(@NotNull CompoundTag nbt) {
        this.missions.clear();

        for (Tag tag : nbt.getList("missions", Tag.TAG_COMPOUND)) {
            this.missions.add(new Mission((CompoundTag) tag));
        }
    }
}
