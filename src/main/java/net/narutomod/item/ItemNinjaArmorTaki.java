
package net.narutomod.item;

import net.narutomod.creativetab.TabModTab;
import net.narutomod.ElementsNarutomodMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

@ElementsNarutomodMod.ModElement.Tag
public class ItemNinjaArmorTaki extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:ninja_armor_takihelmet")
	public static final Item helmet = null;

	public ItemNinjaArmorTaki(ElementsNarutomodMod instance) {
		super(instance, 938);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new ItemNinjaArmor.Base(ItemNinjaArmor.Type.TAKI, EntityEquipmentSlot.HEAD) {
			@Override
			protected ItemNinjaArmor.ArmorData setArmorData(ItemNinjaArmor.Type type, EntityEquipmentSlot slotIn) {
				return new Armor4Slot();
			}

			class Armor4Slot extends ItemNinjaArmor.ArmorData {
				@SideOnly(Side.CLIENT)
				@Override
				protected void init() {
					this.model = new ItemNinjaArmor.ModelNinjaArmor(ItemNinjaArmor.Type.TAKI);
					this.texture = "narutomod:textures/fishnetarmor.png";
				}
				@SideOnly(Side.CLIENT)
				@Override
				public void setSlotVisible(ItemStack stack, Entity entity, EntityEquipmentSlot slot) {
					this.model.bipedHeadwear.showModel = false;
					((ItemNinjaArmor.ModelNinjaArmor)this.model).headwear.showModel = !stack.hasTagCompound() || !stack.getTagCompound().getBoolean("noHeadClothe");
				}
			}
		}.setUnlocalizedName("ninja_armor_takihelmet").setRegistryName("ninja_armor_takihelmet").setCreativeTab(TabModTab.tab));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(helmet, 0, new ModelResourceLocation("narutomod:ninja_armor_takihelmet", "inventory"));
	}
}
