package com.merrydreambound.createbreaking.config;

import com.merrydreambound.createbreaking.CreateBreaking;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import net.minecraft.resources.ResourceLocation;

public class CreateBreakingConfig extends Config {

    public CreateBreakingConfig() {
        super(ResourceLocation.fromNamespaceAndPath(CreateBreaking.MODID,"config"));
    }
    public ValidatedDouble PenetrationCost = new ValidatedDouble(8, 256, 0.125, ValidatedNumber.WidgetType.TEXTBOX); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.
    public ValidatedDouble SpeedCost = new ValidatedDouble(64, 256, 0.125, ValidatedNumber.WidgetType.TEXTBOX); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.


//
//    @Override
//    public SaveType saveType() {
//        return SaveType.SEPARATE;
//    }
}