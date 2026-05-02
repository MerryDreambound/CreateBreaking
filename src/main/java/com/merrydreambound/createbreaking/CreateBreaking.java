package com.merrydreambound.createbreaking;

import com.merrydreambound.createbreaking.config.CreateBreakingConfig;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;

@Mod(CreateBreaking.MODID)
public class CreateBreaking {
    public static final String MODID = "createbreaking";
    public static CreateBreakingConfig CONFIG = ConfigApiJava.registerAndLoadConfig(CreateBreakingConfig::new);
}

