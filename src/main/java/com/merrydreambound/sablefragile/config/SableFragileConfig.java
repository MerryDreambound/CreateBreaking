package com.merrydreambound.sablefragile.config;

import com.merrydreambound.sablefragile.SableFragile;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import net.minecraft.resources.ResourceLocation;

public class SableFragileConfig extends Config {

    public SableFragileConfig() {
        super(ResourceLocation.fromNamespaceAndPath(SableFragile.MODID,"config"));
    }
    public ValidatedDouble PenetrationCost = new ValidatedDouble(256, 1024, 1, ValidatedNumber.WidgetType.TEXTBOX); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.
    public ValidatedDouble SpeedCost = new ValidatedDouble(1000, 25000, 1, ValidatedNumber.WidgetType.TEXTBOX); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.
    public ValidatedDouble MinHeight = new ValidatedDouble(1.1, 256, 0.1, ValidatedNumber.WidgetType.TEXTBOX); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.
    public ValidatedBoolean ExtraFragileWorld = new ValidatedBoolean(false);

}