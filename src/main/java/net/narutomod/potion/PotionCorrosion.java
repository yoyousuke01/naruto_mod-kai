
package net.narutomod.potion;

//import net.narutomod.procedure.ProcedureCorrosionOnPotionActiveTick;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.Particles;
import net.narutomod.procedure.ProcedureUtils;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;

import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nullable;

@ElementsNarutomodMod.ModElement.Tag
public class PotionCorrosion extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:corrosion")
	public static final Potion potion = null;

	public PotionCorrosion(ElementsNarutomodMod instance) {
		super(instance, 601);
	}

	@Override
	public void initElements() {
		elements.potions.add(() -> new PotionCustom());
	}

	public static class PotionCustom extends Potion {
		private final ResourceLocation potionIcon;
		public PotionCustom() {
			super(true, -3355444);
			setRegistryName("corrosion");
			setPotionName("effect.corrosion");
			potionIcon = new ResourceLocation("narutomod:textures/mob_effect/corrosion.png");
		}

		@Override
		public boolean isInstant() {
			return false;
		}

		@Override
		public boolean shouldRenderInvText(PotionEffect effect) {
			return true;
		}

		@Override
		public boolean shouldRenderHUD(PotionEffect effect) {
			return true;
		}

		@Override
		public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, EntityLivingBase entity, int amplifier, double health) {
			if (source != null) {
				entity.getEntityData().setBoolean("TempData_disableKnockback", true);
				entity.attackEntityFrom(new ProcedureUtils.JutsuEffectDamageSource(potion).setCaster(source), (float) (amplifier + 1));
			} else {
				entity.attackEntityFrom(ProcedureUtils.CORROSION, (float) amplifier + 1f);
			}
			Particles.spawnParticle(entity.world, Particles.Types.SMOKE, entity.posX, entity.posY + entity.height * 0.5, entity.posZ, 10, entity.width * 0.3,
					entity.height * 0.3, entity.width * 0.3, 0d, 0d, 0d, 0x20FFFFFF);
			if (entity.getRNG().nextFloat() <= 0.5f) {
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH,
						SoundCategory.NEUTRAL, 0.8f, entity.getRNG().nextFloat() * 0.6f + 0.6f);
			}
		}

		@SideOnly(Side.CLIENT)
		@Override
		public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
			if (mc.currentScreen != null) {
				mc.getTextureManager().bindTexture(potionIcon);
				Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
			}
		}

		@SideOnly(Side.CLIENT)
		@Override
		public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
			mc.getTextureManager().bindTexture(potionIcon);
			Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0, 0, 18, 18, 18, 18);
		}

		@Override
		public boolean isReady(int duration, int amplifier) {
			return true;
		}
	}
}
