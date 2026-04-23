package com.merrydreambound.createbreaking.config;

import com.merrydreambound.createbreaking.CreateBreaking;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.api.SaveType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import net.minecraft.resources.ResourceLocation;
import org.intellij.lang.annotations.Identifier;

public class CreateBreakingConfig extends Config {

    public CreateBreakingConfig() {
        super(ResourceLocation.fromNamespaceAndPath(CreateBreaking.MODID,"config"));
    }

    public ValidatedDouble validatedDouble = new ValidatedDouble(5.0, 10.0, 0.0); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.

    public MySection mySection = new MySection(); // a section of the config with its own validated fields and other sections as applicable. This will appear in-game as a separate screen "layer" with a breadcrumb leading back to the parent screen.
//
    public static class MySection extends ConfigSection { // a Config Section. Self-serializable. Of course it doesn't have to be defined inside of it's parent class, but it may be convenient
        public MySection() {
            super();
        }
        public ValidatedDouble validatedDouble = new ValidatedDouble(5.0, 10.0, 0.0); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.
    }

    //Configs have a default permission level needed to edit them (disabled in single player). You can override that default here
    @Override
    public int defaultPermLevel() {
        return 4;
    }

    //Fzzy Config uses TOML files by default. You can override that behavior to any of the supported FileType
    @Override
    public FileType fileType() {
        return FileType.JSON5;
    }

    //You can define the save type for your config; which determines how clients act when receiving updates from a server.
    //SaveType.SEPARATE will not save updates to the local config files, keeping them separate for singleplayer play.
    @Override
    public SaveType saveType() {
        return SaveType.SEPARATE;
    }
}