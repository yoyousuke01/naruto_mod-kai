
package net.narutomod.item;

import net.narutomod.Chakra;
import net.narutomod.potion.PotionParalysis;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.creativetab.TabModTab;
import net.narutomod.ElementsNarutomodMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ModelBakeEvent;

import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.SoundEvents;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import com.google.common.collect.Multimap;

@ElementsNarutomodMod.ModElement.Tag
public class ItemTotsukaSword extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:totsuka_sword")
	public static final Item block = null;

	public ItemTotsukaSword(ElementsNarutomodMod instance) {
		super(instance, 549);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new ItemSword(EnumHelper.addToolMaterial("TOTSUKA_SWORD", 0, 0, 0f, 16f, 0)) {
			{
				new ClientInitBlock().init(this);
			}

			@Override
			public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
				Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(slot);
				if (slot == EntityEquipmentSlot.MAINHAND) {
					multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
							new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double) this.getAttackDamage(), 0));
					multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
							new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -3, 0));
					multimap.put(EntityPlayer.REACH_DISTANCE.getName(),
							new AttributeModifier(ProcedureUtils.REACH_MODIFIER, "Weapon modifier", 0.2d, 2));
				}
				return multimap;
			}

			public Set<String> getToolClasses(ItemStack stack) {
				HashMap<String, Integer> ret = new HashMap<String, Integer>();
				ret.put("sword", 0);
				return ret.keySet();
			}

			@Override
			public boolean hitEntity(ItemStack itemstack, EntityLivingBase entity, EntityLivingBase sourceentity) {
				super.hitEntity(itemstack, entity, sourceentity);
				entity.addPotionEffect(new PotionEffect(PotionParalysis.potion, 60, 0));
				Chakra.pathway(entity).consume(2000.0d);
				return true;
			}

			@Override
			public void onUpdate(ItemStack itemstack, World world, Entity entity, int slot, boolean isSelected) {
				super.onUpdate(itemstack, world, entity, slot, isSelected);
				if (isSelected && world.rand.nextFloat() < 0.05f) {
					world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.BLOCK_FIRE_AMBIENT,
					 net.minecraft.util.SoundCategory.NEUTRAL, 0.9f, world.rand.nextFloat() * 0.7f + 0.3f);
				}
			}
		}.setUnlocalizedName("totsuka_sword").setRegistryName("totsuka_sword").setCreativeTab(TabModTab.tab));
	}

	public static class InitializerBlock {
		public void init(Item item) { }
	}

	public static class ClientInitBlock extends InitializerBlock {
		@SideOnly(Side.CLIENT)
		@Override
		public void init(Item item) {
			item.setTileEntityItemStackRenderer(new FullbrightTEISR());
		}

		@SideOnly(Side.CLIENT)
		public class FullbrightTEISR extends TileEntityItemStackRenderer {
		    @Override
		    public void renderByItem(ItemStack stack, float partialTicks) {
	            Minecraft mc = Minecraft.getMinecraft();
	            RenderItem renderItem = mc.getRenderItem();
	            IBakedModel model = renderItem.getItemModelWithOverrides(stack, null, mc.player);
            	GlStateManager.disableLighting();
            	OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
		        Tessellator tessellator = Tessellator.getInstance();
		        BufferBuilder bufferbuilder = tessellator.getBuffer();
		        bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
		        for (EnumFacing enumfacing : EnumFacing.values()) {
		            renderItem.renderQuads(bufferbuilder, model.getQuads((IBlockState)null, enumfacing, 0L), -1, stack);
		        }
		        renderItem.renderQuads(bufferbuilder, model.getQuads((IBlockState)null, (EnumFacing)null, 0L), -1, stack);
		        tessellator.draw();
		        GlStateManager.enableLighting();
		    }
		}
	}

	@SideOnly(Side.CLIENT)
	public class FullbrightBakedModel implements IBakedModel {
	    private final IBakedModel baseModel;
	
	    public FullbrightBakedModel(IBakedModel original) {
	        this.baseModel = original;
	    }
	
	    @Override
	    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
	        return baseModel.getQuads(state, side, rand);
	    }
	
	    @Override
	    public boolean isAmbientOcclusion() {
	        return false;
	    }
	
	    @Override
	    public boolean isGui3d() {
	        return baseModel.isGui3d();
	    }
	
	    @Override
	    public boolean isBuiltInRenderer() {
	        return true; // tells Minecraft to call our renderer
	    }
	
	    @Override
	    public TextureAtlasSprite getParticleTexture() {
	        return baseModel.getParticleTexture();
	    }
	
	    @Override
	    public ItemCameraTransforms getItemCameraTransforms() {
	        return baseModel.getItemCameraTransforms();
	    }
	
	    @Override
	    public ItemOverrideList getOverrides() {
	        return ItemOverrideList.NONE;
	    }
	}

	private ModelResourceLocation modelLoc = new ModelResourceLocation("narutomod:totsuka_sword", "inventory");

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        IBakedModel original = event.getModelRegistry().getObject(this.modelLoc);
        if (original != null) {
            event.getModelRegistry().putObject(this.modelLoc, new FullbrightBakedModel(original));
        }
    }

	@SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent event) {
    	MinecraftForge.EVENT_BUS.register(this);
    }

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, this.modelLoc);
	}
}
