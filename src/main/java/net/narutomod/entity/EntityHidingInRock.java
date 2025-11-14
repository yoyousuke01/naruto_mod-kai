
package net.narutomod.entity;

import net.narutomod.ElementsNarutomodMod;
import net.narutomod.Chakra;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemDoton;
import net.narutomod.PlayerTracker;
import net.narutomod.procedure.ProcedureOnLivingUpdate;

import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.block.material.Material;

@ElementsNarutomodMod.ModElement.Tag
public class EntityHidingInRock extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 516;
	public static final int ENTITYID_RANGED = 517;

	public EntityHidingInRock(ElementsNarutomodMod instance) {
		super(instance, 934);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class)
				.id(new ResourceLocation("narutomod", "hiding_in_rock"), ENTITYID).name("hiding_in_rock").tracker(64, 3, true).build());
	}

	public static class EC extends Entity implements ItemJutsu.IJutsu {
		private final int waitTime = 60;
		private EntityLivingBase user;

		public EC(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
		}

		public EC(EntityLivingBase userIn) {
			this(userIn.world);
			this.user = userIn;
			this.setPosition(userIn.posX, userIn.posY, userIn.posZ);
		}
		
		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.DOTON;
		}

		@Override
		protected void entityInit() {
		}

		@Override
		public void setDead() {
			super.setDead();
			if (this.user != null) {
				this.user.getEntityData().removeTag(Jutsu.ID_KEY);
			}
			if (this.isUserIntangible()) {
				this.setUserIntangible(false);
			}
		}

		private boolean isUserIntangible() {
			return this.user != null && ProcedureOnLivingUpdate.isNoClip(this.user);
		}

		private void setUserIntangible(boolean intangibleIn) {
			if (this.isUserIntangible() != intangibleIn) {
				ProcedureOnLivingUpdate.setNoClip(this.user, intangibleIn);
			}
			if (this.user instanceof EntityPlayer && !this.world.isRemote) {
				String string = net.minecraft.util.text.translation.I18n.translateToLocal("chattext.intangible");
				((EntityPlayer)this.user).sendStatusMessage(new TextComponentString(string + intangibleIn), true);
			}
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			if (this.user != null && this.user.isEntityAlive()) {
				this.setPosition(this.user.posX, this.user.posY, this.user.posZ);
				BlockPos[] pos = { 
					new BlockPos(this.user.posX, this.user.posY, this.user.posZ + this.user.width * 0.5),
					new BlockPos(this.user.posX, this.user.posY, this.user.posZ - this.user.width * 0.5),
					new BlockPos(this.user.posX + this.user.width * 0.5, this.user.posY, this.user.posZ),
					new BlockPos(this.user.posX - this.user.width * 0.5, this.user.posY, this.user.posZ),
					new BlockPos(this.user.posX, this.user.posY + 1.5, this.user.posZ + this.user.width * 0.5),
					new BlockPos(this.user.posX, this.user.posY + 1.5, this.user.posZ - this.user.width * 0.5),
					new BlockPos(this.user.posX + this.user.width * 0.5, this.user.posY + 1.5, this.user.posZ),
					new BlockPos(this.user.posX - this.user.width * 0.5, this.user.posY + 1.5, this.user.posZ)
				};
				Material[] material = new Material[pos.length];
				boolean[] inEarth = new boolean[pos.length];
				for (int i = 0; i < pos.length; i++) {
					material[i] = this.world.getBlockState(pos[i]).getMaterial();
					inEarth[i] = ItemDoton.isEarthenMaterial(material[i]);
				}
				if (this.ticksExisted > this.waitTime) {
					byte b = 0;
					for (int i = 0; i < pos.length; i++) {
						b |= inEarth[i] ? 1 : 0; 
					}
					if (b == 0) {
						this.setDead();
					}
				} else {
					for (int i = 0; i < pos.length; i++) {
						if (material[i].isSolid() && !inEarth[i]) {
							this.setDead();
						}
					}
				}
				if (!this.isDead) {
					if (this.ticksExisted % 20 == 1 && !Chakra.pathway(this.user).consume(ItemDoton.HIDINGINROCK.chakraUsage)) {
						this.setDead();
					} else {
						this.setUserIntangible(true);
					}
				}
			}
		}

		@Override
		protected void readEntityFromNBT(NBTTagCompound compound) {
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound compound) {
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			private static final String ID_KEY = "HidingInRockIdKey";
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				if (!PlayerTracker.noKamuiAndHidingInRock(entity.world) && !ProcedureOnLivingUpdate.isNoClip(entity)) {
					entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvent.REGISTRY
					 .getObject(new ResourceLocation("narutomod:jutsu")), SoundCategory.NEUTRAL, 1, 1f);
					entity.world.spawnEntity(new EC(entity));
					entity.getEntityData().setBoolean(ID_KEY, true);
					return true;
				}
				return false;
			}

			@Override
			public boolean isActivated(EntityLivingBase entity) {
				return entity.getEntityData().getBoolean(ID_KEY);
			}

			@Override
			public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
			}
		}
	}
}
