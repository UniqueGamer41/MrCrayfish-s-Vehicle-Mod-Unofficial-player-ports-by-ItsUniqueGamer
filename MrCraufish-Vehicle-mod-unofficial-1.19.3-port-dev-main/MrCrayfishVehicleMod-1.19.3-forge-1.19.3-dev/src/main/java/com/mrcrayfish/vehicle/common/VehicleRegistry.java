package com.mrcrayfish.vehicle.common;

import com.mrcrayfish.vehicle.entity.IEngineTier;
import com.mrcrayfish.vehicle.entity.IEngineType;
import com.mrcrayfish.vehicle.item.EngineItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class VehicleRegistry
{
    private static final Set<Entry<?>> REGISTERED_VEHICLES = new HashSet<>();
    private static final Map<ResourceLocation, IEngineType> ID_TO_ENGINE_TYPE = new HashMap<>();
    private static final Map<Pair<IEngineType, IEngineTier>, EngineItem> PAIR_TO_ENGINE_ITEM = new HashMap<>();

    public static synchronized void registerVehicle(Entry<?> location)
    {
        REGISTERED_VEHICLES.add(location);
    }

    @Nullable
    public static Entry<?> getEntry(ResourceLocation name)
    {
        for(Entry<?> entry : getRegisteredVehicles())
        {
            if(entry.name().equals(name))
            {
                return entry;
            }
        }

        return null;
    }

    public static Set<Entry<?>> getRegisteredVehicles()
    {
        return REGISTERED_VEHICLES;
    }

    public static synchronized void registerEngine(IEngineType type, IEngineTier tier, EngineItem item)
    {
        Pair<IEngineType, IEngineTier> pair = Pair.of(type, tier);
        if(PAIR_TO_ENGINE_ITEM.containsKey(pair))
        {
            throw new IllegalArgumentException("The given engine type/tier pair is already registered!");
        }
        PAIR_TO_ENGINE_ITEM.put(pair, item);
        ID_TO_ENGINE_TYPE.put(type.getId(), type);
    }

    @Nullable
    public static IEngineType getEngineTypeFromId(ResourceLocation id)
    {
        return ID_TO_ENGINE_TYPE.get(id);
    }

    @Nullable
    public static EngineItem getEngineItem(IEngineType type, IEngineTier tier)
    {
        return PAIR_TO_ENGINE_ITEM.get(Pair.of(type, tier));
    }

    public record Entry<T extends EntityType<?>>(ResourceLocation name, Supplier<T> entityType)
    {}
}
