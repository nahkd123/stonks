package stonks.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gl.GlDebug;

@Mixin(GlDebug.class)
public abstract class GlDebugMixin {
	@Inject(method = "onDebugMessage", at = @At("HEAD"), cancellable = true)
	private void disableDebugMixin(int source, int type, int id, int severity, int length, long message, long l, CallbackInfo ci) {
		ci.cancel();
	}
}
