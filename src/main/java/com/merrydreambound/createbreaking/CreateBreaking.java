package com.merrydreambound.createbreaking;

import com.merrydreambound.createbreaking.config.CreateBreakingConfig;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateBreaking.MODID)
public class CreateBreaking {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "createbreaking";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static CreateBreakingConfig CONFIG = ConfigApiJava.registerAndLoadConfig(CreateBreakingConfig::new);
}

