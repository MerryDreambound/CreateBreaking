package com.merrydreambound.createbreaking.config;

import com.merrydreambound.createbreaking.CreateBreaking;
import me.fzzyhmstrs.fzzy_config.api.SaveType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import net.minecraft.resources.ResourceLocation;

public class CreateBreakingConfig extends Config {

    public CreateBreakingConfig() {
        super(ResourceLocation.fromNamespaceAndPath(CreateBreaking.MODID,"config"));
    }

//    @RequiresAction(action = Action.RESTART)
    public ValidatedDouble Weightless_TriggerSpeed = new ValidatedDouble(0.125, 256, 0.125, ValidatedNumber.WidgetType.TEXTBOX); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.
//    @RequiresAction(action = Action.RESTART)
    public ValidatedDouble Super_Light_TriggerSpeed = new ValidatedDouble(0.25, 256, 0.125, ValidatedNumber.WidgetType.TEXTBOX); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.
//    @RequiresAction(action = Action.RESTART)
    public ValidatedDouble Light_TriggerSpeed = new ValidatedDouble(0.5, 256, 0.125, ValidatedNumber.WidgetType.TEXTBOX); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.
//    @RequiresAction(action = Action.RESTART)
    public ValidatedDouble Default_TriggerSpeed = new ValidatedDouble(1, 256, 0.125, ValidatedNumber.WidgetType.TEXTBOX); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.
//    @RequiresAction(action = Action.RESTART)
    public ValidatedDouble Heavy_TriggerSpeed = new ValidatedDouble(2, 256, 0.125, ValidatedNumber.WidgetType.TEXTBOX); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.
//    @RequiresAction(action = Action.RESTART)
    public ValidatedDouble Super_Heavy_TriggerSpeed = new ValidatedDouble(4, 256, 0.125, ValidatedNumber.WidgetType.TEXTBOX); //this field has defined validation, error correction, and will restrict user inputs to doubles between 0 and 10.

    @Override
    public SaveType saveType() {
        return SaveType.SEPARATE;
    }
}