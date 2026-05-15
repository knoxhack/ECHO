package com.knoxhack.echorecovery.registry;

import com.knoxhack.echorecovery.EchoRecovery;
import com.knoxhack.echorecovery.item.GraveKeyItem;
import com.knoxhack.echorecovery.item.RecoveryCompassItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EchoRecovery.MODID);

    public static final DeferredItem<Item> GRAVE_KEY = ITEMS.registerItem("grave_key",
        GraveKeyItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<Item> RECOVERY_COMPASS = ITEMS.registerItem("recovery_compass",
        RecoveryCompassItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<Item> DEATH_RECORD = ITEMS.registerItem("death_record",
        Item::new, p -> p.stacksTo(16));
    public static final DeferredItem<Item> RECOVERY_TOKEN = ITEMS.registerItem("recovery_token",
        Item::new, p -> p.stacksTo(1));

    private ModItems() {}

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
