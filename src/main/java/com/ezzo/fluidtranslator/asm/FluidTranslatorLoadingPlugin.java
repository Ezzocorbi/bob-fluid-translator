package com.ezzo.fluidtranslator.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.Name("BobFluidTranslatorCore")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class FluidTranslatorLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"com.ezzo.fluidtranslator.asm.UniversalFluidTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // Nothing to inject.
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
