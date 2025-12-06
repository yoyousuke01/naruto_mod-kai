
package net.narutomod.entity;

import net.narutomod.ElementsNarutomodMod;
import net.narutomod.NarutomodModVariables;
import net.narutomod.item.ItemAkatsukiRobe;
import net.narutomod.item.ItemAsuraCanon;
import net.narutomod.procedure.ProcedureSync;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;

import javax.annotation.Nullable;

@ElementsNarutomodMod.ModElement.Tag
public class EntityPainAsura extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 492;
	public static final int ENTITYID_RANGED = 493;

	public EntityPainAsura(ElementsNarutomodMod instance) {
		super(instance, 915);
	}

	@Override
	public void initElements() {
		elements.entities
				.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("narutomod", "pain_asura"), ENTITYID)
						.name("pain_asura").tracker(64, 3, true).egg(-16777216, -26368).build());
	}

	public static class EntityCustom extends EntityNinjaMob.Base implements IMob, IRangedAttackMob {
		private static final DataParameter<Boolean> TARGET_STATE = EntityDataManager.<Boolean>createKey(EntityCustom.class, DataSerializers.BOOLEAN);
		private final double missileChakraUsage = 30d;
		private int shootMissileTicks;
		
		public EntityCustom(World world) {
			super(world, 160, 7000d);
			this.setSize(0.6125f, 2.0f);
			this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 10, true, false, this.playerTargetSelectorAkatsuki));
		}

		@Override
		public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
			this.setCustomNameTag(net.minecraft.util.text.translation.I18n.translateToLocal("entity.pain.name"));
			this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ItemAkatsukiRobe.body));
			this.setItemToInventory(new ItemStack(ItemAsuraCanon.block), 0);
			return super.onInitialSpawn(difficulty, livingdata);
		}

		@Override
		protected void entityInit() {
			super.entityInit();
			this.dataManager.register(TARGET_STATE, Boolean.valueOf(false));
		}

		private void setTargetState(boolean state) {
			if (!this.world.isRemote) {
				this.dataManager.set(TARGET_STATE, Boolean.valueOf(state));
			}
		}

		private boolean getLastTargetState() {
			return ((Boolean)this.dataManager.get(TARGET_STATE)).booleanValue();
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(100D);
			this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(50.0D);
		}

		@Override
		protected double meleeReach() {
			return 4.4d;
		}

		@Override
		protected void initEntityAI() {
			super.initEntityAI();
			this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
			this.tasks.addTask(0, new EntityAISwimming(this));
			this.tasks.addTask(2, new EntityNinjaMob.AILeapAtTarget(this, 0.0F, 24.0F) {
				@Override
				public boolean shouldExecute() {
					return super.shouldExecute() && EntityCustom.this.getAttackTarget().posY - EntityCustom.this.posY > 5d;
				}
			});
			this.tasks.addTask(3, new EntityNinjaMob.AIAttackMelee(this, 1.2d, true) {
				@Override
				public boolean shouldExecute() {
					return super.shouldExecute() && EntityCustom.this.getDistance(EntityCustom.this.getAttackTarget()) < 6d;
				}
				@Override
				public boolean shouldContinueExecuting() {
					return super.shouldContinueExecuting() && EntityCustom.this.getDistance(EntityCustom.this.getAttackTarget()) < 6d;
				}
			});
			this.tasks.addTask(4, new EntityAIAttackRanged(this, 1.2d, 20, 20f));
			this.tasks.addTask(5, new EntityAIWatchClosest2(this, EntityPlayer.class, 32.0F, 1.0F));
			this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityNinjaMob.Base.class, 24.0F) {
				@Override
				public boolean shouldExecute() {
					return super.shouldExecute() && !this.entity.isOnSameTeam(this.closestEntity);
				}
			});
			this.tasks.addTask(7, new EntityAIWander(this, 0.5d));
			this.tasks.addTask(8, new EntityAILookIdle(this));
		}

		@Override
		protected void updateAITasks() {
			super.updateAITasks();
			boolean targetAlive = this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive();
			if (targetAlive) {
				if (this.shootMissileTicks > 0) {
					ProcedureSync.EntityNBTTag.setAndSync(EntityCustom.this, NarutomodModVariables.forceBowPose, true);
					ItemAsuraCanon.EntityMissile.shoot(this);
				} else {
					ProcedureSync.EntityNBTTag.removeAndSync(EntityCustom.this, NarutomodModVariables.forceBowPose);
				}
			} else {
				ProcedureSync.EntityNBTTag.removeAndSync(EntityCustom.this, NarutomodModVariables.forceBowPose);
			}
			if (targetAlive != this.getLastTargetState()) {
				this.swapWithInventory(EntityEquipmentSlot.CHEST, 1);
				this.swapWithInventory(EntityEquipmentSlot.MAINHAND, 0);
			}
			this.setTargetState(targetAlive);
			--this.shootMissileTicks;
		}

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			if (source == DamageSource.FALL) {
				return false;
			}
			return super.attackEntityFrom(source, amount);
		}

		@Override
		protected void damageEntity(DamageSource source, float amount) {
			amount *= source.isDamageAbsolute() ? 1.0f : source.isUnblockable() ? 0.4f + this.rand.nextFloat() * 0.4f : (0.1f + this.rand.nextFloat() * 0.1f);
			super.damageEntity(source, amount);
		}
		
		@Override
		public void setSwingingArms(boolean swingingArms) {
		}

		@Override
		public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
			if (this.consumeChakra(this.missileChakraUsage)) {
				this.shootMissileTicks = 3;
			}
		}
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		new Renderer().register();
	}

	public static class Renderer extends EntityRendererRegister {
		@SideOnly(Side.CLIENT)
		@Override
		public void register() {
			RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, renderManager -> new RenderCustom(renderManager));
		}

		@SideOnly(Side.CLIENT)
		public class RenderCustom extends EntityNinjaMob.RenderBase<EntityCustom> {
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/pain_asura.png");

			public RenderCustom(RenderManager renderManagerIn) {
				super(renderManagerIn, new ModelPainAsura());
			}

			@Override
			protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
				float f = 0.0625f * 16f;
				GlStateManager.scale(f, f, f);
				boolean clothesOff = entity.getLastTargetState();
				ModelPainAsura model = (ModelPainAsura)this.getMainModel();
				model.face2.showModel = clothesOff;
				model.face3.showModel = clothesOff;
				model.tail.showModel = clothesOff;
				model.bipedRightArm1.showModel = clothesOff;
				model.bipedRightArm2.showModel = clothesOff;
				model.bipedLeftArm1.showModel = clothesOff;
				model.bipedLeftArm2.showModel = clothesOff;
			}

			@Override
			public void transformHeldFull3DItemLayer() {
				GlStateManager.translate(0.0F, 0.1875F, 0.0F);
			}

			@Override
			protected ResourceLocation getEntityTexture(EntityCustom entity) {
				return this.texture;
			}
		}

		// Made with Blockbench 3.7.4
		// Exported for Minecraft version 1.12
		// Paste this class into your mod and generate all required imports
		@SideOnly(Side.CLIENT)
		public class ModelPainAsura extends EntityNinjaMob.ModelNinja {
			//private final ModelRenderer bipedHead;
			private final ModelRenderer eyes;
			private final ModelRenderer face2;
			private final ModelRenderer eyes2;
			private final ModelRenderer face3;
			private final ModelRenderer eyes3;
			//private final ModelRenderer bipedHeadwear;
			private final ModelRenderer spikes;
			//private final ModelRenderer bipedBody;
			private final ModelRenderer tail;
			private final ModelRenderer tail2;
			private final ModelRenderer tail3;
			private final ModelRenderer tail4;
			private final ModelRenderer tail5;
			private final ModelRenderer tail6;
			//private final ModelRenderer bipedRightArm;
			private final ModelRenderer bipedRightArm1;
			private final ModelRenderer bipedRightArm2;
			//private final ModelRenderer bipedLeftArm;
			private final ModelRenderer bipedLeftArm1;
			private final ModelRenderer bipedLeftArm2;
			//private final ModelRenderer bipedRightLeg;
			//private final ModelRenderer bipedLeftLeg;

			public ModelPainAsura() {
				textureWidth = 64;
				textureHeight = 64;

				bipedHead = new ModelRenderer(this);
				bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHead.cubeList.add(new ModelBox(bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 9, 8, -0.5F, false));
		
				eyes = new ModelRenderer(this);
				eyes.setRotationPoint(0.0F, -3.0F, 0.0F);
				bipedHead.addChild(eyes);
				eyes.cubeList.add(new ModelBox(eyes, 40, 52, -7.7F, -5.95F, -8.55F, 12, 12, 0, -5.0F, false));
				eyes.cubeList.add(new ModelBox(eyes, 40, 52, -4.3F, -5.95F, -8.55F, 12, 12, 0, -5.0F, true));
		
				face2 = new ModelRenderer(this);
				face2.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHead.addChild(face2);
				setRotationAngle(face2, 0.0F, 1.5708F, 0.0F);
				face2.cubeList.add(new ModelBox(face2, 0, 0, -4.0F, -8.0F, -4.01F, 8, 9, 8, -0.505F, false));
		
				eyes2 = new ModelRenderer(this);
				eyes2.setRotationPoint(0.0F, -3.0F, 0.0F);
				face2.addChild(eyes2);
				eyes2.cubeList.add(new ModelBox(eyes2, 40, 52, -7.7F, -5.95F, -8.55F, 12, 12, 0, -5.0F, false));
				eyes2.cubeList.add(new ModelBox(eyes2, 40, 52, -4.3F, -5.95F, -8.55F, 12, 12, 0, -5.0F, true));
		
				face3 = new ModelRenderer(this);
				face3.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHead.addChild(face3);
				setRotationAngle(face3, 0.0F, -1.5708F, 0.0F);
				face3.cubeList.add(new ModelBox(face3, 0, 0, -4.0F, -8.0F, -4.01F, 8, 9, 8, -0.505F, false));
		
				eyes3 = new ModelRenderer(this);
				eyes3.setRotationPoint(0.0F, -3.0F, 0.0F);
				face3.addChild(eyes3);
				eyes3.cubeList.add(new ModelBox(eyes3, 40, 52, -7.7F, -5.95F, -8.55F, 12, 12, 0, -5.0F, false));
				eyes3.cubeList.add(new ModelBox(eyes3, 40, 52, -4.3F, -5.95F, -8.55F, 12, 12, 0, -5.0F, true));
		
				bipedHeadwear = new ModelRenderer(this);
				bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHeadwear.cubeList.add(new ModelBox(bipedHeadwear, 32, 0, -4.0F, -8.0F, -4.0F, 8, 9, 8, -0.35F, false));
				bipedHeadwear.cubeList.add(new ModelBox(bipedHeadwear, 0, 52, -6.0F, -11.7F, -7.4F, 12, 12, 0, -3.7F, false));
		
				spikes = new ModelRenderer(this);
				spikes.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHeadwear.addChild(spikes);
				spikes.cubeList.add(new ModelBox(spikes, 0, 0, -3.1F, -8.25F, -0.9F, 1, 1, 1, -0.2F, false));
				spikes.cubeList.add(new ModelBox(spikes, 0, 0, 1.9F, -8.25F, -0.9F, 1, 1, 1, -0.2F, false));
				spikes.cubeList.add(new ModelBox(spikes, 0, 0, -0.5F, -8.25F, -2.9F, 1, 1, 1, -0.2F, false));
				spikes.cubeList.add(new ModelBox(spikes, 0, 0, 1.0F, -8.25F, 1.8F, 1, 1, 1, -0.2F, false));
				spikes.cubeList.add(new ModelBox(spikes, 0, 0, -2.0F, -8.25F, 1.8F, 1, 1, 1, -0.2F, false));
				spikes.cubeList.add(new ModelBox(spikes, 0, 0, -0.5F, -0.55F, -4.2F, 1, 1, 1, -0.1F, false));
				spikes.cubeList.add(new ModelBox(spikes, 0, 0, -3.125F, -2.875F, -4.1F, 1, 1, 1, -0.35F, false));
				spikes.cubeList.add(new ModelBox(spikes, 0, 0, 2.125F, -2.875F, -4.1F, 1, 1, 1, -0.35F, true));
				spikes.cubeList.add(new ModelBox(spikes, 0, 0, -3.125F, -2.225F, -4.1F, 1, 1, 1, -0.35F, false));
				spikes.cubeList.add(new ModelBox(spikes, 0, 0, 2.125F, -2.225F, -4.1F, 1, 1, 1, -0.35F, true));
		
				bipedBody = new ModelRenderer(this);
				bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 18, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F, false));
				bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 34, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.2F, false));
		
				tail = new ModelRenderer(this);
				tail.setRotationPoint(0.0F, 7.0F, 2.0F);
				bipedBody.addChild(tail);
				setRotationAngle(tail, -0.7854F, 0.0F, 0.0F);
				tail.cubeList.add(new ModelBox(tail, 24, 0, -3.0F, -8.0F, 0.0F, 6, 8, 0, 0.0F, false));
		
				tail2 = new ModelRenderer(this);
				tail2.setRotationPoint(0.0F, -8.0F, 0.0F);
				tail.addChild(tail2);
				setRotationAngle(tail2, 0.5236F, 0.0F, 0.0F);
				tail2.cubeList.add(new ModelBox(tail2, 24, 0, -3.0F, -8.0F, 0.0F, 6, 8, 0, 0.0F, false));
		
				tail3 = new ModelRenderer(this);
				tail3.setRotationPoint(0.0F, -8.0F, 0.0F);
				tail2.addChild(tail3);
				setRotationAngle(tail3, 0.7854F, 0.0F, 0.0F);
				tail3.cubeList.add(new ModelBox(tail3, 24, 0, -3.0F, -8.0F, 0.0F, 6, 8, 0, 0.0F, false));
		
				tail4 = new ModelRenderer(this);
				tail4.setRotationPoint(0.0F, -8.0F, 0.0F);
				tail3.addChild(tail4);
				setRotationAngle(tail4, 0.7854F, 0.0F, 0.0F);
				tail4.cubeList.add(new ModelBox(tail4, 24, 0, -3.0F, -8.0F, 0.0F, 6, 8, 0, 0.0F, false));
		
				tail5 = new ModelRenderer(this);
				tail5.setRotationPoint(0.0F, -8.0F, 0.0F);
				tail4.addChild(tail5);
				setRotationAngle(tail5, 0.5236F, 0.0F, 0.0F);
				tail5.cubeList.add(new ModelBox(tail5, 24, 0, -3.0F, -8.0F, 0.0F, 6, 8, 0, 0.0F, false));
		
				tail6 = new ModelRenderer(this);
				tail6.setRotationPoint(0.0F, -8.0F, 0.0F);
				tail5.addChild(tail6);
				setRotationAngle(tail6, 0.5236F, 0.0F, 0.0F);
				tail6.cubeList.add(new ModelBox(tail6, 26, 56, -3.0F, -8.0F, 0.0F, 6, 8, 0, 0.0F, false));
		
				bipedRightArm1 = new ModelRenderer(this);
				bipedRightArm1.setRotationPoint(-5.0F, 2.0F, 1.0F);
				bipedBody.addChild(bipedRightArm1);
				setRotationAngle(bipedRightArm1, 0.0F, 0.5236F, 0.7854F);
				bipedRightArm1.cubeList.add(new ModelBox(bipedRightArm1, 40, 18, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, false));
				bipedRightArm1.cubeList.add(new ModelBox(bipedRightArm1, 40, 34, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.2F, false));
		
				bipedRightArm2 = new ModelRenderer(this);
				bipedRightArm2.setRotationPoint(-5.0F, 2.0F, 2.0F);
				bipedBody.addChild(bipedRightArm2);
				setRotationAngle(bipedRightArm2, 1.0472F, 0.5236F, 1.5708F);
				bipedRightArm2.cubeList.add(new ModelBox(bipedRightArm2, 40, 18, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, false));
				bipedRightArm2.cubeList.add(new ModelBox(bipedRightArm2, 40, 34, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.2F, false));
		
				bipedLeftArm1 = new ModelRenderer(this);
				bipedLeftArm1.setRotationPoint(5.0F, 2.0F, 1.0F);
				bipedBody.addChild(bipedLeftArm1);
				setRotationAngle(bipedLeftArm1, 0.3927F, -0.5236F, -0.7854F);
				bipedLeftArm1.cubeList.add(new ModelBox(bipedLeftArm1, 40, 18, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, true));
				bipedLeftArm1.cubeList.add(new ModelBox(bipedLeftArm1, 40, 34, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.2F, true));
		
				bipedLeftArm2 = new ModelRenderer(this);
				bipedLeftArm2.setRotationPoint(5.0F, 2.0F, 2.0F);
				bipedBody.addChild(bipedLeftArm2);
				setRotationAngle(bipedLeftArm2, 1.0472F, -0.5236F, -1.5708F);
				bipedLeftArm2.cubeList.add(new ModelBox(bipedLeftArm2, 40, 18, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, true));
				bipedLeftArm2.cubeList.add(new ModelBox(bipedLeftArm2, 40, 34, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.2F, true));

				bipedRightArm = new ModelRenderer(this);
				bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
				setRotationAngle(bipedRightArm, -0.3927F, 0.0F, 0.0F);
				bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 18, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, false));
				bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 34, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.2F, false));
				
				bipedLeftArm = new ModelRenderer(this);
				bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
				bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 18, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, true));
				bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 34, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.2F, true));
				
				bipedRightLeg = new ModelRenderer(this);
				bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
				setRotationAngle(bipedRightLeg, 0.3927F, 0.0F, 0.0F);
				bipedRightLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 18, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, false));
				bipedRightLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 34, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F, false));
		
				bipedLeftLeg = new ModelRenderer(this);
				bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
				setRotationAngle(bipedLeftLeg, -0.3927F, 0.0F, 0.0F);
				bipedLeftLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 18, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, true));
				bipedLeftLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 34, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F, true));
			}

			@Override
			public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
				super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		        this.bipedRightArm1.rotateAngleX = 0.3927F + MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		        this.bipedLeftArm1.rotateAngleX = 0.3927F + MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		        this.bipedRightArm1.rotateAngleZ = 0.7854F;
		        this.bipedLeftArm1.rotateAngleZ = -0.7854F;
		        this.bipedRightArm1.rotateAngleY = 0.5236F;
		        this.bipedLeftArm1.rotateAngleY = -0.5236F;
		        this.bipedRightArm2.rotateAngleX = 1.0472F + MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		        this.bipedLeftArm2.rotateAngleX = 1.0472F + MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		        this.bipedRightArm2.rotateAngleZ = 1.5708F;
		        this.bipedLeftArm2.rotateAngleZ = -1.5708F;
		        this.bipedRightArm2.rotateAngleY = 0.5236F;
		        this.bipedLeftArm2.rotateAngleY = -0.5236F;
		        if (this.isSneak) {
					this.bipedRightArm1.rotateAngleX = 0.3927F + 1.8835F;
					this.bipedRightArm1.rotateAngleY += -0.3927F;
					this.bipedLeftArm1.rotateAngleX = 0.3927F + 1.8835F;
					this.bipedLeftArm1.rotateAngleY += 0.3927F;
					this.bipedRightArm2.rotateAngleX = 1.0472F + 1.8835F;
					this.bipedRightArm2.rotateAngleY += -0.3927F;
					this.bipedLeftArm2.rotateAngleX = 1.0472F + 1.8835F;
					this.bipedLeftArm2.rotateAngleY += 0.3927F;
		        } else {
			        this.bipedRightArm1.rotateAngleZ += MathHelper.cos((ageInTicks + 20) * 0.09F) * 0.05F + 0.05F;
			        this.bipedLeftArm1.rotateAngleZ -= MathHelper.cos((ageInTicks + 20) * 0.09F) * 0.05F + 0.05F;
			        this.bipedRightArm1.rotateAngleX += MathHelper.sin((ageInTicks + 20) * 0.067F) * 0.05F;
			        this.bipedLeftArm1.rotateAngleX -= MathHelper.sin((ageInTicks + 20) * 0.067F) * 0.05F;
			        this.bipedRightArm2.rotateAngleZ += MathHelper.cos((ageInTicks + 40) * 0.09F) * 0.05F + 0.05F;
			        this.bipedLeftArm2.rotateAngleZ -= MathHelper.cos((ageInTicks + 40) * 0.09F) * 0.05F + 0.05F;
			        this.bipedRightArm2.rotateAngleX += MathHelper.sin((ageInTicks + 40) * 0.067F) * 0.05F;
			        this.bipedLeftArm2.rotateAngleX -= MathHelper.sin((ageInTicks + 40) * 0.067F) * 0.05F;
		        }
			}
		}
	}
}
