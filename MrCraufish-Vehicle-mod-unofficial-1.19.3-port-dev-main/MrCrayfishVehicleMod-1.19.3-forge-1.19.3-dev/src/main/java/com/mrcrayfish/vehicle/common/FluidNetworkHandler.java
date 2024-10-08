package com.mrcrayfish.vehicle.common;

import com.mrcrayfish.vehicle.block.FluidPipeBlock;
import com.mrcrayfish.vehicle.block.entity.PipeBlockEntity;
import com.mrcrayfish.vehicle.block.entity.PumpBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles updating the disabled state of pipes. This runs after everything has ticked
 * to avoid race conditions.
 *
 * Author: MrCrayfish
 */
public class FluidNetworkHandler
{
    private static FluidNetworkHandler instance;

    public static FluidNetworkHandler instance()
    {
        if(instance == null)
        {
            instance = new FluidNetworkHandler();
        }
        return instance;
    }

    private boolean dirty = false;
    private Map<ResourceKey<Level>, Set<BlockPos>> pipeUpdateMap = new HashMap<>();

    private FluidNetworkHandler() {}

    public void addPipeForUpdate(PipeBlockEntity tileEntity)
    {
        if(!(tileEntity instanceof PumpBlockEntity))
        {
            this.dirty = true;
            this.pipeUpdateMap.computeIfAbsent(tileEntity.getLevel().dimension(), key -> new HashSet<>()).add(tileEntity.getBlockPos());
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.LevelTickEvent event)
    {
        if(!this.dirty)
            return;

        if(event.phase != TickEvent.Phase.END)
            return;

        Set<BlockPos> positions = this.pipeUpdateMap.remove(event.level.dimension());
        if(positions != null)
        {
            positions.forEach(pos ->
            {
                BlockEntity tileEntity = event.level.getBlockEntity(pos);
                if(tileEntity instanceof PipeBlockEntity pipeTileEntity)
                {
                    BlockState state = pipeTileEntity.getBlockState();
                    boolean disabled = pipeTileEntity.getPumps().isEmpty() || event.level.hasNeighborSignal(pos);
                    event.level.setBlock(pos, state.setValue(FluidPipeBlock.DISABLED, disabled), (1 << 0) | (1 << 1));
                }
            });
        }

        if(this.pipeUpdateMap.isEmpty())
        {
            this.dirty = false;
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        this.dirty = false;
    }
}
