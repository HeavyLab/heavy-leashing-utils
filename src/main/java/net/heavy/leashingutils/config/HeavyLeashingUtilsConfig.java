package net.heavy.leashingutils.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.util.TranslatableOption;

@Config(name = "heavy-leashing-utils")
public class HeavyLeashingUtilsConfig implements ConfigData {
    public boolean displayAttachedStatusIcon = true;

    public boolean displayAttachedStatusCount = true;

    public boolean playDetachSound = true;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public IconHudPosition iconHudPosition = IconHudPosition.CENTER_RIGHT;

    @ConfigEntry.BoundedDiscrete(min = -100, max = 100)
    public int iconHudXOffset = 0;

    @ConfigEntry.BoundedDiscrete(min = -100, max = 100)
    public int iconHudYOffset = 0;
}
