
package net.narutomod.potion;

//import net.narutomod.procedure.ProcedureAmaterasuFlameOnPotionActiveTick;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.item.ItemSharingan;
import net.narutomod.Particles;
import net.narutomod.procedure.ProcedureUtils;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nullable;

@ElementsNarutomodMod.ModElement.Tag
public class PotionAmaterasuFlame extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:amaterasuflame")
	public static final Potion potion = null;

	public PotionAmaterasuFlame(ElementsNarutomodMod instance) {
		super(instance, 175);
	}

	@Override
	public void initElements() {
		elements.potions.add(() -> new PotionCustom());
	}

	public static class PotionCustom extends Potion {
		private final ResourceLocation potionIcon;
		public PotionCustom() {
			super(true, -16777216);
			setRegistryName("amaterasuflame");
			setPotionName("effect.amaterasuflame");
			potionIcon = new ResourceLocation("narutomod:textures/mob_effect/amaterasuflame.png");
		}

		@Override
		public boolean isInstant() {
			return true;
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
			ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
			if (stack.getItem() instanceof ItemSharingan.Base && (((ItemSharingan.Base)stack.getItem()).getSubType() == ItemSharingan.Type.AMATERASU
					|| ((ItemSharingan.Base)stack.getItem()).isEternal())) {
				entity.removePotionEffect(PotionAmaterasuFlame.potion);
				entity.extinguish();
			} else {
				if (source != null) {
					entity.getEntityData().setBoolean("TempData_disableKnockback", true);
					entity.attackEntityFrom(new ProcedureUtils.JutsuEffectDamageSource(potion).setCaster(source), (float) (amplifier + 1));
				} else {
					entity.attackEntityFrom(ProcedureUtils.AMATERASU, (float) (amplifier + 1));
				}
				Particles.spawnParticle(entity.world, Particles.Types.FLAME, entity.posX, entity.posY + entity.height * 0.5f, entity.posZ,
				 amplifier + 1, entity.width * 0.25, entity.height * 0.2, entity.width * 0.25, 0d, 0d, 0d, 0xA0000000, 20);
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

		@Override
		public java.util.List<net.minecraft.item.ItemStack> getCurativeItems() {
			return new java.util.ArrayList<net.minecraft.item.ItemStack>();
		}
	}
}
