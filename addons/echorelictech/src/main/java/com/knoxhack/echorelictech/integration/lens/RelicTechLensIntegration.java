package com.knoxhack.echorelictech.integration.lens;

import com.knoxhack.echorelictech.EchoRelicTech;
import com.knoxhack.echorelictech.api.RelicTechApi;
import com.knoxhack.echorelictech.block.entity.ContainmentLockerBlockEntity;
import com.knoxhack.echorelictech.block.entity.NullBatteryDockBlockEntity;
import com.knoxhack.echorelictech.block.entity.PrototypeWorkbenchBlockEntity;
import com.knoxhack.echorelictech.block.entity.RelicAnalyzerBlockEntity;
import com.knoxhack.echorelictech.registry.ModBlocks;
import com.knoxhack.echorelictech.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RelicTechLensIntegration {
    public static void register() {
        EchoRelicTech.LOGGER.info("ECHO Lens integration loaded for RelicTech.");
        try {
            Class<?> providerRegistry = Class.forName("com.knoxhack.echolens.registry.LensProviderRegistry");
            Class<?> blockProvider = Class.forName("com.knoxhack.echolens.api.BlockLensProvider");
            Class<?> machineProvider = Class.forName("com.knoxhack.echolens.api.MachineLensProvider");
            Class<?> contextClass = Class.forName("com.knoxhack.echolens.api.LensContext");
            Class<?> sectionClass = Class.forName("com.knoxhack.echolens.api.LensInfoSection");
            Class<?> rowClass = Class.forName("com.knoxhack.echolens.api.LensInfoRow");
            Class<?> catEnum = Class.forName("com.knoxhack.echolens.api.LensDataCategory");
            Class<?> toneEnum = Class.forName("com.knoxhack.echolens.api.LensTone");
            Class<?> visEnum = Class.forName("com.knoxhack.echolens.api.LensVisibility");

            java.lang.reflect.Method register = providerRegistry.getMethod("register", Class.forName("com.knoxhack.echolens.api.LensInfoProvider"));

            Object provider = java.lang.reflect.Proxy.newProxyInstance(
                RelicTechLensIntegration.class.getClassLoader(),
                new Class[]{blockProvider, machineProvider},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("id".equals(name)) return Identifier.fromNamespaceAndPath("echorelictech", "relic_machines");
                    if ("priority".equals(name)) return 300;
                    if ("category".equals(name)) return catEnum.getEnumConstants()[4]; // MACHINE
                    if ("supports".equals(name)) return true;
                    if ("inspectBlock".equals(name)) {
                        LensContextAdapter ctx = new LensContextAdapter(args[0]);
                        BlockState state = (BlockState) args[1];
                        return inspectBlock(ctx, state, sectionClass, rowClass, toneEnum, visEnum);
                    }
                    if ("inspect".equals(name)) {
                        return java.util.List.of();
                    }
                    return null;
                }
            );
            register.invoke(null, provider);
        } catch (Exception | LinkageError e) {
            EchoRelicTech.LOGGER.warn("Lens integration could not fully register.", e);
        }
    }

    private static List<Object> inspectBlock(LensContextAdapter ctx, BlockState state,
                                               Class<?> sectionClass, Class<?> rowClass,
                                               Class<?> toneEnum, Class<?> visEnum) throws Exception {
        var block = state.getBlock();
        List<Object> sections = new ArrayList<>();
        Object infoTone = toneEnum.getEnumConstants()[1]; // INFO
        Object expandedVis = visEnum.getEnumConstants()[1]; // EXPANDED

        if (block == ModBlocks.RELIC_ANALYZER.get()) {
            if (ctx.blockEntity() instanceof RelicAnalyzerBlockEntity be) {
                List<Object> rows = new ArrayList<>();
                rows.add(createRow(rowClass, "Status", be.hasOutput() ? "Analysis complete" : be.getInput().isEmpty() ? "Idle" : "Analyzing...", "A", infoTone, expandedVis));
                rows.add(createRow(rowClass, "Input", be.getInput().isEmpty() ? "None" : be.getInput().getHoverName().getString(), "I", infoTone, expandedVis));
                sections.add(createSection(sectionClass, "echorelictech:analyzer", "Relic Analyzer", "#", infoTone, expandedVis, rows));
            }
        } else if (block == ModBlocks.PROTOTYPE_WORKBENCH.get()) {
            if (ctx.blockEntity() instanceof PrototypeWorkbenchBlockEntity be) {
                List<Object> rows = new ArrayList<>();
                rows.add(createRow(rowClass, "Relic", be.getRelicSlot().isEmpty() ? "None" : be.getRelicSlot().getHoverName().getString(), "R", infoTone, expandedVis));
                rows.add(createRow(rowClass, "Material", be.getMaterialSlot().isEmpty() ? "None" : be.getMaterialSlot().getHoverName().getString(), "M", infoTone, expandedVis));
                sections.add(createSection(sectionClass, "echorelictech:workbench", "Prototype Workbench", "#", infoTone, expandedVis, rows));
            }
        } else if (block == ModBlocks.CONTAINMENT_LOCKER.get()) {
            if (ctx.blockEntity() instanceof ContainmentLockerBlockEntity be) {
                List<Object> rows = new ArrayList<>();
                int occupied = 0;
                for (int i = 0; i < be.getContainerSize(); i++) if (!be.getItem(i).isEmpty()) occupied++;
                rows.add(createRow(rowClass, "Occupied", occupied + "/" + be.getContainerSize(), "O", infoTone, expandedVis));
                sections.add(createSection(sectionClass, "echorelictech:locker", "Containment Locker", "#", infoTone, expandedVis, rows));
            }
        } else if (block == ModBlocks.NULL_BATTERY_DOCK.get()) {
            if (ctx.blockEntity() instanceof NullBatteryDockBlockEntity be) {
                List<Object> rows = new ArrayList<>();
                rows.add(createRow(rowClass, "Battery", be.getBattery().isEmpty() ? "None" : be.getBattery().getHoverName().getString(), "B", infoTone, expandedVis));
                rows.add(createRow(rowClass, "Charge", String.valueOf(be.getBattery().getOrDefault(com.knoxhack.echorelictech.registry.ModDataComponents.NULL_CHARGE.get(), 0)), "C", infoTone, expandedVis));
                sections.add(createSection(sectionClass, "echorelictech:dock", "Null Battery Dock", "#", infoTone, expandedVis, rows));
            }
        }
        return sections;
    }

    private static Object createRow(Class<?> rowClass, String label, String value, String icon, Object tone, Object visibility) throws Exception {
        return rowClass.getMethod("of", String.class, String.class, String.class, tone.getClass().getSuperclass(), visibility.getClass().getSuperclass())
            .invoke(null, label, value, icon, tone, visibility);
    }

    private static Object createSection(Class<?> sectionClass, String id, String title, String icon, Object tone, Object visibility, List<Object> rows) throws Exception {
        return sectionClass.getMethod("of", Identifier.class, sectionClass.getDeclaringClass().getClassLoader().loadClass("com.knoxhack.echolens.api.LensDataCategory"), String.class, String.class, tone.getClass().getSuperclass(), visibility.getClass().getSuperclass(), List.class)
            .invoke(null, Identifier.parse(id), sectionClass.getDeclaringClass().getClassLoader().loadClass("com.knoxhack.echolens.api.LensDataCategory").getEnumConstants()[4], title, icon, tone, visibility, rows);
    }

    private static class LensContextAdapter {
        private final Object ctx;
        LensContextAdapter(Object ctx) { this.ctx = ctx; }
        net.minecraft.world.level.block.entity.BlockEntity blockEntity() {
            try {
                var method = ctx.getClass().getMethod("blockEntity");
                return (net.minecraft.world.level.block.entity.BlockEntity) method.invoke(ctx);
            } catch (Exception e) { return null; }
        }
    }
}
