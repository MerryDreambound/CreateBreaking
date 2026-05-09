package com.merrydreambound.sablefragile;

import com.merrydreambound.sablefragile.config.SableFragileConfig;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;

import net.neoforged.fml.common.Mod;

@Mod(SableFragile.MODID)
public class SableFragile {
    public static final String MODID = "sablefragile";
    public static SableFragileConfig CONFIG = ConfigApiJava.registerAndLoadConfig(SableFragileConfig::new);
}

