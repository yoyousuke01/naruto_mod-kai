
package net.narutomod.item;

import net.narutomod.creativetab.TabModTab;
import net.narutomod.ElementsNarutomodMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.EnumAction;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nullable;
import com.google.common.collect.Multimap;

@ElementsNarutomodMod.ModElement.Tag
public class ItemGunbai extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:gunbai")
	public static final Item block = null;
	public static final int ENTITYID = 389;
	private static final String USE_BLOCKING_MODEL = "UseBlockingModel";

	public ItemGunbai(ElementsNarutomodMod instance) {
		super(instance, 769);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new RangedItem());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
   	    ModelBakery.registerItemVariants(block,
   	     new ModelResourceLocation("narutomod:gunbai", "inventory"),
   	     new ModelResourceLocation("narutomod:gunbai_blocking", "inventory"));

	    ModelLoader.setCustomMeshDefinition(block, new ItemMeshDefinition() {
	        @Override
	        public ModelResourceLocation getModelLocation(ItemStack stack) {
	            if (stack.hasTagCompound() && stack.getTagCompound().getBoolean(USE_BLOCKING_MODEL)) {
	                return new ModelResourceLocation("narutomod:gunbai_blocking", "inventory");
	            }
	            return new ModelResourceLocation("narutomod:gunbai", "inventory");
	        }
	    });
	}

	public static class RangedItem extends Item implements ItemOnBody.Interface {
		public RangedItem() {
			super();
			setMaxDamage(2500);
			setFull3D();
			setUnlocalizedName("gunbai");
			setRegistryName("gunbai");
			maxStackSize = 1;
			setCreativeTab(TabModTab.tab);
		}

		@Override
		public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
			Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(slot);
			if (slot == EntityEquipmentSlot.MAINHAND) {
				multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
						new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Ranged item modifier", (double) 18, 0));
				multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
						new AttributeModifier(ATTACK_SPEED_MODIFIER, "Ranged item modifier", -2.4, 0));
			}
			return multimap;
		}

		@Override
		public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			if (entityIn instanceof EntityLivingBase && ((EntityLivingBase)entityIn).isHandActive()
			 && ((EntityLivingBase)entityIn).getActiveItemStack().getItem() == block) {
				if (worldIn.isRemote && !stack.getTagCompound().getBoolean(USE_BLOCKING_MODEL)) {
					stack.getTagCompound().setBoolean(USE_BLOCKING_MODEL, true);
				}
			} else if (stack.getTagCompound().hasKey(USE_BLOCKING_MODEL)) {
				stack.getTagCompound().removeTag(USE_BLOCKING_MODEL);
			}
		}

		@Override
		public boolean canDisableShield(ItemStack stack, ItemStack shield, EntityLivingBase entity, EntityLivingBase attacker) {
			return false;
		}

		@Override
		public boolean isShield(ItemStack stack, @Nullable EntityLivingBase entity) {
			return stack.getItem() == block;
		}

		@Override
		public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
			playerIn.setActiveHand(handIn);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
		}

		@Override
		public EnumAction getItemUseAction(ItemStack itemstack) {
			return EnumAction.BLOCK;
		}

		@Override
		public int getMaxItemUseDuration(ItemStack itemstack) {
			return 72000;
		}
	}
}
