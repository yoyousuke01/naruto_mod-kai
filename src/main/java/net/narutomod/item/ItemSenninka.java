
package net.narutomod.item;

import net.narutomod.creativetab.TabModTab;
import net.narutomod.Chakra;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.entity.EntityRendererRegister;
import net.narutomod.gui.overlay.OverlayChakraDisplay;
import net.narutomod.Particles;
import net.narutomod.procedure.ProcedureSync;
import net.narutomod.procedure.ProcedureUtils;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
//import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
//import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;

@ElementsNarutomodMod.ModElement.Tag
public class ItemSenninka extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:senninka")
	public static final Item block = null;
	public static final int ENTITYID = 524;
	private static final String CHAKRA_BEFORE = "ChakraAmountB4Activation";
	private static final String START_TIME = "SenninkaStartTime";
	public static final ItemJutsu.JutsuEnum BROADAXE = new ItemJutsu.JutsuEnum(0, "item.senninka_broadaxe.name", 'S', 150, 50d, new Broadaxe());
	public static final ItemJutsu.JutsuEnum PISTONFIST = new ItemJutsu.JutsuEnum(1, "item.senninka.pistonfist", 'S', 150, 50d, new PistonFist());
	public static final ItemJutsu.JutsuEnum STAGE2 = new ItemJutsu.JutsuEnum(2, "item.senninka.stage2", 'S', 150, 50d, new Stage2());

	public ItemSenninka(ElementsNarutomodMod instance) {
		super(instance, 939);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new RangedItem(BROADAXE, PISTONFIST, STAGE2));
		//elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityArrowCustom.class)
		//		.id(new ResourceLocation("narutomod", "entitybulletsenninka"), ENTITYID).name("entitybulletsenninka").tracker(64, 1, true).build());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("narutomod:senninka", "inventory"));
	}

	public static class RangedItem extends ItemJutsu.Base implements ItemOnBody.Interface {
		@SideOnly(Side.CLIENT)
		private ModelBiped armorModel;

		public RangedItem(ItemJutsu.JutsuEnum... list) {
			super(ItemJutsu.JutsuEnum.Type.SENNINKA, list);
			setUnlocalizedName("senninka");
			setRegistryName("senninka");
			setCreativeTab(TabModTab.tab);
		}

		@Override
		public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) {
			super.onUpdate(itemstack, world, entity, par4, par5);
			if (entity instanceof EntityLivingBase) {
				for (ItemJutsu.JutsuEnum jutsuEnum : ((RangedItem)itemstack.getItem()).getAllJutsus(itemstack)) {
					if (((RangedItem)itemstack.getItem()).canUseJutsu(itemstack, jutsuEnum, (EntityLivingBase)entity)) {
						((SenninkaJutsu)jutsuEnum.jutsu).onUpdate(itemstack, world, entity, par4, par5);
					}
				}
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
			if (this.armorModel == null) {
				this.armorModel = new Renderer.ModelJugo();
			}
			for (ItemJutsu.JutsuEnum jutsuEnum : ((RangedItem)stack.getItem()).getAllJutsus(stack)) {
				if (((SenninkaJutsu)jutsuEnum.jutsu).setModelVisibility(living, stack, (Renderer.ModelJugo)this.armorModel)) {
					return this.armorModel;
				}
			}
			return null;
		}

		@Override
		public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
			return "narutomod:textures/jugo.png";
		}

		@Override
		public boolean showSkinLayer() {
			return true;
		}

		@Override
		public ItemOnBody.BodyPart showOnBody() {
			return ItemOnBody.BodyPart.NONE;
		}
	}

	public static abstract class SenninkaJutsu implements ItemJutsu.IJutsuCallback {
		public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) {
		}

		@SideOnly(Side.CLIENT)
		public abstract boolean setModelVisibility(EntityLivingBase living, ItemStack stack, Renderer.ModelJugo model);
	}

	public static class Broadaxe extends SenninkaJutsu {
		@Override
		public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
			if (entity instanceof EntityPlayer && !ProcedureUtils.hasItemInInventory((EntityPlayer)entity, ItemSenninkaBroadaxe.block)) {
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ,
				 SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:woodgrow")), SoundCategory.PLAYERS, 1f, 1f);
				//Chakra.Pathway cp = Chakra.pathway(entity);
				//stack.getTagCompound().setDouble(CHAKRA_BEFORE, cp.getAmount());
				//float f = ((RangedItem)stack.getItem()).getCurrentJutsuXpModifier(stack, entity);
				//cp.consume(-0.5f / f, true);
				//if (entity instanceof EntityPlayerMP) {
				//	OverlayChakraDisplay.ShowFlamesMessage.send((EntityPlayerMP)entity, true);
				//}
				ItemStack itemstack = new ItemStack(ItemSenninkaBroadaxe.block);
				ProcedureUtils.swapItemToSlot((EntityPlayer)entity, EntityEquipmentSlot.MAINHAND, itemstack);
				return true;
			}
			return false;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean setModelVisibility(EntityLivingBase living, ItemStack stack, Renderer.ModelJugo model) {
			if (living.getHeldItemMainhand().getItem() == ItemSenninkaBroadaxe.block
			 && !PISTONFIST.jutsu.isActivated(stack) && !STAGE2.jutsu.isActivated(stack)) {
				model.setVisible(false);
				model.bipedHead.showModel = true;
				model.headStage1.showModel = true;
				model.bipedRightArm.showModel = true;
				model.rightArmSpikes.rotateAngleX = 3.1416F;
				model.bipedBody.showModel = true;
				model.bodyStage1.showModel = true;
				model.isSneak = living.isSneaking();
				model.isRiding = living.isRiding();
				model.isChild = living.isChild();
				return true;
			}
			return false;
		}
	}

	public static class PistonFist extends SenninkaJutsu {
		private final String idKey = "PistonFistStackKey";
		private final Map<IAttribute, AttributeModifier> buffMap = ImmutableMap.<IAttribute, AttributeModifier>builder()
			.put(SharedMonsterAttributes.ATTACK_DAMAGE, new AttributeModifier(ItemSenjutsu.RangedItem.ATTACK_DAMAGE_MODIFIER, "senninka.damage", 50.0d, 0))
			.put(SharedMonsterAttributes.MOVEMENT_SPEED, new AttributeModifier(ItemSenjutsu.RangedItem.MOVEMENT_SPEED_MODIFIER, "senninka.movement", 1.5d, 1))
			.build();

		@Override
		public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
			if (!this.isActivated(stack)) {
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ,
				 SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:woodgrow")), SoundCategory.PLAYERS, 1f, 1f);
				STAGE2.jutsu.deactivate(entity);
				stack.getTagCompound().setBoolean(this.idKey, true);
				ProcedureSync.EntityNBTTag.setAndSync(entity, START_TIME, entity.ticksExisted + 3);
				for (Map.Entry<IAttribute, AttributeModifier> entry : this.buffMap.entrySet()) {
					IAttributeInstance attr = entity.getEntityAttribute(entry.getKey());
					if (attr != null) {
						attr.applyModifier(entry.getValue());
					}
				}
				return true;
			} else {
				this.deactivate(entity);
			}
			return false;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean setModelVisibility(EntityLivingBase living, ItemStack stack, Renderer.ModelJugo model) {
			if (this.isActivated(stack)) {
				model.setVisible(false);
				model.bipedHead.showModel = true;
				model.headStage1.showModel = true;
				model.bipedRightArm.showModel = true;
				model.armExhaust.showModel = true;
				model.rightArmSpikes.rotateAngleX = 0.0F;
				model.bipedBody.showModel = true;
				model.bodyStage1.showModel = true;
				model.isSneak = living.isSneaking();
				model.isRiding = living.isRiding();
				model.isChild = living.isChild();
				return true;
			}
			return false;
		}

		@Override
		public boolean isActivated(ItemStack stack) {
			return stack.hasTagCompound() && stack.getTagCompound().getBoolean(this.idKey);
		}

		@Override
		public void deactivate(EntityLivingBase entity) {
			for (Map.Entry<IAttribute, AttributeModifier> entry : this.buffMap.entrySet()) {
				IAttributeInstance attr = entity.getEntityAttribute(entry.getKey());
				if (attr != null) {
					attr.removeModifier(entry.getValue());
				}
			}
			ItemStack stack = ProcedureUtils.getMatchingItemStack(entity, block);
			if (stack != null) {
				stack.getTagCompound().removeTag(this.idKey);
			}
			ProcedureSync.EntityNBTTag.removeAndSync(entity, START_TIME);
		}
	}

	public static class Stage2 extends SenninkaJutsu {
		private final String idKey = "Stage2StackKey";
		private final Map<IAttribute, AttributeModifier> buffMap = ImmutableMap.<IAttribute, AttributeModifier>builder()
			.put(SharedMonsterAttributes.ATTACK_DAMAGE, new AttributeModifier(ItemSenjutsu.RangedItem.ATTACK_DAMAGE_MODIFIER, "senninka.damage", 60.0d, 0))
			.put(SharedMonsterAttributes.ATTACK_SPEED, new AttributeModifier(ItemSenjutsu.RangedItem.ATTACK_SPEED_MODIFIER, "senninka.damagespeed", 2.0d, 1))
			.put(SharedMonsterAttributes.MOVEMENT_SPEED, new AttributeModifier(ItemSenjutsu.RangedItem.MOVEMENT_SPEED_MODIFIER, "senninka.movement", 1.8d, 1))
			.put(SharedMonsterAttributes.MAX_HEALTH, new AttributeModifier(ItemSenjutsu.RangedItem.MAX_HEALTH_MODIFIER, "senninka.health", 80.0d, 0))
			.build();

		@Override
		public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
			if (!this.isActivated(stack)) {
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ,
				 SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:woodgrow")), SoundCategory.PLAYERS, 1f, 1f);
				PISTONFIST.jutsu.deactivate(entity);
				stack.getTagCompound().setBoolean(this.idKey, true);
				ProcedureSync.EntityNBTTag.setAndSync(entity, START_TIME, entity.ticksExisted + 3);
				for (Map.Entry<IAttribute, AttributeModifier> entry : this.buffMap.entrySet()) {
					IAttributeInstance attr = entity.getEntityAttribute(entry.getKey());
					if (attr != null && !attr.hasModifier(entry.getValue())) {
						attr.applyModifier(entry.getValue());
					}
				}
				return true;
			} else {
				this.deactivate(entity);
			}
			return false;
		}

		@Override
		public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) {
			if (this.isActivated(itemstack) && entity instanceof EntityLivingBase && !world.isRemote) {
				if (entity.ticksExisted % 20 == 3) {
					((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 22, 8, false, false));
				}
				if (entity.ticksExisted > entity.getEntityData().getInteger(START_TIME) + 40) {
					Vec3d vec = new Vec3d(-0.365625d, 0.884375d, -0.440625d)
					 .rotateYaw(-((EntityLivingBase)entity).renderYawOffset * (float)Math.PI / 180F)
					 .add(entity.getPositionVector());
					Vec3d vec1 = new Vec3d(-0.25d, -0.0625d, -0.25d).scale(1.4d)
					 .rotateYaw(-((EntityLivingBase)entity).renderYawOffset * (float)Math.PI / 180F);
					Particles.spawnParticle(world, Particles.Types.SMOKE, vec.x, vec.y, vec.z, 20,
					 0d, 0d, 0d, vec1.x, vec1.y, vec1.z, 0x20FFFFFF, 10, 3, 0xF0, entity.getEntityId());
					vec = new Vec3d(-0.365625d, 0.61875d, -0.409375d)
					 .rotateYaw(-((EntityLivingBase)entity).renderYawOffset * (float)Math.PI / 180F)
					 .add(entity.getPositionVector());
					Particles.spawnParticle(world, Particles.Types.SMOKE, vec.x, vec.y, vec.z, 20,
					 0d, 0d, 0d, vec1.x, vec1.y, vec1.z, 0x20FFFFFF, 10, 3, 0xF0, entity.getEntityId());
					vec = new Vec3d(0.365625d, 0.884375d, -0.440625d)
					 .rotateYaw(-((EntityLivingBase)entity).renderYawOffset * (float)Math.PI / 180F)
					 .add(entity.getPositionVector());
					vec1 = new Vec3d(0.25d, -0.0625d, -0.25d).scale(1.4d)
					 .rotateYaw(-((EntityLivingBase)entity).renderYawOffset * (float)Math.PI / 180F);
					Particles.spawnParticle(world, Particles.Types.SMOKE, vec.x, vec.y, vec.z, 20,
					 0d, 0d, 0d, vec1.x, vec1.y, vec1.z, 0x20FFFFFF, 10, 3, 0xF0, entity.getEntityId());
					vec = new Vec3d(0.365625d, 0.61875d, -0.409375d)
					 .rotateYaw(-((EntityLivingBase)entity).renderYawOffset * (float)Math.PI / 180F)
					 .add(entity.getPositionVector());
					Particles.spawnParticle(world, Particles.Types.SMOKE, vec.x, vec.y, vec.z, 20,
					 0d, 0d, 0d, vec1.x, vec1.y, vec1.z, 0x20FFFFFF, 10, 3, 0xF0, entity.getEntityId());
				}
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean setModelVisibility(EntityLivingBase living, ItemStack stack, Renderer.ModelJugo model) {
			if (this.isActivated(stack)) {
				model.setVisible(false);
				model.bipedHead.showModel = true;
				model.bipedHeadwear.showModel = true;
				model.headStage0.showModel = true;
				model.headStage2.showModel = true;
				model.bipedRightArm.showModel = true;
				model.bipedLeftArm.showModel = true;
				model.armExhaust.showModel = true;
				model.rightArmSpikes.rotateAngleX = 0.0F;
				model.bipedBody.showModel = true;
				model.bodyStage2.showModel = true;
				model.isSneak = living.isSneaking();
				model.isRiding = living.isRiding();
				model.isChild = living.isChild();
				return true;
			}
			return false;
		}

		@Override
		public boolean isActivated(ItemStack stack) {
			return stack.hasTagCompound() && stack.getTagCompound().getBoolean(this.idKey);
		}

		@Override
		public void deactivate(EntityLivingBase entity) {
			for (Map.Entry<IAttribute, AttributeModifier> entry : this.buffMap.entrySet()) {
				IAttributeInstance attr = entity.getEntityAttribute(entry.getKey());
				if (attr != null) {
					attr.removeModifier(entry.getValue());
				}
			}
			ItemStack stack = ProcedureUtils.getMatchingItemStack(entity, block);
			if (stack != null) {
				stack.getTagCompound().removeTag(this.idKey);
			}
			ProcedureSync.EntityNBTTag.removeAndSync(entity, START_TIME);
		}
	}

	/*public static class EntityArrowCustom extends EntityTippedArrow {
		public EntityArrowCustom(World a) {
			super(a);
		}

		public EntityArrowCustom(World worldIn, double x, double y, double z) {
			super(worldIn, x, y, z);
		}

		public EntityArrowCustom(World worldIn, EntityLivingBase shooter) {
			super(worldIn, shooter);
		}

		@Override
		protected void arrowHit(EntityLivingBase entity) {
			super.arrowHit(entity);
			entity.setArrowCountInEntity(entity.getArrowCountInEntity() - 1);
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			int x = (int) this.posX;
			int y = (int) this.posY;
			int z = (int) this.posZ;
			World world = this.world;
			Entity entity = (Entity) shootingEntity;
			if (this.inGround) {
				this.world.removeEntity(this);
			}
		}
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		new Renderer().register();
	}*/

	public static class Renderer extends EntityRendererRegister {
		@SideOnly(Side.CLIENT)
		public static class ModelJugo extends ModelBiped {
			//private final ModelRenderer bipedHead;
			private final ModelRenderer headStage0;
			private final ModelRenderer hair;
			private final ModelRenderer bone1;
			private final ModelRenderer bone7;
			private final ModelRenderer bone9;
			private final ModelRenderer bone15;
			private final ModelRenderer bone2;
			private final ModelRenderer bone6;
			private final ModelRenderer bone3;
			private final ModelRenderer bone8;
			private final ModelRenderer bone11;
			private final ModelRenderer bone18;
			private final ModelRenderer bone12;
			private final ModelRenderer bone17;
			private final ModelRenderer bone10;
			private final ModelRenderer bone16;
			private final ModelRenderer bone4;
			private final ModelRenderer bone13;
			private final ModelRenderer bone14;
			private final ModelRenderer bone5;
			private final ModelRenderer headStage1;
			private final ModelRenderer headStage2;
			private final ModelRenderer eyeRight;
			private final ModelRenderer eyeLeft;
			//private final ModelRenderer bipedHeadwear;
			//private final ModelRenderer bipedBody;
			private final ModelRenderer bodyStage1;
			private final ModelRenderer bodyStage2;
			private final ModelRenderer exhaust1;
			private final ModelRenderer bone33;
			private final ModelRenderer bone47;
			private final ModelRenderer exhaustExtension1;
			private final ModelRenderer bone67;
			private final ModelRenderer bone69;
			private final ModelRenderer bone70;
			private final ModelRenderer bone71;
			private final ModelRenderer ball1;
			private final ModelRenderer exhaust2;
			private final ModelRenderer bone30;
			private final ModelRenderer bone31;
			private final ModelRenderer exhaustExtension2;
			private final ModelRenderer bone99;
			private final ModelRenderer bone100;
			private final ModelRenderer bone101;
			private final ModelRenderer bone102;
			private final ModelRenderer ball2;
			private final ModelRenderer exhaust3;
			private final ModelRenderer bone74;
			private final ModelRenderer bone75;
			private final ModelRenderer exhaustExtension3;
			private final ModelRenderer bone77;
			private final ModelRenderer bone78;
			private final ModelRenderer bone79;
			private final ModelRenderer bone80;
			private final ModelRenderer ball3;
			private final ModelRenderer exhaust4;
			private final ModelRenderer bone84;
			private final ModelRenderer bone90;
			private final ModelRenderer exhaustExtension4;
			private final ModelRenderer bone93;
			private final ModelRenderer bone98;
			private final ModelRenderer bone103;
			private final ModelRenderer bone104;
			private final ModelRenderer ball4;
			private final ModelRenderer exhaust5;
			private final ModelRenderer bone82;
			private final ModelRenderer bone83;
			private final ModelRenderer exhaustExtension5;
			private final ModelRenderer bone85;
			private final ModelRenderer bone87;
			private final ModelRenderer bone88;
			private final ModelRenderer bone89;
			private final ModelRenderer bone81;
			private final ModelRenderer ball5;
			private final ModelRenderer exhaust6;
			private final ModelRenderer bone64;
			private final ModelRenderer bone65;
			private final ModelRenderer exhaustExtension6;
			private final ModelRenderer bone66;
			private final ModelRenderer bone68;
			private final ModelRenderer bone73;
			private final ModelRenderer bone76;
			private final ModelRenderer bone105;
			private final ModelRenderer ball6;
			private final ModelRenderer exhaust7;
			private final ModelRenderer bone91;
			private final ModelRenderer bone92;
			private final ModelRenderer exhaustExtension7;
			private final ModelRenderer bone94;
			private final ModelRenderer bone95;
			private final ModelRenderer bone96;
			private final ModelRenderer bone97;
			private final ModelRenderer ball7;
			private final ModelRenderer exhaust8;
			private final ModelRenderer bone58;
			private final ModelRenderer bone59;
			private final ModelRenderer exhaustExtension8;
			private final ModelRenderer bone60;
			private final ModelRenderer bone61;
			private final ModelRenderer bone62;
			private final ModelRenderer bone63;
			private final ModelRenderer ball8;
			//private final ModelRenderer bipedRightArm;
			private final ModelRenderer rightArmSpikes;
			private final ModelRenderer bone129;
			private final ModelRenderer bone72;
			private final ModelRenderer bone25;
			private final ModelRenderer bone125;
			private final ModelRenderer bone126;
			private final ModelRenderer bone127;
			private final ModelRenderer bone128;
			private final ModelRenderer bone124;
			private final ModelRenderer bone86;
			private final ModelRenderer bone131;
			private final ModelRenderer bone132;
			private final ModelRenderer bone133;
			private final ModelRenderer bone134;
			private final ModelRenderer bone135;
			private final ModelRenderer bone136;
			private final ModelRenderer bone137;
			private final ModelRenderer bone138;
			private final ModelRenderer bone139;
			private final ModelRenderer bone140;
			private final ModelRenderer bone141;
			private final ModelRenderer bone142;
			private final ModelRenderer bone143;
			private final ModelRenderer bone144;
			private final ModelRenderer bone145;
			private final ModelRenderer bone146;
			private final ModelRenderer bone147;
			private final ModelRenderer bone148;
			private final ModelRenderer bone150;
			private final ModelRenderer bone151;
			private final ModelRenderer bone152;
			private final ModelRenderer bone153;
			private final ModelRenderer bone154;
			private final ModelRenderer bone157;
			private final ModelRenderer bone158;
			private final ModelRenderer broadaxe;
			private final ModelRenderer armExhaust;
			private final ModelRenderer bone106;
			private final ModelRenderer bone109;
			private final ModelRenderer bone110;
			private final ModelRenderer bone107;
			private final ModelRenderer bone108;
			//private final ModelRenderer bipedLeftArm;
			private final ModelRenderer leftArmSpikes;
			private final ModelRenderer bone19;
			private final ModelRenderer bone20;
			private final ModelRenderer bone21;
			private final ModelRenderer bone22;
			private final ModelRenderer bone23;
			private final ModelRenderer bone24;
			private final ModelRenderer bone26;
			private final ModelRenderer bone27;
			private final ModelRenderer bone28;
			private final ModelRenderer bone29;
			private final ModelRenderer bone32;
			private final ModelRenderer bone34;
			private final ModelRenderer bone35;
			private final ModelRenderer bone36;
			private final ModelRenderer bone37;
			private final ModelRenderer bone38;
			private final ModelRenderer bone39;
			private final ModelRenderer bone40;
			private final ModelRenderer bone41;
			private final ModelRenderer bone42;
			private final ModelRenderer bone43;
			private final ModelRenderer bone44;
			private final ModelRenderer bone45;
			private final ModelRenderer bone46;
			private final ModelRenderer bone48;
			private final ModelRenderer bone49;
			private final ModelRenderer bone50;
			private final ModelRenderer bone51;
			private final ModelRenderer bone52;
			private final ModelRenderer bone53;
			private final ModelRenderer bone54;
			private final ModelRenderer bone55;
			private final ModelRenderer bone56;
			private final ModelRenderer bone57;
			//private final ModelRenderer bipedRightLeg;
			//private final ModelRenderer bipedLeftLeg;
			private ModelBiped wearerModel;
		
			public ModelJugo() {
				textureWidth = 64;
				textureHeight = 96;
		
				bipedHead = new ModelRenderer(this);
				bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
				
		
				headStage0 = new ModelRenderer(this);
				headStage0.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHead.addChild(headStage0);
				headStage0.cubeList.add(new ModelBox(headStage0, 32, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, false));
		
				hair = new ModelRenderer(this);
				hair.setRotationPoint(0.0F, -1.0F, 0.0F);
				headStage0.addChild(hair);
				
		
				bone1 = new ModelRenderer(this);
				bone1.setRotationPoint(-2.0F, -5.25F, 0.0F);
				hair.addChild(bone1);
				setRotationAngle(bone1, -0.1745F, 0.0F, -0.5236F);
				bone1.cubeList.add(new ModelBox(bone1, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, false));
		
				bone7 = new ModelRenderer(this);
				bone7.setRotationPoint(2.0F, -5.25F, 0.0F);
				hair.addChild(bone7);
				setRotationAngle(bone7, -0.1745F, 0.0F, 0.5236F);
				bone7.cubeList.add(new ModelBox(bone7, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, true));
		
				bone9 = new ModelRenderer(this);
				bone9.setRotationPoint(-2.25F, -4.25F, 0.0F);
				hair.addChild(bone9);
				setRotationAngle(bone9, -0.5236F, 0.5236F, -1.0472F);
				bone9.cubeList.add(new ModelBox(bone9, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, false));
		
				bone15 = new ModelRenderer(this);
				bone15.setRotationPoint(2.25F, -4.25F, 0.0F);
				hair.addChild(bone15);
				setRotationAngle(bone15, -0.5236F, -0.5236F, 1.0472F);
				bone15.cubeList.add(new ModelBox(bone15, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, true));
		
				bone2 = new ModelRenderer(this);
				bone2.setRotationPoint(-2.0F, -5.0F, -2.0F);
				hair.addChild(bone2);
				setRotationAngle(bone2, 0.1745F, 0.0F, -0.3491F);
				bone2.cubeList.add(new ModelBox(bone2, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, false));
		
				bone6 = new ModelRenderer(this);
				bone6.setRotationPoint(2.0F, -5.0F, -2.0F);
				hair.addChild(bone6);
				setRotationAngle(bone6, 0.1745F, 0.0F, 0.3491F);
				bone6.cubeList.add(new ModelBox(bone6, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, true));
		
				bone3 = new ModelRenderer(this);
				bone3.setRotationPoint(-2.0F, -4.5F, 2.0F);
				hair.addChild(bone3);
				setRotationAngle(bone3, -0.5236F, 0.0F, -0.3491F);
				bone3.cubeList.add(new ModelBox(bone3, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, false));
		
				bone8 = new ModelRenderer(this);
				bone8.setRotationPoint(2.0F, -4.5F, 2.0F);
				hair.addChild(bone8);
				setRotationAngle(bone8, -0.5236F, 0.0F, 0.3491F);
				bone8.cubeList.add(new ModelBox(bone8, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, true));
		
				bone11 = new ModelRenderer(this);
				bone11.setRotationPoint(-2.0F, -3.5F, 2.0F);
				hair.addChild(bone11);
				setRotationAngle(bone11, -1.309F, 0.0F, -0.3491F);
				bone11.cubeList.add(new ModelBox(bone11, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, false));
		
				bone18 = new ModelRenderer(this);
				bone18.setRotationPoint(2.0F, -3.5F, 2.0F);
				hair.addChild(bone18);
				setRotationAngle(bone18, -1.309F, 0.0F, 0.3491F);
				bone18.cubeList.add(new ModelBox(bone18, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, true));
		
				bone12 = new ModelRenderer(this);
				bone12.setRotationPoint(-3.0F, -2.5F, 1.0F);
				hair.addChild(bone12);
				setRotationAngle(bone12, -2.0944F, 0.0F, -0.3491F);
				bone12.cubeList.add(new ModelBox(bone12, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, false));
		
				bone17 = new ModelRenderer(this);
				bone17.setRotationPoint(3.0F, -2.5F, 1.0F);
				hair.addChild(bone17);
				setRotationAngle(bone17, -2.0944F, 0.0F, 0.3491F);
				bone17.cubeList.add(new ModelBox(bone17, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, true));
		
				bone10 = new ModelRenderer(this);
				bone10.setRotationPoint(-2.0F, -3.5F, 1.0F);
				hair.addChild(bone10);
				setRotationAngle(bone10, -0.9599F, -0.0873F, -1.309F);
				bone10.cubeList.add(new ModelBox(bone10, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, false));
		
				bone16 = new ModelRenderer(this);
				bone16.setRotationPoint(2.0F, -3.5F, 1.0F);
				hair.addChild(bone16);
				setRotationAngle(bone16, -0.9599F, 0.0873F, 1.309F);
				bone16.cubeList.add(new ModelBox(bone16, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, true));
		
				bone4 = new ModelRenderer(this);
				bone4.setRotationPoint(0.0F, -5.0F, -2.0F);
				hair.addChild(bone4);
				setRotationAngle(bone4, 0.5236F, 0.0F, 0.0F);
				bone4.cubeList.add(new ModelBox(bone4, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, false));
		
				bone13 = new ModelRenderer(this);
				bone13.setRotationPoint(0.0F, -6.0F, -2.0F);
				hair.addChild(bone13);
				setRotationAngle(bone13, 0.0873F, 0.0F, 0.0F);
				bone13.cubeList.add(new ModelBox(bone13, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, true));
		
				bone14 = new ModelRenderer(this);
				bone14.setRotationPoint(0.0F, -6.0F, 0.0F);
				hair.addChild(bone14);
				setRotationAngle(bone14, -0.2618F, 0.0F, 0.0F);
				bone14.cubeList.add(new ModelBox(bone14, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, false));
		
				bone5 = new ModelRenderer(this);
				bone5.setRotationPoint(0.0F, -5.0F, 2.0F);
				hair.addChild(bone5);
				setRotationAngle(bone5, -0.7854F, 0.0F, 0.0F);
				bone5.cubeList.add(new ModelBox(bone5, 24, 0, -2.0F, -4.0F, -2.0F, 4, 4, 4, -0.1F, false));
		
				headStage1 = new ModelRenderer(this);
				headStage1.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHead.addChild(headStage1);
				headStage1.cubeList.add(new ModelBox(headStage1, 0, 64, -8.0F, -12.0F, -8.0F, 16, 16, 16, -3.95F, false));
		
				headStage2 = new ModelRenderer(this);
				headStage2.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHead.addChild(headStage2);
				headStage2.cubeList.add(new ModelBox(headStage2, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.1F, false));
		
				eyeRight = new ModelRenderer(this);
				eyeRight.setRotationPoint(0.5F, 0.3F, -2.25F);
				headStage2.addChild(eyeRight);
				eyeRight.cubeList.add(new ModelBox(eyeRight, 0, 55, -6.0F, -6.95F, -5.05F, 7, 7, 0, -3.15F, false));
		
				eyeLeft = new ModelRenderer(this);
				eyeLeft.setRotationPoint(-0.5F, 0.3F, -2.25F);
				headStage2.addChild(eyeLeft);
				eyeLeft.cubeList.add(new ModelBox(eyeLeft, 0, 55, -1.0F, -6.95F, -5.05F, 7, 7, 0, -3.15F, true));
		
				bipedHeadwear = new ModelRenderer(this);
				bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedHeadwear.cubeList.add(new ModelBox(bipedHeadwear, 16, 40, -4.0F, -8.0F, -4.0F, 8, 8, 1, 0.4F, false));
		
				bipedBody = new ModelRenderer(this);
				bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
				
		
				bodyStage1 = new ModelRenderer(this);
				bodyStage1.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedBody.addChild(bodyStage1);
				bodyStage1.cubeList.add(new ModelBox(bodyStage1, 40, 42, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.05F, false));
		
				bodyStage2 = new ModelRenderer(this);
				bodyStage2.setRotationPoint(0.0F, 0.0F, 0.0F);
				bipedBody.addChild(bodyStage2);
				bodyStage2.cubeList.add(new ModelBox(bodyStage2, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.11F, false));
				bodyStage2.cubeList.add(new ModelBox(bodyStage2, 16, 32, -4.0F, 0.0F, -2.0F, 8, 4, 4, 0.34F, false));
		
				exhaust1 = new ModelRenderer(this);
				exhaust1.setRotationPoint(-2.0F, 3.0F, 0.25F);
				bodyStage2.addChild(exhaust1);
				setRotationAngle(exhaust1, -1.0472F, -0.7854F, 0.0F);
				exhaust1.cubeList.add(new ModelBox(exhaust1, 0, 43, -2.0F, -6.0F, -2.0F, 4, 6, 4, 0.0F, true));
		
				bone33 = new ModelRenderer(this);
				bone33.setRotationPoint(2.0F, -5.9F, 2.1F);
				exhaust1.addChild(bone33);
				setRotationAngle(bone33, 0.5236F, 0.0F, 0.0F);
				bone33.cubeList.add(new ModelBox(bone33, 0, 43, -4.0F, -4.0F, -4.0F, 4, 4, 4, -0.1F, true));
		
				bone47 = new ModelRenderer(this);
				bone47.setRotationPoint(0.1F, -3.8F, 0.2F);
				bone33.addChild(bone47);
				setRotationAngle(bone47, 0.3491F, 0.0F, 0.0F);
				bone47.cubeList.add(new ModelBox(bone47, 0, 43, -4.0F, -4.0F, -4.0F, 4, 4, 4, -0.2F, true));
		
				exhaustExtension1 = new ModelRenderer(this);
				exhaustExtension1.setRotationPoint(-2.0F, -3.3F, 0.0F);
				bone47.addChild(exhaustExtension1);
				setRotationAngle(exhaustExtension1, 0.3491F, 0.1745F, -0.3491F);
				exhaustExtension1.cubeList.add(new ModelBox(exhaustExtension1, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.0F, true));
		
				bone67 = new ModelRenderer(this);
				bone67.setRotationPoint(0.0F, -3.25F, 0.0F);
				exhaustExtension1.addChild(bone67);
				setRotationAngle(bone67, 0.3491F, 0.0873F, -0.2618F);
				bone67.cubeList.add(new ModelBox(bone67, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.2F, true));
		
				bone69 = new ModelRenderer(this);
				bone69.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone67.addChild(bone69);
				setRotationAngle(bone69, 0.3491F, 0.0F, -0.2618F);
				bone69.cubeList.add(new ModelBox(bone69, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.4F, true));
		
				bone70 = new ModelRenderer(this);
				bone70.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone69.addChild(bone70);
				setRotationAngle(bone70, 0.3491F, -0.0873F, -0.2618F);
				bone70.cubeList.add(new ModelBox(bone70, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.6F, true));
		
				bone71 = new ModelRenderer(this);
				bone71.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone70.addChild(bone71);
				setRotationAngle(bone71, 0.1745F, 0.0F, -0.1745F);
				bone71.cubeList.add(new ModelBox(bone71, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.8F, true));
		
				ball1 = new ModelRenderer(this);
				ball1.setRotationPoint(0.0F, -8.0F, -2.0F);
				bone71.addChild(ball1);
				
		
				exhaust2 = new ModelRenderer(this);
				exhaust2.setRotationPoint(2.0F, 3.0F, 0.25F);
				bodyStage2.addChild(exhaust2);
				setRotationAngle(exhaust2, -1.0472F, 0.7854F, 0.0F);
				exhaust2.cubeList.add(new ModelBox(exhaust2, 0, 43, -2.0F, -6.0F, -2.0F, 4, 6, 4, 0.0F, false));
		
				bone30 = new ModelRenderer(this);
				bone30.setRotationPoint(-2.0F, -5.9F, 2.1F);
				exhaust2.addChild(bone30);
				setRotationAngle(bone30, 0.5236F, 0.0F, 0.0F);
				bone30.cubeList.add(new ModelBox(bone30, 0, 43, 0.0F, -4.0F, -4.0F, 4, 4, 4, -0.1F, false));
		
				bone31 = new ModelRenderer(this);
				bone31.setRotationPoint(-0.1F, -3.8F, 0.2F);
				bone30.addChild(bone31);
				setRotationAngle(bone31, 0.3491F, 0.0F, 0.0F);
				bone31.cubeList.add(new ModelBox(bone31, 0, 43, 0.0F, -4.0F, -4.0F, 4, 4, 4, -0.2F, false));
		
				exhaustExtension2 = new ModelRenderer(this);
				exhaustExtension2.setRotationPoint(2.0F, -3.3F, 0.0F);
				bone31.addChild(exhaustExtension2);
				setRotationAngle(exhaustExtension2, 0.3491F, -0.1745F, 0.3491F);
				exhaustExtension2.cubeList.add(new ModelBox(exhaustExtension2, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.0F, false));
		
				bone99 = new ModelRenderer(this);
				bone99.setRotationPoint(0.0F, -3.25F, 0.0F);
				exhaustExtension2.addChild(bone99);
				setRotationAngle(bone99, 0.3491F, -0.0873F, 0.2618F);
				bone99.cubeList.add(new ModelBox(bone99, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.2F, false));
		
				bone100 = new ModelRenderer(this);
				bone100.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone99.addChild(bone100);
				setRotationAngle(bone100, 0.3491F, 0.0F, 0.2618F);
				bone100.cubeList.add(new ModelBox(bone100, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.4F, false));
		
				bone101 = new ModelRenderer(this);
				bone101.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone100.addChild(bone101);
				setRotationAngle(bone101, 0.3491F, 0.0873F, 0.2618F);
				bone101.cubeList.add(new ModelBox(bone101, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.6F, false));
		
				bone102 = new ModelRenderer(this);
				bone102.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone101.addChild(bone102);
				setRotationAngle(bone102, 0.1745F, 0.0F, 0.1745F);
				bone102.cubeList.add(new ModelBox(bone102, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.8F, false));
		
				ball2 = new ModelRenderer(this);
				ball2.setRotationPoint(0.0F, -8.0F, -2.0F);
				bone102.addChild(ball2);
				
		
				exhaust3 = new ModelRenderer(this);
				exhaust3.setRotationPoint(-2.0F, 3.0F, 0.25F);
				bodyStage2.addChild(exhaust3);
				setRotationAngle(exhaust3, -1.9199F, -0.6109F, 0.0F);
				exhaust3.cubeList.add(new ModelBox(exhaust3, 0, 43, -2.0F, -6.0F, -2.0F, 4, 6, 4, 0.0F, true));
		
				bone74 = new ModelRenderer(this);
				bone74.setRotationPoint(2.0F, -5.9F, 2.1F);
				exhaust3.addChild(bone74);
				setRotationAngle(bone74, 0.5236F, 0.0F, -0.2618F);
				bone74.cubeList.add(new ModelBox(bone74, 0, 43, -4.0F, -4.0F, -4.0F, 4, 4, 4, -0.1F, true));
		
				bone75 = new ModelRenderer(this);
				bone75.setRotationPoint(0.1F, -3.8F, 0.2F);
				bone74.addChild(bone75);
				setRotationAngle(bone75, 0.3478F, 0.0298F, -0.3438F);
				bone75.cubeList.add(new ModelBox(bone75, 0, 43, -4.0F, -4.0F, -4.0F, 4, 4, 4, -0.2F, true));
		
				exhaustExtension3 = new ModelRenderer(this);
				exhaustExtension3.setRotationPoint(-2.0F, -3.3F, 0.0F);
				bone75.addChild(exhaustExtension3);
				setRotationAngle(exhaustExtension3, 0.1745F, 0.1745F, -0.2618F);
				exhaustExtension3.cubeList.add(new ModelBox(exhaustExtension3, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.0F, true));
		
				bone77 = new ModelRenderer(this);
				bone77.setRotationPoint(0.0F, -3.25F, 0.0F);
				exhaustExtension3.addChild(bone77);
				setRotationAngle(bone77, 0.2618F, 0.0873F, -0.2618F);
				bone77.cubeList.add(new ModelBox(bone77, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.2F, true));
		
				bone78 = new ModelRenderer(this);
				bone78.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone77.addChild(bone78);
				setRotationAngle(bone78, 0.2641F, -0.0183F, -0.3979F);
				bone78.cubeList.add(new ModelBox(bone78, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.4F, true));
		
				bone79 = new ModelRenderer(this);
				bone79.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone78.addChild(bone79);
				setRotationAngle(bone79, 0.1742F, 0.0076F, -0.6105F);
				bone79.cubeList.add(new ModelBox(bone79, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.6F, true));
		
				bone80 = new ModelRenderer(this);
				bone80.setRotationPoint(0.0F, -4.5F, 0.0F);
				bone79.addChild(bone80);
				setRotationAngle(bone80, 0.1743F, 0.0113F, -0.3039F);
				bone80.cubeList.add(new ModelBox(bone80, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.8F, true));
		
				ball3 = new ModelRenderer(this);
				ball3.setRotationPoint(0.0F, -8.0F, -2.0F);
				bone80.addChild(ball3);
				
		
				exhaust4 = new ModelRenderer(this);
				exhaust4.setRotationPoint(2.0F, 3.0F, 0.25F);
				bodyStage2.addChild(exhaust4);
				setRotationAngle(exhaust4, -1.9199F, 0.6109F, 0.0F);
				exhaust4.cubeList.add(new ModelBox(exhaust4, 0, 43, -2.0F, -6.0F, -2.0F, 4, 6, 4, 0.0F, false));
		
				bone84 = new ModelRenderer(this);
				bone84.setRotationPoint(-2.0F, -5.9F, 2.1F);
				exhaust4.addChild(bone84);
				setRotationAngle(bone84, 0.5236F, 0.0F, 0.2618F);
				bone84.cubeList.add(new ModelBox(bone84, 0, 43, 0.0F, -4.0F, -4.0F, 4, 4, 4, -0.1F, false));
		
				bone90 = new ModelRenderer(this);
				bone90.setRotationPoint(-0.1F, -3.8F, 0.2F);
				bone84.addChild(bone90);
				setRotationAngle(bone90, 0.3478F, -0.0298F, 0.3438F);
				bone90.cubeList.add(new ModelBox(bone90, 0, 43, 0.0F, -4.0F, -4.0F, 4, 4, 4, -0.2F, false));
		
				exhaustExtension4 = new ModelRenderer(this);
				exhaustExtension4.setRotationPoint(2.0F, -3.3F, 0.0F);
				bone90.addChild(exhaustExtension4);
				setRotationAngle(exhaustExtension4, 0.1745F, -0.1745F, 0.2618F);
				exhaustExtension4.cubeList.add(new ModelBox(exhaustExtension4, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.0F, false));
		
				bone93 = new ModelRenderer(this);
				bone93.setRotationPoint(0.0F, -3.25F, 0.0F);
				exhaustExtension4.addChild(bone93);
				setRotationAngle(bone93, 0.2618F, -0.0873F, 0.2618F);
				bone93.cubeList.add(new ModelBox(bone93, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.2F, false));
		
				bone98 = new ModelRenderer(this);
				bone98.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone93.addChild(bone98);
				setRotationAngle(bone98, 0.2641F, 0.0183F, 0.3979F);
				bone98.cubeList.add(new ModelBox(bone98, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.4F, false));
		
				bone103 = new ModelRenderer(this);
				bone103.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone98.addChild(bone103);
				setRotationAngle(bone103, 0.1742F, -0.0076F, 0.6105F);
				bone103.cubeList.add(new ModelBox(bone103, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.6F, false));
		
				bone104 = new ModelRenderer(this);
				bone104.setRotationPoint(0.0F, -4.5F, 0.0F);
				bone103.addChild(bone104);
				setRotationAngle(bone104, 0.1743F, -0.0113F, 0.3039F);
				bone104.cubeList.add(new ModelBox(bone104, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.8F, false));
		
				ball4 = new ModelRenderer(this);
				ball4.setRotationPoint(0.0F, -8.0F, -2.0F);
				bone104.addChild(ball4);
				
		
				exhaust5 = new ModelRenderer(this);
				exhaust5.setRotationPoint(-2.0F, 3.0F, 0.25F);
				bodyStage2.addChild(exhaust5);
				setRotationAngle(exhaust5, -2.618F, -0.2618F, 0.0F);
				exhaust5.cubeList.add(new ModelBox(exhaust5, 0, 43, -2.0F, -6.0F, -2.0F, 4, 6, 4, 0.0F, true));
		
				bone82 = new ModelRenderer(this);
				bone82.setRotationPoint(2.25F, -5.9F, 2.1F);
				exhaust5.addChild(bone82);
				setRotationAngle(bone82, 0.5087F, 0.1298F, -0.228F);
				bone82.cubeList.add(new ModelBox(bone82, 0, 43, -4.0F, -4.0F, -4.0F, 4, 4, 4, -0.1F, true));
		
				bone83 = new ModelRenderer(this);
				bone83.setRotationPoint(-2.0F, -3.55F, -2.0F);
				bone82.addChild(bone83);
				setRotationAngle(bone83, 0.3491F, 0.1658F, -0.1658F);
				bone83.cubeList.add(new ModelBox(bone83, 0, 43, -2.0F, -3.25F, -2.0F, 4, 4, 4, -0.2F, true));
		
				exhaustExtension5 = new ModelRenderer(this);
				exhaustExtension5.setRotationPoint(0.0F, -2.55F, 2.0F);
				bone83.addChild(exhaustExtension5);
				setRotationAngle(exhaustExtension5, 0.3245F, 0.2178F, -0.475F);
				exhaustExtension5.cubeList.add(new ModelBox(exhaustExtension5, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.0F, true));
		
				bone85 = new ModelRenderer(this);
				bone85.setRotationPoint(0.0F, -3.25F, 0.0F);
				exhaustExtension5.addChild(bone85);
				setRotationAngle(bone85, 0.2126F, 0.1339F, -0.2903F);
				bone85.cubeList.add(new ModelBox(bone85, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.2F, true));
		
				bone87 = new ModelRenderer(this);
				bone87.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone85.addChild(bone87);
				setRotationAngle(bone87, 0.0805F, 0.1428F, -0.3239F);
				bone87.cubeList.add(new ModelBox(bone87, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.4F, true));
		
				bone88 = new ModelRenderer(this);
				bone88.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone87.addChild(bone88);
				setRotationAngle(bone88, 0.1815F, 0.0499F, -0.6301F);
				bone88.cubeList.add(new ModelBox(bone88, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.6F, true));
		
				bone89 = new ModelRenderer(this);
				bone89.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone88.addChild(bone89);
				setRotationAngle(bone89, 0.2601F, 0.0887F, -0.4182F);
				bone89.cubeList.add(new ModelBox(bone89, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.8F, true));
		
				bone81 = new ModelRenderer(this);
				bone81.setRotationPoint(0.0F, -5.0F, 0.0F);
				bone89.addChild(bone81);
				setRotationAngle(bone81, 0.1478F, -0.0058F, -0.169F);
				bone81.cubeList.add(new ModelBox(bone81, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.8F, true));
		
				ball5 = new ModelRenderer(this);
				ball5.setRotationPoint(0.0F, -8.0F, -2.0F);
				bone81.addChild(ball5);
				
		
				exhaust6 = new ModelRenderer(this);
				exhaust6.setRotationPoint(2.0F, 3.0F, 0.25F);
				bodyStage2.addChild(exhaust6);
				setRotationAngle(exhaust6, -2.618F, 0.2618F, 0.0F);
				exhaust6.cubeList.add(new ModelBox(exhaust6, 0, 43, -2.0F, -6.0F, -2.0F, 4, 6, 4, 0.0F, false));
		
				bone64 = new ModelRenderer(this);
				bone64.setRotationPoint(-2.25F, -5.9F, 2.1F);
				exhaust6.addChild(bone64);
				setRotationAngle(bone64, 0.5087F, -0.1298F, 0.228F);
				bone64.cubeList.add(new ModelBox(bone64, 0, 43, 0.0F, -4.0F, -4.0F, 4, 4, 4, -0.1F, false));
		
				bone65 = new ModelRenderer(this);
				bone65.setRotationPoint(2.0F, -3.55F, -2.0F);
				bone64.addChild(bone65);
				setRotationAngle(bone65, 0.3491F, -0.1658F, 0.1658F);
				bone65.cubeList.add(new ModelBox(bone65, 0, 43, -2.0F, -3.25F, -2.0F, 4, 4, 4, -0.2F, false));
		
				exhaustExtension6 = new ModelRenderer(this);
				exhaustExtension6.setRotationPoint(0.0F, -2.55F, 2.0F);
				bone65.addChild(exhaustExtension6);
				setRotationAngle(exhaustExtension6, 0.3245F, -0.2178F, 0.475F);
				exhaustExtension6.cubeList.add(new ModelBox(exhaustExtension6, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.0F, false));
		
				bone66 = new ModelRenderer(this);
				bone66.setRotationPoint(0.0F, -3.25F, 0.0F);
				exhaustExtension6.addChild(bone66);
				setRotationAngle(bone66, 0.2126F, -0.1339F, 0.2903F);
				bone66.cubeList.add(new ModelBox(bone66, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.2F, false));
		
				bone68 = new ModelRenderer(this);
				bone68.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone66.addChild(bone68);
				setRotationAngle(bone68, 0.0805F, -0.1428F, 0.3239F);
				bone68.cubeList.add(new ModelBox(bone68, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.4F, false));
		
				bone73 = new ModelRenderer(this);
				bone73.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone68.addChild(bone73);
				setRotationAngle(bone73, 0.1815F, -0.0499F, 0.6301F);
				bone73.cubeList.add(new ModelBox(bone73, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.6F, false));
		
				bone76 = new ModelRenderer(this);
				bone76.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone73.addChild(bone76);
				setRotationAngle(bone76, 0.2601F, -0.0887F, 0.4182F);
				bone76.cubeList.add(new ModelBox(bone76, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.8F, false));
		
				bone105 = new ModelRenderer(this);
				bone105.setRotationPoint(0.0F, -5.0F, 0.0F);
				bone76.addChild(bone105);
				setRotationAngle(bone105, 0.1478F, 0.0058F, 0.169F);
				bone105.cubeList.add(new ModelBox(bone105, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.8F, false));
		
				ball6 = new ModelRenderer(this);
				ball6.setRotationPoint(0.0F, -8.0F, -2.0F);
				bone105.addChild(ball6);
				
		
				exhaust7 = new ModelRenderer(this);
				exhaust7.setRotationPoint(-2.0F, 7.0F, 0.25F);
				bodyStage2.addChild(exhaust7);
				setRotationAngle(exhaust7, -2.618F, -0.2618F, 0.0F);
				exhaust7.cubeList.add(new ModelBox(exhaust7, 0, 43, -2.0F, -6.0F, -2.0F, 4, 6, 4, 0.0F, true));
		
				bone91 = new ModelRenderer(this);
				bone91.setRotationPoint(2.25F, -5.9F, 2.1F);
				exhaust7.addChild(bone91);
				setRotationAngle(bone91, 0.5087F, 0.1298F, -0.228F);
				bone91.cubeList.add(new ModelBox(bone91, 0, 43, -4.0F, -4.0F, -4.0F, 4, 4, 4, -0.1F, true));
		
				bone92 = new ModelRenderer(this);
				bone92.setRotationPoint(-1.75F, -3.1F, -1.8F);
				bone91.addChild(bone92);
				setRotationAngle(bone92, 0.2443F, 0.1134F, -0.3316F);
				bone92.cubeList.add(new ModelBox(bone92, 0, 43, -2.0F, -3.45F, -2.0F, 4, 4, 4, -0.2F, true));
		
				exhaustExtension7 = new ModelRenderer(this);
				exhaustExtension7.setRotationPoint(0.25F, -2.5F, 2.0F);
				bone92.addChild(exhaustExtension7);
				setRotationAngle(exhaustExtension7, -0.0436F, 0.1745F, -0.3491F);
				exhaustExtension7.cubeList.add(new ModelBox(exhaustExtension7, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.0F, true));
		
				bone94 = new ModelRenderer(this);
				bone94.setRotationPoint(0.25F, -3.0F, 0.0F);
				exhaustExtension7.addChild(bone94);
				setRotationAngle(bone94, -0.0322F, 0.0892F, -0.612F);
				bone94.cubeList.add(new ModelBox(bone94, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.2F, true));
		
				bone95 = new ModelRenderer(this);
				bone95.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone94.addChild(bone95);
				setRotationAngle(bone95, -0.0027F, 0.0111F, -0.5668F);
				bone95.cubeList.add(new ModelBox(bone95, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.4F, true));
		
				bone96 = new ModelRenderer(this);
				bone96.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone95.addChild(bone96);
				setRotationAngle(bone96, 0.0169F, -0.0701F, -0.4798F);
				bone96.cubeList.add(new ModelBox(bone96, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.6F, true));
		
				bone97 = new ModelRenderer(this);
				bone97.setRotationPoint(0.0F, -4.5F, 0.0F);
				bone96.addChild(bone97);
				setRotationAngle(bone97, 0.001F, 0.0227F, -0.3481F);
				bone97.cubeList.add(new ModelBox(bone97, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.8F, true));
		
				ball7 = new ModelRenderer(this);
				ball7.setRotationPoint(0.0F, -8.0F, -2.0F);
				bone97.addChild(ball7);
				
		
				exhaust8 = new ModelRenderer(this);
				exhaust8.setRotationPoint(2.0F, 7.0F, 0.25F);
				bodyStage2.addChild(exhaust8);
				setRotationAngle(exhaust8, -2.618F, 0.2618F, 0.0F);
				exhaust8.cubeList.add(new ModelBox(exhaust8, 0, 43, -2.0F, -6.0F, -2.0F, 4, 6, 4, 0.0F, false));
		
				bone58 = new ModelRenderer(this);
				bone58.setRotationPoint(-2.25F, -5.9F, 2.1F);
				exhaust8.addChild(bone58);
				setRotationAngle(bone58, 0.5087F, -0.1298F, 0.228F);
				bone58.cubeList.add(new ModelBox(bone58, 0, 43, 0.0F, -4.0F, -4.0F, 4, 4, 4, -0.1F, false));
		
				bone59 = new ModelRenderer(this);
				bone59.setRotationPoint(1.75F, -3.1F, -1.8F);
				bone58.addChild(bone59);
				setRotationAngle(bone59, 0.2443F, -0.1134F, 0.3316F);
				bone59.cubeList.add(new ModelBox(bone59, 0, 43, -2.0F, -3.45F, -2.0F, 4, 4, 4, -0.2F, false));
		
				exhaustExtension8 = new ModelRenderer(this);
				exhaustExtension8.setRotationPoint(-0.25F, -2.5F, 2.0F);
				bone59.addChild(exhaustExtension8);
				setRotationAngle(exhaustExtension8, -0.0436F, -0.1745F, 0.3491F);
				exhaustExtension8.cubeList.add(new ModelBox(exhaustExtension8, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.0F, false));
		
				bone60 = new ModelRenderer(this);
				bone60.setRotationPoint(-0.25F, -3.0F, 0.0F);
				exhaustExtension8.addChild(bone60);
				setRotationAngle(bone60, -0.0322F, -0.0892F, 0.612F);
				bone60.cubeList.add(new ModelBox(bone60, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.2F, false));
		
				bone61 = new ModelRenderer(this);
				bone61.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone60.addChild(bone61);
				setRotationAngle(bone61, -0.0027F, -0.0111F, 0.5668F);
				bone61.cubeList.add(new ModelBox(bone61, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.4F, false));
		
				bone62 = new ModelRenderer(this);
				bone62.setRotationPoint(0.0F, -3.5F, 0.0F);
				bone61.addChild(bone62);
				setRotationAngle(bone62, 0.0169F, 0.0701F, 0.4798F);
				bone62.cubeList.add(new ModelBox(bone62, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.6F, false));
		
				bone63 = new ModelRenderer(this);
				bone63.setRotationPoint(0.0F, -4.5F, 0.0F);
				bone62.addChild(bone63);
				setRotationAngle(bone63, 0.001F, -0.0227F, 0.3481F);
				bone63.cubeList.add(new ModelBox(bone63, 0, 43, -2.0F, -4.0F, -4.0F, 4, 4, 4, 0.8F, false));
		
				ball8 = new ModelRenderer(this);
				ball8.setRotationPoint(0.0F, -8.0F, -2.0F);
				bone63.addChild(ball8);
				
		
				bipedRightArm = new ModelRenderer(this);
				bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
				setRotationAngle(bipedRightArm, -0.3927F, 0.0F, 0.0F);
				bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 16, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.11F, false));
		
				rightArmSpikes = new ModelRenderer(this);
				rightArmSpikes.setRotationPoint(-1.0F, 6.0F, 0.0F);
				bipedRightArm.addChild(rightArmSpikes);
				
		
				bone129 = new ModelRenderer(this);
				bone129.setRotationPoint(0.25F, 4.5F, 0.0F);
				rightArmSpikes.addChild(bone129);
				setRotationAngle(bone129, 0.0F, 0.0F, -0.3491F);
				
		
				bone72 = new ModelRenderer(this);
				bone72.setRotationPoint(-2.0F, 1.0F, 0.0F);
				bone129.addChild(bone72);
				setRotationAngle(bone72, 0.0F, 0.0F, -0.3491F);
				
		
				bone25 = new ModelRenderer(this);
				bone25.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone72.addChild(bone25);
				setRotationAngle(bone25, 0.0F, 0.7854F, 0.0F);
				bone25.cubeList.add(new ModelBox(bone25, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone125 = new ModelRenderer(this);
				bone125.setRotationPoint(0.0F, 1.0F, -2.0F);
				bone129.addChild(bone125);
				setRotationAngle(bone125, 0.3491F, 0.0F, 0.0F);
				
		
				bone126 = new ModelRenderer(this);
				bone126.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone125.addChild(bone126);
				setRotationAngle(bone126, 0.0F, -0.7854F, 0.0F);
				bone126.cubeList.add(new ModelBox(bone126, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone127 = new ModelRenderer(this);
				bone127.setRotationPoint(0.0F, 1.0F, 2.0F);
				bone129.addChild(bone127);
				setRotationAngle(bone127, -0.3491F, 0.0F, 0.0F);
				
		
				bone128 = new ModelRenderer(this);
				bone128.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone127.addChild(bone128);
				setRotationAngle(bone128, 0.0F, 2.3562F, 0.0F);
				bone128.cubeList.add(new ModelBox(bone128, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone124 = new ModelRenderer(this);
				bone124.setRotationPoint(-1.5F, 1.0F, -1.5F);
				bone129.addChild(bone124);
				setRotationAngle(bone124, 0.2618F, 0.0F, -0.2618F);
				bone124.cubeList.add(new ModelBox(bone124, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone86 = new ModelRenderer(this);
				bone86.setRotationPoint(-1.5F, 1.0F, 1.5F);
				bone129.addChild(bone86);
				setRotationAngle(bone86, -1.5708F, 1.309F, -1.8326F);
				bone86.cubeList.add(new ModelBox(bone86, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone131 = new ModelRenderer(this);
				bone131.setRotationPoint(0.0F, 2.5F, 0.0F);
				rightArmSpikes.addChild(bone131);
				setRotationAngle(bone131, 0.0F, -0.3491F, -0.1745F);
				
		
				bone132 = new ModelRenderer(this);
				bone132.setRotationPoint(-2.0F, 1.0F, 0.0F);
				bone131.addChild(bone132);
				setRotationAngle(bone132, 0.0F, 0.0F, -0.3491F);
				
		
				bone133 = new ModelRenderer(this);
				bone133.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone132.addChild(bone133);
				setRotationAngle(bone133, 0.0F, 0.7854F, 0.0F);
				bone133.cubeList.add(new ModelBox(bone133, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone134 = new ModelRenderer(this);
				bone134.setRotationPoint(0.0F, 1.0F, -2.0F);
				bone131.addChild(bone134);
				setRotationAngle(bone134, 0.3491F, 0.0F, 0.0F);
				
		
				bone135 = new ModelRenderer(this);
				bone135.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone134.addChild(bone135);
				setRotationAngle(bone135, 0.0F, -0.7854F, 0.0F);
				bone135.cubeList.add(new ModelBox(bone135, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone136 = new ModelRenderer(this);
				bone136.setRotationPoint(0.0F, 1.0F, 2.0F);
				bone131.addChild(bone136);
				setRotationAngle(bone136, -0.3491F, 0.0F, 0.0F);
				
		
				bone137 = new ModelRenderer(this);
				bone137.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone136.addChild(bone137);
				setRotationAngle(bone137, 0.0F, 2.3562F, 0.0F);
				bone137.cubeList.add(new ModelBox(bone137, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone138 = new ModelRenderer(this);
				bone138.setRotationPoint(-1.5F, 1.0F, -1.5F);
				bone131.addChild(bone138);
				setRotationAngle(bone138, 0.2618F, 0.0F, -0.2618F);
				bone138.cubeList.add(new ModelBox(bone138, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone139 = new ModelRenderer(this);
				bone139.setRotationPoint(-1.5F, 1.0F, 1.5F);
				bone131.addChild(bone139);
				setRotationAngle(bone139, -1.5708F, 1.309F, -1.8326F);
				bone139.cubeList.add(new ModelBox(bone139, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone140 = new ModelRenderer(this);
				bone140.setRotationPoint(0.0F, 0.5F, 0.0F);
				rightArmSpikes.addChild(bone140);
				setRotationAngle(bone140, 0.0F, 0.0F, -0.0873F);
				
		
				bone141 = new ModelRenderer(this);
				bone141.setRotationPoint(-2.0F, 1.0F, 0.0F);
				bone140.addChild(bone141);
				setRotationAngle(bone141, 0.0F, 0.0F, -0.3491F);
				
		
				bone142 = new ModelRenderer(this);
				bone142.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone141.addChild(bone142);
				setRotationAngle(bone142, 0.0F, 0.7854F, 0.0F);
				bone142.cubeList.add(new ModelBox(bone142, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone143 = new ModelRenderer(this);
				bone143.setRotationPoint(0.0F, 1.0F, -2.0F);
				bone140.addChild(bone143);
				setRotationAngle(bone143, 0.3491F, 0.0F, 0.0F);
				
		
				bone144 = new ModelRenderer(this);
				bone144.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone143.addChild(bone144);
				setRotationAngle(bone144, 0.0F, -0.7854F, 0.0F);
				bone144.cubeList.add(new ModelBox(bone144, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone145 = new ModelRenderer(this);
				bone145.setRotationPoint(0.0F, 1.0F, 2.0F);
				bone140.addChild(bone145);
				setRotationAngle(bone145, -0.3491F, 0.0F, 0.0F);
				
		
				bone146 = new ModelRenderer(this);
				bone146.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone145.addChild(bone146);
				setRotationAngle(bone146, 0.0F, 2.3562F, 0.0F);
				bone146.cubeList.add(new ModelBox(bone146, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone147 = new ModelRenderer(this);
				bone147.setRotationPoint(-1.5F, 1.0F, -1.5F);
				bone140.addChild(bone147);
				setRotationAngle(bone147, 0.2618F, 0.0F, -0.2618F);
				bone147.cubeList.add(new ModelBox(bone147, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone148 = new ModelRenderer(this);
				bone148.setRotationPoint(-1.5F, 1.0F, 1.5F);
				bone140.addChild(bone148);
				setRotationAngle(bone148, -1.5708F, 1.309F, -1.8326F);
				bone148.cubeList.add(new ModelBox(bone148, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone150 = new ModelRenderer(this);
				bone150.setRotationPoint(0.0F, -1.5F, 0.0F);
				rightArmSpikes.addChild(bone150);
				setRotationAngle(bone150, 0.0F, 0.3491F, 0.0F);
				
		
				bone151 = new ModelRenderer(this);
				bone151.setRotationPoint(-2.0F, 1.0F, 0.0F);
				bone150.addChild(bone151);
				setRotationAngle(bone151, 0.0F, 0.0F, -0.3491F);
				
		
				bone152 = new ModelRenderer(this);
				bone152.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone151.addChild(bone152);
				setRotationAngle(bone152, 0.0F, 0.7854F, 0.0F);
				bone152.cubeList.add(new ModelBox(bone152, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone153 = new ModelRenderer(this);
				bone153.setRotationPoint(0.0F, 1.0F, -2.0F);
				bone150.addChild(bone153);
				setRotationAngle(bone153, 0.3491F, 0.0F, 0.0F);
				
		
				bone154 = new ModelRenderer(this);
				bone154.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone153.addChild(bone154);
				setRotationAngle(bone154, 0.0F, -0.7854F, 0.0F);
				bone154.cubeList.add(new ModelBox(bone154, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone157 = new ModelRenderer(this);
				bone157.setRotationPoint(-1.5F, 1.0F, -1.5F);
				bone150.addChild(bone157);
				setRotationAngle(bone157, 0.2618F, 0.0F, -0.2618F);
				bone157.cubeList.add(new ModelBox(bone157, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				bone158 = new ModelRenderer(this);
				bone158.setRotationPoint(-1.5F, 1.0F, 1.5F);
				bone150.addChild(bone158);
				setRotationAngle(bone158, -1.5708F, 1.309F, -1.8326F);
				bone158.cubeList.add(new ModelBox(bone158, 40, 32, 0.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, false));
		
				broadaxe = new ModelRenderer(this);
				broadaxe.setRotationPoint(-5.0F, 10.0F, 2.0F);
				bipedRightArm.addChild(broadaxe);
				setRotationAngle(broadaxe, 0.0F, 0.0F, -0.6981F);
				broadaxe.cubeList.add(new ModelBox(broadaxe, 18, 50, -3.0F, -6.0F, 0.0F, 6, 12, 0, 2.0F, false));
		
				armExhaust = new ModelRenderer(this);
				armExhaust.setRotationPoint(-3.5F, 2.5F, 0.0F);
				bipedRightArm.addChild(armExhaust);
				setRotationAngle(armExhaust, 0.0F, 0.0F, -0.1309F);
				
		
				bone106 = new ModelRenderer(this);
				bone106.setRotationPoint(0.5F, -0.5F, 0.0F);
				armExhaust.addChild(bone106);
				setRotationAngle(bone106, -0.0315F, -0.8124F, -0.501F);
				bone106.cubeList.add(new ModelBox(bone106, 0, 0, -1.0F, -6.0F, -1.0F, 2, 6, 2, 0.2F, false));
		
				bone109 = new ModelRenderer(this);
				bone109.setRotationPoint(1.0F, -2.5F, -1.0F);
				armExhaust.addChild(bone109);
				setRotationAngle(bone109, 0.3182F, -0.7925F, -0.6233F);
				bone109.cubeList.add(new ModelBox(bone109, 0, 0, -1.0F, -6.0F, -1.0F, 2, 6, 2, 0.2F, false));
		
				bone110 = new ModelRenderer(this);
				bone110.setRotationPoint(1.0F, -2.5F, 1.0F);
				armExhaust.addChild(bone110);
				setRotationAngle(bone110, -0.3864F, -0.9815F, -0.0904F);
				bone110.cubeList.add(new ModelBox(bone110, 0, 0, -1.0F, -6.0F, -1.0F, 2, 6, 2, 0.2F, false));
		
				bone107 = new ModelRenderer(this);
				bone107.setRotationPoint(1.0F, -0.5F, -1.0F);
				armExhaust.addChild(bone107);
				setRotationAngle(bone107, 0.6902F, -0.7106F, -1.0887F);
				bone107.cubeList.add(new ModelBox(bone107, 0, 0, -1.0F, -6.0F, -1.0F, 2, 6, 2, 0.2F, false));
		
				bone108 = new ModelRenderer(this);
				bone108.setRotationPoint(1.0F, -0.5F, 1.0F);
				armExhaust.addChild(bone108);
				setRotationAngle(bone108, -0.7646F, -0.8326F, 0.018F);
				bone108.cubeList.add(new ModelBox(bone108, 0, 0, -1.0F, -6.0F, -1.0F, 2, 6, 2, 0.2F, false));
		
				bipedLeftArm = new ModelRenderer(this);
				bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
				setRotationAngle(bipedLeftArm, 0.3927F, 0.0F, 0.0F);
				bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 16, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.11F, true));
		
				leftArmSpikes = new ModelRenderer(this);
				leftArmSpikes.setRotationPoint(1.0F, 6.0F, 0.0F);
				bipedLeftArm.addChild(leftArmSpikes);
				
		
				bone19 = new ModelRenderer(this);
				bone19.setRotationPoint(-0.25F, 4.5F, 0.0F);
				leftArmSpikes.addChild(bone19);
				setRotationAngle(bone19, 0.0F, 0.0F, 0.3491F);
				
		
				bone20 = new ModelRenderer(this);
				bone20.setRotationPoint(2.0F, 1.0F, 0.0F);
				bone19.addChild(bone20);
				setRotationAngle(bone20, 0.0F, 0.0F, 0.3491F);
				
		
				bone21 = new ModelRenderer(this);
				bone21.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone20.addChild(bone21);
				setRotationAngle(bone21, 0.0F, -0.7854F, 0.0F);
				bone21.cubeList.add(new ModelBox(bone21, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone22 = new ModelRenderer(this);
				bone22.setRotationPoint(0.0F, 1.0F, -2.0F);
				bone19.addChild(bone22);
				setRotationAngle(bone22, 0.3491F, 0.0F, 0.0F);
				
		
				bone23 = new ModelRenderer(this);
				bone23.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone22.addChild(bone23);
				setRotationAngle(bone23, 0.0F, 0.7854F, 0.0F);
				bone23.cubeList.add(new ModelBox(bone23, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone24 = new ModelRenderer(this);
				bone24.setRotationPoint(0.0F, 1.0F, 2.0F);
				bone19.addChild(bone24);
				setRotationAngle(bone24, -0.3491F, 0.0F, 0.0F);
				
		
				bone26 = new ModelRenderer(this);
				bone26.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone24.addChild(bone26);
				setRotationAngle(bone26, 0.0F, -2.3562F, 0.0F);
				bone26.cubeList.add(new ModelBox(bone26, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone27 = new ModelRenderer(this);
				bone27.setRotationPoint(1.5F, 1.0F, -1.5F);
				bone19.addChild(bone27);
				setRotationAngle(bone27, 0.2618F, 0.0F, 0.2618F);
				bone27.cubeList.add(new ModelBox(bone27, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone28 = new ModelRenderer(this);
				bone28.setRotationPoint(1.5F, 1.0F, 1.5F);
				bone19.addChild(bone28);
				setRotationAngle(bone28, -1.5708F, -1.309F, 1.8326F);
				bone28.cubeList.add(new ModelBox(bone28, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone29 = new ModelRenderer(this);
				bone29.setRotationPoint(0.0F, 2.5F, 0.0F);
				leftArmSpikes.addChild(bone29);
				setRotationAngle(bone29, 0.0F, 0.3491F, 0.1745F);
				
		
				bone32 = new ModelRenderer(this);
				bone32.setRotationPoint(2.0F, 1.0F, 0.0F);
				bone29.addChild(bone32);
				setRotationAngle(bone32, 0.0F, 0.0F, 0.3491F);
				
		
				bone34 = new ModelRenderer(this);
				bone34.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone32.addChild(bone34);
				setRotationAngle(bone34, 0.0F, -0.7854F, 0.0F);
				bone34.cubeList.add(new ModelBox(bone34, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone35 = new ModelRenderer(this);
				bone35.setRotationPoint(0.0F, 1.0F, -2.0F);
				bone29.addChild(bone35);
				setRotationAngle(bone35, 0.3491F, 0.0F, 0.0F);
				
		
				bone36 = new ModelRenderer(this);
				bone36.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone35.addChild(bone36);
				setRotationAngle(bone36, 0.0F, 0.7854F, 0.0F);
				bone36.cubeList.add(new ModelBox(bone36, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone37 = new ModelRenderer(this);
				bone37.setRotationPoint(0.0F, 1.0F, 2.0F);
				bone29.addChild(bone37);
				setRotationAngle(bone37, -0.3491F, 0.0F, 0.0F);
				
		
				bone38 = new ModelRenderer(this);
				bone38.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone37.addChild(bone38);
				setRotationAngle(bone38, 0.0F, -2.3562F, 0.0F);
				bone38.cubeList.add(new ModelBox(bone38, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone39 = new ModelRenderer(this);
				bone39.setRotationPoint(1.5F, 1.0F, -1.5F);
				bone29.addChild(bone39);
				setRotationAngle(bone39, 0.2618F, 0.0F, 0.2618F);
				bone39.cubeList.add(new ModelBox(bone39, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone40 = new ModelRenderer(this);
				bone40.setRotationPoint(1.5F, 1.0F, 1.5F);
				bone29.addChild(bone40);
				setRotationAngle(bone40, -1.5708F, -1.309F, 1.8326F);
				bone40.cubeList.add(new ModelBox(bone40, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone41 = new ModelRenderer(this);
				bone41.setRotationPoint(0.0F, 0.5F, 0.0F);
				leftArmSpikes.addChild(bone41);
				setRotationAngle(bone41, 0.0F, 0.0F, 0.0873F);
				
		
				bone42 = new ModelRenderer(this);
				bone42.setRotationPoint(2.0F, 1.0F, 0.0F);
				bone41.addChild(bone42);
				setRotationAngle(bone42, 0.0F, 0.0F, 0.3491F);
				
		
				bone43 = new ModelRenderer(this);
				bone43.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone42.addChild(bone43);
				setRotationAngle(bone43, 0.0F, -0.7854F, 0.0F);
				bone43.cubeList.add(new ModelBox(bone43, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone44 = new ModelRenderer(this);
				bone44.setRotationPoint(0.0F, 1.0F, -2.0F);
				bone41.addChild(bone44);
				setRotationAngle(bone44, 0.3491F, 0.0F, 0.0F);
				
		
				bone45 = new ModelRenderer(this);
				bone45.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone44.addChild(bone45);
				setRotationAngle(bone45, 0.0F, 0.7854F, 0.0F);
				bone45.cubeList.add(new ModelBox(bone45, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone46 = new ModelRenderer(this);
				bone46.setRotationPoint(0.0F, 1.0F, 2.0F);
				bone41.addChild(bone46);
				setRotationAngle(bone46, -0.3491F, 0.0F, 0.0F);
				
		
				bone48 = new ModelRenderer(this);
				bone48.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone46.addChild(bone48);
				setRotationAngle(bone48, 0.0F, -2.3562F, 0.0F);
				bone48.cubeList.add(new ModelBox(bone48, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone49 = new ModelRenderer(this);
				bone49.setRotationPoint(1.5F, 1.0F, -1.5F);
				bone41.addChild(bone49);
				setRotationAngle(bone49, 0.2618F, 0.0F, 0.2618F);
				bone49.cubeList.add(new ModelBox(bone49, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone50 = new ModelRenderer(this);
				bone50.setRotationPoint(1.5F, 1.0F, 1.5F);
				bone41.addChild(bone50);
				setRotationAngle(bone50, -1.5708F, -1.309F, 1.8326F);
				bone50.cubeList.add(new ModelBox(bone50, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone51 = new ModelRenderer(this);
				bone51.setRotationPoint(0.0F, -1.5F, 0.0F);
				leftArmSpikes.addChild(bone51);
				setRotationAngle(bone51, 0.0F, -0.3491F, 0.0F);
				
		
				bone52 = new ModelRenderer(this);
				bone52.setRotationPoint(2.0F, 1.0F, 0.0F);
				bone51.addChild(bone52);
				setRotationAngle(bone52, 0.0F, 0.0F, 0.3491F);
				
		
				bone53 = new ModelRenderer(this);
				bone53.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone52.addChild(bone53);
				setRotationAngle(bone53, 0.0F, -0.7854F, 0.0F);
				bone53.cubeList.add(new ModelBox(bone53, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone54 = new ModelRenderer(this);
				bone54.setRotationPoint(0.0F, 1.0F, -2.0F);
				bone51.addChild(bone54);
				setRotationAngle(bone54, 0.3491F, 0.0F, 0.0F);
				
		
				bone55 = new ModelRenderer(this);
				bone55.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone54.addChild(bone55);
				setRotationAngle(bone55, 0.0F, 0.7854F, 0.0F);
				bone55.cubeList.add(new ModelBox(bone55, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone56 = new ModelRenderer(this);
				bone56.setRotationPoint(1.5F, 1.0F, -1.5F);
				bone51.addChild(bone56);
				setRotationAngle(bone56, 0.2618F, 0.0F, 0.2618F);
				bone56.cubeList.add(new ModelBox(bone56, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bone57 = new ModelRenderer(this);
				bone57.setRotationPoint(1.5F, 1.0F, 1.5F);
				bone51.addChild(bone57);
				setRotationAngle(bone57, -1.5708F, -1.309F, 1.8326F);
				bone57.cubeList.add(new ModelBox(bone57, 40, 32, -4.0F, -6.0F, 0.0F, 4, 6, 4, 0.2F, true));
		
				bipedRightLeg = new ModelRenderer(this);
				bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
				setRotationAngle(bipedRightLeg, 0.3927F, 0.0F, 0.0F);
				bipedRightLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.1F, false));
				bipedRightLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 32, -2.0F, 0.0F, -2.0F, 4, 7, 4, 0.35F, false));
		
				bipedLeftLeg = new ModelRenderer(this);
				bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
				setRotationAngle(bipedLeftLeg, -0.3927F, 0.0F, 0.0F);
				bipedLeftLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.1F, true));
				bipedLeftLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 32, -2.0F, 0.0F, -2.0F, 4, 7, 4, 0.35F, true));

				broadaxe.showModel = false;
			}
		
			public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
				modelRenderer.rotateAngleX = x;
				modelRenderer.rotateAngleY = y;
				modelRenderer.rotateAngleZ = z;
			}

			@Override
			public void setVisible(boolean visible) {
				super.setVisible(visible);
				headStage0.showModel = visible;
				headStage1.showModel = visible;
				headStage2.showModel = visible;
				armExhaust.showModel = visible;
				bodyStage1.showModel = visible;
				bodyStage2.showModel = visible;
				exhaustExtension1.showModel = visible;
				exhaustExtension2.showModel = visible;
				exhaustExtension3.showModel = visible;
				exhaustExtension4.showModel = visible;
				exhaustExtension5.showModel = visible;
				exhaustExtension6.showModel = visible;
				exhaustExtension7.showModel = visible;
				exhaustExtension8.showModel = visible;
			}

			@Override
			public void setModelAttributes(ModelBase model) {
				super.setModelAttributes(model);
				if (model instanceof ModelBiped) {
					this.wearerModel = (ModelBiped)model;
				}
			}
	
			@Override
			public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
				if (entity instanceof AbstractClientPlayer && ((AbstractClientPlayer)entity).getSkinType().equals("slim")) {
					this.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
					this.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
				}
				super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
				if (!(entity instanceof AbstractClientPlayer) && this.wearerModel != null) {
					copyModelAngles(this.wearerModel.bipedLeftArm, this.bipedLeftArm);
					copyModelAngles(this.wearerModel.bipedRightArm, this.bipedRightArm);
					copyModelAngles(this.wearerModel.bipedLeftLeg, this.bipedLeftLeg);
					copyModelAngles(this.wearerModel.bipedRightLeg, this.bipedRightLeg);
				}
				float f6 = f2 - entity.getEntityData().getInteger(START_TIME);
				if (f6 <= 40F) {
					float gb = f6 >= 20F ? MathHelper.clamp((f6 - 20F) / 20F, 0.0F, 1.0F) : 0.0F;
					float a = MathHelper.clamp(f6 / 20F, 0F, 1.0F);
//System.out.println(">>>>>> f6="+f6+", gb="+gb+", a="+a);
					GlStateManager.enableBlend();
					GlStateManager.alphaFunc(0x204, 0.001f);
					GlStateManager.color(1.0F, gb, gb, a);
					GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
				}
			}
		}
	}
}
