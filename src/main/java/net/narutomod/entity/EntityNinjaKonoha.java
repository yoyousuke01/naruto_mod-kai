
package net.narutomod.entity;

import net.narutomod.ElementsNarutomodMod;
import net.narutomod.item.ItemNinjaArmorKonoha;
import net.narutomod.item.ItemKunai;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;

import javax.annotation.Nullable;

@ElementsNarutomodMod.ModElement.Tag
public class EntityNinjaKonoha extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 309;
	public static final int ENTITYID_RANGED = 310;
	
	public EntityNinjaKonoha(ElementsNarutomodMod instance) {
		super(instance, 627);
	}

	@Override
	public void initElements() {
		elements.entities
				.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("narutomod", "ninja_konoha"), ENTITYID)
						.name("ninja_konoha").tracker(64, 3, true).egg(-16751053, -16764058).build());
	}

	public static class EntityCustom extends EntityNinjaMob.Base {
		public EntityCustom(World world) {
			super(world, 120, 7000d);
			this.setSize(0.6f, 1.8f);
			this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 10, true, false, this.playerTargetSelector));
		}

		@Override
		public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
			this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ItemNinjaArmorKonoha.helmet));
			this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ItemNinjaArmorKonoha.body));
			this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ItemNinjaArmorKonoha.legs));
			this.setItemToInventory(new ItemStack(ItemKunai.block), 0);
			return super.onInitialSpawn(difficulty, livingdata);
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
		}

		@Override
		protected void initEntityAI() {
			super.initEntityAI();
			this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
			this.tasks.addTask(0, new EntityAISwimming(this));
			this.tasks.addTask(2, new EntityNinjaMob.AILeapAtTarget(this, 1.0F));
			this.tasks.addTask(4, new EntityNinjaMob.AIAttackMelee(this, 1.2d, true));
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
			//if (this.ticksExisted == 40) {
			//	this.wingsEntity = EntityShikigami.EC.Jutsu.createJutsu(this);
			//}
		}

		@Override
		public boolean getCanSpawnHere() {
			return super.getCanSpawnHere() && (int)this.posY >= this.world.getSeaLevel() && this.world.canSeeSky(this.getPosition());
			 //&& this.world.getEntities(EntityCustom.class, EntitySelectors.IS_ALIVE).isEmpty();
			 //&& !EntityNinjaMob.SpawnData.spawnedRecentlyHere(this, 36000);
			 //&& this.rand.nextInt(5) == 0;
		}

		//@Override
		//public boolean isOnSameTeam(Entity entityIn) {
		//	return super.isOnSameTeam(entityIn) || EntityNinjaMob.TeamAkatsuki.contains(entityIn.getClass());
		//}
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
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/ninja_konoha.png");

			public RenderCustom(RenderManager renderManagerIn) {
				super(renderManagerIn, new ModelKonohaNinja());
			}

			@Override
			protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
				float f = 0.0625f * 15;
				GlStateManager.scale(f, f, f);
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

		@SideOnly(Side.CLIENT)
		public class ModelKonohaNinja extends EntityNinjaMob.ModelNinja {
			public ModelKonohaNinja() {
				super();

				bipedHead = new ModelRenderer(this);
				bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHead.cubeList.add(new ModelBox(bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, false));
		
				bipedHeadwear = new ModelRenderer(this);
				bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHeadwear.cubeList.add(new ModelBox(bipedHeadwear, 32, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.25F, false));
		
				bipedBody = new ModelRenderer(this);
				bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F, false));
				bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 32, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.25F, false));
		
				bipedRightArm = new ModelRenderer(this);
				bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
				bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 16, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, false));
				bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 32, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.2F, false));
		
				bipedLeftArm = new ModelRenderer(this);
				bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
				bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 16, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, true));
				bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 32, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.2F, true));
		
				bipedRightLeg = new ModelRenderer(this);
				bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
				bipedRightLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, false));
				bipedRightLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 32, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F, false));
				
				bipedLeftLeg = new ModelRenderer(this);
				bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
				bipedLeftLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, true));				
				bipedLeftLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 48, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F, true));
			}

		}
	}
}
