
package net.narutomod.entity;

import net.narutomod.ElementsNarutomodMod;
import net.narutomod.item.*;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import net.minecraft.world.World;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;

import java.util.Map;
import javax.annotation.Nullable;
import com.google.common.collect.Maps;

@ElementsNarutomodMod.ModElement.Tag
public class EntityKankuro extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 295;
	public static final int ENTITYID_RANGED = 296;

	public EntityKankuro(ElementsNarutomodMod instance) {
		super(instance, 619);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("narutomod", "kankuro"), ENTITYID)
				.name("kankuro").tracker(64, 3, true).egg(-16777216, -3355444).build());
	}

	@Override
	public void init(FMLInitializationEvent event) {
		EntityRegistry.addSpawn(EntityCustom.class, 5, 1, 1, EnumCreatureType.AMBIENT, Biomes.DESERT, Biomes.DESERT_HILLS, Biomes.MUTATED_DESERT);
	}

	public static class EntityCustom extends EntityNinjaMerchant.Base {
		public EntityCustom(World world) {
			super(world, 80);
			this.setSize(0.6f, 1.8f);
		}

		@Override
		public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
			this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ItemNinjaArmorSuna.body));
			return super.onInitialSpawn(difficulty, livingdata);
		}

		@Override
		public Map<EntityNinjaMerchant.TradeLevel, MerchantRecipeList> getTrades() {
			Map<EntityNinjaMerchant.TradeLevel, MerchantRecipeList> trades = Maps.newHashMap();

			MerchantRecipeList commonTrades = new MerchantRecipeList();
			commonTrades.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 2), ItemStack.EMPTY, new ItemStack(ItemSmokeBomb.block, 8), 0, 1));
			commonTrades.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 4), ItemStack.EMPTY, new ItemStack(ItemPoisonbomb.block, 8), 0, 1));
			commonTrades.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 2), ItemStack.EMPTY, new ItemStack(ItemSenbon.block, 16), 0, 1));
			commonTrades.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 4), ItemStack.EMPTY, new ItemStack(ItemPoisonSenbon.block, 16), 0, 1));
			MerchantRecipeList uncommonTrades = new MerchantRecipeList();
			uncommonTrades.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 20), ItemStack.EMPTY, new ItemStack(ItemScrollPuppet.block, 1), 0, 1));
			MerchantRecipeList rareTrades = new MerchantRecipeList();
			rareTrades.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 45), ItemStack.EMPTY, new ItemStack(ItemScrollKarasu.block, 1), 0, 1));
			rareTrades.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 45), ItemStack.EMPTY, new ItemStack(ItemScrollSanshouo.block, 1), 0, 1));

			trades.put(EntityNinjaMerchant.TradeLevel.COMMON, commonTrades);
			trades.put(EntityNinjaMerchant.TradeLevel.UNCOMMON, uncommonTrades);
			trades.put(EntityNinjaMerchant.TradeLevel.RARE, rareTrades);
			return trades;
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10D);
		}

		@Override
		protected void initEntityAI() {
			super.initEntityAI();
			this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityMob.class, 10, false, false, (p)-> {
				return p instanceof EntityZombie|| p instanceof EntityCreeper || p instanceof AbstractSkeleton;
			}) {
				@Override
				public boolean shouldExecute() {
					return EntityCustom.this.hasHome() && super.shouldExecute();
				}
			});
		}

		@Override
		protected void updateAITasks() {
			super.updateAITasks();
		}

		@Override
		public boolean getCanSpawnHere() {
			return super.getCanSpawnHere() 
			 && this.world.getEntitiesWithinAABB(EntityCustom.class, this.getEntityBoundingBox().grow(128d, 16d, 128d)).isEmpty();
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
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/kankuro.png");

			public RenderCustom(RenderManager renderManagerIn) {
				super(renderManagerIn, new ModelKankuro());
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

		// Made with Blockbench 4.12.1
		// Exported for Minecraft version 1.7 - 1.12
		// Paste this class into your mod and generate all required imports
		@SideOnly(Side.CLIENT)
		public class ModelKankuro extends EntityNinjaMob.ModelNinja {
			private final ModelRenderer bone;
			private final ModelRenderer bone3;
			private final ModelRenderer bone2;
			private final ModelRenderer bone4;
			public ModelKankuro() {
				textureWidth = 64;
				textureHeight = 64;
				bipedHead = new ModelRenderer(this);
				bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHead.cubeList.add(new ModelBox(bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, false));
				bipedHeadwear = new ModelRenderer(this);
				bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHeadwear.cubeList.add(new ModelBox(bipedHeadwear, 32, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.25F, false));
				bipedHeadwear.cubeList.add(new ModelBox(bipedHeadwear, 0, 55, -4.0F, 0.5F, -4.0F, 8, 1, 8, 0.25F, false));
				bone = new ModelRenderer(this);
				bone.setRotationPoint(-3.25F, -8.25F, -4.25F);
				bipedHeadwear.addChild(bone);
				setRotationAngle(bone, -0.7854F, 0.0F, 0.0F);
				bone.cubeList.add(new ModelBox(bone, 56, 0, -1.0F, -2.0F, 0.0F, 2, 2, 2, 0.0F, false));
				bone3 = new ModelRenderer(this);
				bone3.setRotationPoint(3.25F, -8.25F, -4.25F);
				bipedHeadwear.addChild(bone3);
				setRotationAngle(bone3, -0.7854F, 0.0F, 0.0F);
				bone3.cubeList.add(new ModelBox(bone3, 56, 0, -1.0F, -2.0F, 0.0F, 2, 2, 2, 0.0F, true));
				bone2 = new ModelRenderer(this);
				bone2.setRotationPoint(-2.25F, -8.25F, -2.75F);
				bipedHeadwear.addChild(bone2);
				setRotationAngle(bone2, 0.0F, 0.0F, -0.9599F);
				bone2.cubeList.add(new ModelBox(bone2, 56, 2, 0.0F, -3.0F, -1.5F, 0, 3, 3, 0.0F, false));
				bone4 = new ModelRenderer(this);
				bone4.setRotationPoint(2.25F, -8.25F, -2.75F);
				bipedHeadwear.addChild(bone4);
				setRotationAngle(bone4, 0.0F, 0.0F, 0.9599F);
				bone4.cubeList.add(new ModelBox(bone4, 56, 2, 0.0F, -3.0F, -1.5F, 0, 3, 3, 0.0F, true));
				bipedBody = new ModelRenderer(this);
				bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.25F, false));
				bipedRightArm = new ModelRenderer(this);
				bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
				bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 16, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, false));
				bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 32, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.25F, false));
				bipedLeftArm = new ModelRenderer(this);
				bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
				bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 16, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, true));
				bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 32, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.25F, true));
				bipedRightLeg = new ModelRenderer(this);
				bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
				bipedRightLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, false));
				bipedRightLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 32, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F, false));
				bipedLeftLeg = new ModelRenderer(this);
				bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
				bipedLeftLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, true));
				bipedLeftLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 32, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F, true));
			}
		}
	}
}
