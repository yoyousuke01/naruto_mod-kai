package net.narutomod.item;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.Item;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelBiped;

import net.narutomod.procedure.ProcedureAsuraPathArmorBodyTickEvent;
import net.narutomod.ElementsNarutomodMod;

import java.util.HashMap;
import java.util.UUID;
import com.google.common.collect.Multimap;

@ElementsNarutomodMod.ModElement.Tag
public class ItemAsuraPathArmor extends ElementsNarutomodMod.ModElement {
	@ObjectHolder("narutomod:asurapatharmorbody")
	public static final Item body = null;
	private static final AttributeModifier HEALTH_MODIFIER = new AttributeModifier(UUID.fromString("92f6ba90-c2b1-460d-a88e-5dd725be3c17"), "Asura bonus", 40d, 0);

	public ItemAsuraPathArmor(ElementsNarutomodMod instance) {
		super(instance, 210);
	}

	public void initElements() {
		ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial("ASURAPATHARMOR", "narutomod:sasuke_", 1024, new int[]{2, 5, 1024, 2}, 0, null,
				5.0F);
		this.elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.CHEST) {
			@SideOnly(Side.CLIENT)
			private ModelBiped armorModel;

			@SideOnly(Side.CLIENT)
			@Override
			public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
				if (this.armorModel == null) {
					this.armorModel = new ModelArmorCustom();
				}

				this.armorModel.isSneak = living.isSneaking();
				this.armorModel.isRiding = living.isRiding();
				this.armorModel.isChild = living.isChild();
				return this.armorModel;
			}

			@Override
			public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
				return "narutomod:textures/pain_asura.png";
			}

			@Override
			public void onArmorTick(World world, EntityPlayer entity, ItemStack itemstack) {
				int x = (int) entity.posX;
				int y = (int) entity.posY;
				int z = (int) entity.posZ;
				HashMap<Object, Object> $_dependencies = new HashMap<>();
				$_dependencies.put("entity", entity);
				$_dependencies.put("world", world);
				$_dependencies.put("itemstack", itemstack);
				ProcedureAsuraPathArmorBodyTickEvent.executeProcedure((HashMap) $_dependencies);
			}

			@Override
			public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
				Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
				if (equipmentSlot == EntityEquipmentSlot.CHEST) {
					multimap.put(SharedMonsterAttributes.MAX_HEALTH.getName(), HEALTH_MODIFIER);
				}
				return multimap;
			}

			@Override
			public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
				return false;
			}
		}.setUnlocalizedName("asurapatharmorbody").setRegistryName("asurapatharmorbody").setCreativeTab(null));
	}

	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(body, 0, new ModelResourceLocation("narutomod:asurapatharmorbody", "inventory"));
	}
	
	public static class ModelArmorCustom extends ModelBiped {
		//private final ModelRenderer bodywear;
		private final ModelRenderer tail;
		private final ModelRenderer tail2;
		private final ModelRenderer tail3;
		private final ModelRenderer tail4;
		private final ModelRenderer tail5;
		private final ModelRenderer tail6;
		//private final ModelRenderer rightArmwear;
		//private final ModelRenderer leftArmwear;
		private final ModelRenderer bipedRightArm1;
		private final ModelRenderer bipedRightArm2;
		private final ModelRenderer bipedLeftArm1;
		private final ModelRenderer bipedLeftArm2;

		public ModelArmorCustom() {
			this.textureWidth = 64;
			this.textureHeight = 64;
			
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
			setRotationAngle(bipedLeftArm, 0.3927F, 0.0F, 0.0F);
			bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 18, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, true));
			bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 34, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.2F, true));
		}

		public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
			modelRenderer.rotateAngleX = x;
			modelRenderer.rotateAngleY = y;
			modelRenderer.rotateAngleZ = z;
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
