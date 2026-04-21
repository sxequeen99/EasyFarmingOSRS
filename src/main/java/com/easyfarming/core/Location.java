package com.easyfarming.core;

import com.easyfarming.EasyFarmingConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Location {
    private String name;
    private Boolean farmLimps;
    private List<Teleport> teleportOptions;
    private EasyFarmingConfig config;
    private final Function<EasyFarmingConfig, EasyFarmingConfig.OptionEnumTeleport> selectedTeleportFunction;

    // NEW: Map to hold specific teleports for different patch types (e.g., Catherby Herb vs Fruit Tree)
    private final Map<String, Function<EasyFarmingConfig, EasyFarmingConfig.OptionEnumTeleport>> typeSpecificTeleports;

    private String overrideTeleportEnumOption;

    public Location(Function<EasyFarmingConfig, EasyFarmingConfig.OptionEnumTeleport> selectedTeleportFunction,
                    EasyFarmingConfig config, String name, Boolean farmLimps) {
        this.config = config;
        this.selectedTeleportFunction = selectedTeleportFunction;
        this.name = name;
        this.farmLimps = farmLimps;
        this.teleportOptions = new ArrayList<>();
        this.typeSpecificTeleports = new HashMap<>();
    }

    public void addPatch(List<Integer> ids) {
        // Placeholder so code compiles. Actual logic handled by overlays.
    }

    public void addTeleportOption(Teleport teleport) {
        teleportOptions.add(teleport);
    }

    // NEW: Bind a specific config getter to a specific patch type
    public void addTypeSpecificTeleport(String patchType, Function<EasyFarmingConfig, EasyFarmingConfig.OptionEnumTeleport> configFunction) {
        this.typeSpecificTeleports.put(patchType, configFunction);
    }

    // UPGRADED: Now accepts patchType to look up the specific config!
    public Teleport getSelectedTeleport(String patchType) {
        String configEnumOption = null;

        // 1. Custom Run Override Priority
        if (overrideTeleportEnumOption != null) {
            configEnumOption = overrideTeleportEnumOption;
        }
        // 2. Patch-Specific Config (e.g. Catherby Fruit Tree)
        else if (config != null && patchType != null && typeSpecificTeleports.containsKey(patchType)) {
            configEnumOption = typeSpecificTeleports.get(patchType).apply(config).name();
        }
        // 3. Default Config Fallback (e.g. Catherby Herb)
        else if (config != null && selectedTeleportFunction != null) {
            configEnumOption = selectedTeleportFunction.apply(config).name();
        }

        if (configEnumOption != null) {
            for (Teleport teleport : teleportOptions) {
                String opt = teleport.getEnumOption();
                if (opt != null && configEnumOption.equalsIgnoreCase(opt)) {
                    return teleport;
                }
            }
        }

        return teleportOptions.isEmpty() ? null : teleportOptions.get(0);
    }

    // Backward compatibility for generic calls
    public Teleport getSelectedTeleport() {
        return getSelectedTeleport(null);
    }

    public void setOverrideTeleportEnumOption(String enumOption) {
        this.overrideTeleportEnumOption = enumOption;
    }

    public String getOverrideTeleportEnumOption() {
        return overrideTeleportEnumOption;
    }

    public String getName() { return name; }
    public Boolean getFarmLimps() { return farmLimps; }
    public List<Teleport> getTeleportOptions() { return teleportOptions; }
}