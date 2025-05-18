
package net.narutomod.entity;

import net.narutomod.ElementsNarutomodMod;
import net.narutomod.item.*;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.MerchantRecipe;

import java.util.Map;
import javax.annotation.Nullable;
import com.google.common.collect.Maps;

@ElementsNarutomodMod.ModElement.Tag
public class EntityTemari extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 297;
	public static final int ENTITYID_RANGED = 298;

	public EntityTemari(ElementsNarutomodMod instance) {
		super(instance, 620);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("narutomod", "temari"), ENTITYID)
				.name("temari").tracker(64, 3, true).egg(-13421773, -3407821).build());
	}

	@Override
	public void init(FMLInitializationEvent event) {
		EntityRegistry.addSpawn(EntityCustom.class, 5, 1, 1, EnumCreatureType.AMBIENT, Biomes.DESERT, Biomes.DESERT_HILLS, Biomes.MUTATED_DESERT);
	}

	public static class EntityCustom extends EntityNinjaMerchant.Base {
		public EntityCustom(World world) {
			super(world, 80);
			this.setSize(0.525f, 1.75f);
		}

		@Override
		public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
			ItemStack stack = new ItemStack(ItemNinjaArmorSuna.helmet);
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setBoolean("noHeadClothe", true);
			this.setItemStackToSlot(EntityEquipmentSlot.HEAD, stack);
			this.setItemToInventory(new ItemStack(ItemFoldingFan.block), 0);
			return super.onInitialSpawn(difficulty, livingdata);
		}

		@Override
		public Map<EntityNinjaMerchant.TradeLevel, MerchantRecipeList> getTrades() {
			Map<EntityNinjaMerchant.TradeLevel, MerchantRecipeList> trades = Maps.newHashMap();

			MerchantRecipeList commonTrades = new MerchantRecipeList();
			commonTrades.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 2), ItemStack.EMPTY, new ItemStack(ItemRice.block, 2), 0, 1));
			commonTrades.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 5), ItemStack.EMPTY, new ItemStack(ItemMilitaryRationsPill.block, 2), 0, 1));
			MerchantRecipeList uncommonTrades = new MerchantRecipeList();
			uncommonTrades.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 12), ItemStack.EMPTY, new ItemStack(ItemFoldingFan.block, 1), 0, 1));
			uncommonTrades.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 15), ItemStack.EMPTY, new ItemStack(ItemScrollBigBlow.block, 1), 0, 1));

			trades.put(EntityNinjaMerchant.TradeLevel.COMMON, commonTrades);
			trades.put(EntityNinjaMerchant.TradeLevel.UNCOMMON, uncommonTrades);
			return trades;
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
			this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityMob.class, 10, false, false, (p)-> {
				return p instanceof EntityZombie || p instanceof EntityCreeper || p instanceof AbstractSkeleton;
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
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/temari.png");

			public RenderCustom(RenderManager renderManagerIn) {
				super(renderManagerIn, new ModelTemari());
			}

			@Override
			protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
				float f = 0.0625f * 14;
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

		// Made with Blockbench 4.12.2
		// Exported for Minecraft version 1.7 - 1.12
		// Paste this class into your mod and generate all required imports
		@SideOnly(Side.CLIENT)
		public class ModelTemari extends EntityNinjaMob.ModelNinja {
			private final ModelRenderer bone3;
			private final ModelRenderer bone;
			private final ModelRenderer bone2;
			private final ModelRenderer bone4;
			private final ModelRenderer bone5;
			private final ModelRenderer bone6;
			private final ModelRenderer bone7;
			private final ModelRenderer bone8;
			private final ModelRenderer bone9;
			private final ModelRenderer bone10;
			private final ModelRenderer bone11;
			private final ModelRenderer bone12;
			private final ModelRenderer bone13;
			private final ModelRenderer bone14;
			private final ModelRenderer bone15;
			private final ModelRenderer bone16;
			private final ModelRenderer bone17;
			private final ModelRenderer bone18;
			public ModelTemari() {
				textureWidth = 64;
				textureHeight = 64;
		
				bipedHead = new ModelRenderer(this);
				bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHead.cubeList.add(new ModelBox(bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, false));
		
				bipedHeadwear = new ModelRenderer(this);
				bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHeadwear.cubeList.add(new ModelBox(bipedHeadwear, 32, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.6F, false));
		
				bone3 = new ModelRenderer(this);
				bone3.setRotationPoint(-2.0F, -6.0F, 4.0F);
				bipedHeadwear.addChild(bone3);
				setRotationAngle(bone3, 0.0F, 0.5236F, 0.5236F);
				
		
				bone = new ModelRenderer(this);
				bone.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone3.addChild(bone);
				setRotationAngle(bone, 0.5236F, 0.0F, 0.0F);
				bone.cubeList.add(new ModelBox(bone, 24, 0, -6.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, false));
		
				bone2 = new ModelRenderer(this);
				bone2.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone3.addChild(bone2);
				setRotationAngle(bone2, -0.5236F, 0.0F, 0.0F);
				bone2.cubeList.add(new ModelBox(bone2, 24, 0, -6.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, false));
		
				bone4 = new ModelRenderer(this);
				bone4.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone3.addChild(bone4);
				setRotationAngle(bone4, -1.5708F, 0.0F, 0.0F);
				bone4.cubeList.add(new ModelBox(bone4, 24, 0, -6.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, false));
		
				bone5 = new ModelRenderer(this);
				bone5.setRotationPoint(2.0F, -6.0F, 4.0F);
				bipedHeadwear.addChild(bone5);
				setRotationAngle(bone5, 0.0F, -0.5236F, -0.5236F);
				
		
				bone6 = new ModelRenderer(this);
				bone6.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone5.addChild(bone6);
				setRotationAngle(bone6, 0.5236F, 0.0F, 0.0F);
				bone6.cubeList.add(new ModelBox(bone6, 24, 0, 0.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, true));
		
				bone7 = new ModelRenderer(this);
				bone7.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone5.addChild(bone7);
				setRotationAngle(bone7, -0.5236F, 0.0F, 0.0F);
				bone7.cubeList.add(new ModelBox(bone7, 24, 0, 0.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, true));
		
				bone8 = new ModelRenderer(this);
				bone8.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone5.addChild(bone8);
				setRotationAngle(bone8, -1.5708F, 0.0F, 0.0F);
				bone8.cubeList.add(new ModelBox(bone8, 24, 0, 0.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, true));
		
				bone9 = new ModelRenderer(this);
				bone9.setRotationPoint(-2.0F, -2.0F, 4.0F);
				bipedHeadwear.addChild(bone9);
				setRotationAngle(bone9, 0.0F, 0.5236F, -0.5236F);
				
		
				bone10 = new ModelRenderer(this);
				bone10.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone9.addChild(bone10);
				setRotationAngle(bone10, -0.5236F, 0.0F, 0.0F);
				bone10.cubeList.add(new ModelBox(bone10, 24, 0, -6.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, false));
		
				bone11 = new ModelRenderer(this);
				bone11.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone9.addChild(bone11);
				setRotationAngle(bone11, 0.5236F, 0.0F, 0.0F);
				bone11.cubeList.add(new ModelBox(bone11, 24, 0, -6.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, false));
		
				bone12 = new ModelRenderer(this);
				bone12.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone9.addChild(bone12);
				setRotationAngle(bone12, 1.5708F, 0.0F, 0.0F);
				bone12.cubeList.add(new ModelBox(bone12, 24, 0, -6.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, false));
		
				bone13 = new ModelRenderer(this);
				bone13.setRotationPoint(2.0F, -2.0F, 4.0F);
				bipedHeadwear.addChild(bone13);
				setRotationAngle(bone13, 0.0F, -0.5236F, 0.5236F);
				
		
				bone14 = new ModelRenderer(this);
				bone14.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone13.addChild(bone14);
				setRotationAngle(bone14, -0.5236F, 0.0F, 0.0F);
				bone14.cubeList.add(new ModelBox(bone14, 24, 0, 0.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, true));
		
				bone15 = new ModelRenderer(this);
				bone15.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone13.addChild(bone15);
				setRotationAngle(bone15, 0.5236F, 0.0F, 0.0F);
				bone15.cubeList.add(new ModelBox(bone15, 24, 0, 0.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, true));
		
				bone16 = new ModelRenderer(this);
				bone16.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone13.addChild(bone16);
				setRotationAngle(bone16, 1.5708F, 0.0F, 0.0F);
				bone16.cubeList.add(new ModelBox(bone16, 24, 0, 0.0F, -3.0F, 0.0F, 6, 6, 0, 0.0F, true));
		
				bipedBody = new ModelRenderer(this);
				bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F, false));
				bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 32, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.25F, false));
		
				bipedRightArm = new ModelRenderer(this);
				bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
				setRotationAngle(bipedRightArm, -0.1745F, 0.0F, 0.0F);
				bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 16, -2.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F, false));
				bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 32, -2.0F, -2.0F, -2.0F, 3, 12, 4, 0.25F, false));
		
				bipedLeftArm = new ModelRenderer(this);
				bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
				setRotationAngle(bipedLeftArm, 0.2094F, 0.0F, 0.0F);
				bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 32, 48, -1.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F, false));
				bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 48, 48, -1.0F, -2.0F, -2.0F, 3, 12, 4, 0.25F, false));
		
				bipedRightLeg = new ModelRenderer(this);
				bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
				bipedRightLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, false));
		
				bone17 = new ModelRenderer(this);
				bone17.setRotationPoint(-2.1F, -1.0F, 0.0F);
				bipedRightLeg.addChild(bone17);
				setRotationAngle(bone17, 0.0F, 0.0F, 0.1309F);
				bone17.cubeList.add(new ModelBox(bone17, 0, 32, 0.1F, 0.0F, -2.0F, 4, 12, 4, 0.25F, false));
		
				bipedLeftLeg = new ModelRenderer(this);
				bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
				bipedLeftLeg.cubeList.add(new ModelBox(bipedLeftLeg, 16, 48, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, false));
		
				bone18 = new ModelRenderer(this);
				bone18.setRotationPoint(2.1F, -1.0F, 0.0F);
				bipedLeftLeg.addChild(bone18);
				setRotationAngle(bone18, 0.0F, 0.0F, -0.1309F);
				bone18.cubeList.add(new ModelBox(bone18, 0, 48, -4.1F, 0.0F, -2.0F, 4, 12, 4, 0.25F, false));
			}
		}
	}
}
