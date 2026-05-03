package com.knoxhack.echoashfallprotocol.worldgen;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;

/**
 * Utility class for generating structure templates.
 * Delegates to the POI generator used by the release resource workflow.
 */
public class StructureTemplateGenerator {
    
    /**
     * Generates all POI structure templates.
     *
     * @return true when generation completed without errors.
     */
    public static boolean generateAllStructures() {
        EchoAshfallProtocol.LOGGER.info("[StructureTemplateGenerator] Generating all structure templates...");
        try {
            POIStructureGenerator.main(new String[0]);
            EchoAshfallProtocol.LOGGER.info("[StructureTemplateGenerator] All structure templates generated.");
            return true;
        } catch (Exception e) {
            EchoAshfallProtocol.LOGGER.error("[StructureTemplateGenerator] POI generation failed: {}", e.getMessage(), e);
            return false;
        }
    }
}
