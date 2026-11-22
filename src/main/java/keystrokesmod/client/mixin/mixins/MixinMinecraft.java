package keystrokesmod.client.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import keystrokesmod.client.event.impl.GameLoopEvent;
import keystrokesmod.client.main.Raven;
import keystrokesmod.client.ui.hud.ClickStats;
import net.minecraft.client.Minecraft;

@Mixin(priority = 1005, value = Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "runTick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        Raven.eventBus.post(new GameLoopEvent());
    }

    /** The game's own left click: counted for the CPS widget and keystrokes (real output, not input). */
    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void pablo$countLeftClick(CallbackInfo ci) {
        ClickStats.leftClick();
    }

    /** The game's own right click. */
    @Inject(method = "rightClickMouse", at = @At("HEAD"))
    private void pablo$countRightClick(CallbackInfo ci) {
        ClickStats.rightClick();
    }

}
