
package net.narutomod.item;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.SoundEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.init.Blocks;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.Block;

import net.narutomod.entity.EntityEarthSpears;
import net.narutomod.entity.EntitySwampPit;
import net.narutomod.entity.EntityEarthSandwich;
import net.narutomod.entity.EntityEarthGolem;
import net.narutomod.entity.EntityHidingInRock;
import net.narutomod.creativetab.TabModTab;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.procedure.ProcedureUtils;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@ElementsNarutomodMod.ModElement.Tag
public class ItemDoton extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:doton")
	public static final Item block = null;
	public static final int ENTITYID = 134;
	private static final List<Material> earthenMaterials = Arrays.asList(Material.GROUND, Material.ROCK, Material.SAND, Material.CLAY);
	public static final ItemJutsu.JutsuEnum HIDINGINROCK = new ItemJutsu.JutsuEnum(0, "hiding_in_rock", 'C', 10d, new EntityHidingInRock.EC.Jutsu());
	public static final ItemJutsu.JutsuEnum EARTHWALL = new ItemJutsu.JutsuEnum(1, "entityearthwall", 'B', 20d, new EntityEarthWall.Jutsu());
	public static final ItemJutsu.JutsuEnum SANDWICH = new ItemJutsu.JutsuEnum(2, "earth_sandwich", 'B', 100d, new EntityEarthSandwich.EC.Jutsu());
	public static final ItemJutsu.JutsuEnum SWAMPPIT = new ItemJutsu.JutsuEnum(3, "swamp_pit", 'A', 100d, new EntitySwampPit.EC.Jutsu());
	public static final ItemJutsu.JutsuEnum SPEARS = new ItemJutsu.JutsuEnum(4, "earth_spears", 'C', 20d, new EntityEarthSpears.EC.Jutsu());
	public static final ItemJutsu.JutsuEnum GOLEM = new ItemJutsu.JutsuEnum(5, "earth_golem", 'B', 100d, new EntityEarthGolem.EC.Jutsu());

	public ItemDoton(ElementsNarutomodMod instance) {
		super(instance, 378);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new RangedItem(HIDINGINROCK, EARTHWALL, SANDWICH, SWAMPPIT, SPEARS, GOLEM));
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityEarthWall.class)
				.id(new ResourceLocation("narutomod", "entityearthwall"), ENTITYID).name("entityearthwall").tracker(64, 1, true).build());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("narutomod:doton", "inventory"));
	}

	public static class RangedItem extends ItemJutsu.Base {
		public RangedItem(ItemJutsu.JutsuEnum... list) {
			super(ItemJutsu.JutsuEnum.Type.DOTON, list);
			this.setUnlocalizedName("doton");
			this.setRegistryName("doton");
			this.setCreativeTab(TabModTab.tab);
		}
	}

	public static boolean isEarthenMaterial(Material material) {
		return earthenMaterials.contains(material);
	}

	public static class EntityEarthWall extends Entity implements ItemJutsu.IJutsu {
		private final int blockChunk = 128;
		private final int duration = 200;
		private double wallHeight;
		private List<BlockPos> affectedList;
		private List<BlockPos> tempList;
		private final List<Template.BlockInfo> allBlocks;
		private boolean dieOnDone;
		private int vindex;
		private int removeTime;
		
		public EntityEarthWall(World a) {
			super(a);
			this.setSize(0.01f, 0.01f);
			this.affectedList = Lists.<BlockPos>newArrayList();
			this.allBlocks = Lists.<Template.BlockInfo>newArrayList();
			this.dieOnDone = true;
		}

		// x, y, z is the center point of the wall
		// yaw is the rotation angle the wall is facing
		// set autoIn to true to self kill after wall building finished
		public EntityEarthWall(World worldIn, double x, double y, double z, float yaw, double widthIn, double heightIn, double thickness, boolean autoIn) {
			this(worldIn);
			this.dieOnDone = autoIn;
			this.wallHeight = heightIn;
			if (thickness < 1d)
				thickness = 1d;
			thickness = (thickness - 1d) / 2d;
			widthIn /= 2d;
			this.setPosition(x, y, z);
			Vec3d vec = new Vec3d(x, y, z);
			Vec3d vec3d = vec.add(Vec3d.fromPitchYaw(0f, yaw - 90f).scale(widthIn));
			Vec3d vec3d1 = vec.add(Vec3d.fromPitchYaw(0f, yaw + 90f).scale(widthIn));
			for (AxisAlignedBB aabb : this.world.getCollisionBoxes(null, this.getEntityBoundingBox().grow(widthIn, this.wallHeight, widthIn))) {
				BlockPos pos = new BlockPos(ProcedureUtils.BB.getCenter(aabb));
				if (autoIn) {
					RayTraceResult r = aabb.grow(thickness, this.wallHeight, thickness).calculateIntercept(vec3d, vec3d1);
					if (r != null && this.isNeighborEarthenMaterial(pos) && this.world.getBlockState(pos.up()).getCollisionBoundingBox(this.world, pos.up()) == null) {// && this.world.isAirBlock(pos.up())) {
						this.affectedList.add(pos);
					}
				} else {
					RayTraceResult r = aabb.grow(thickness, 0d, thickness).expand(0d, this.wallHeight, 0d).calculateIntercept(vec3d, vec3d1);
					if (r != null && this.isNeighborEarthenMaterial(pos) && (aabb.maxY == y || !this.world.getBlockState(pos.up()).isTopSolid())) {
						this.affectedList.add(pos);
					}
				}
			}
			this.tempList = Lists.newArrayList();
		}

		public EntityEarthWall(World worldIn, double x, double y, double z, float yaw, double widthIn) {
			this(worldIn, x, y, z, yaw, widthIn, widthIn * 0.6d, widthIn * 0.25d, true);
		}

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.DOTON;
		}

		private boolean isNeighborEarthenMaterial(BlockPos pos) {
			return this.getNeightborEarthenBlock(pos).getBlock() != Blocks.AIR;
		}

		private IBlockState getNeightborEarthenBlock(BlockPos pos) {
			IBlockState bstate = Blocks.AIR.getDefaultState();
			if (isEarthenMaterial(this.world.getBlockState(pos).getMaterial())) {
				bstate = this.world.getBlockState(pos);
			} else for (EnumFacing face : EnumFacing.values()) {
				if (bstate.getBlock() == Blocks.AIR && isEarthenMaterial(this.world.getBlockState(pos.offset(face)).getMaterial()))
					bstate = this.world.getBlockState(pos.offset(face));
			}
			if (bstate.getBlock() instanceof BlockOre || bstate.getBlock() instanceof BlockRedstoneOre || bstate.getBlock() == Blocks.BEDROCK) {
				bstate = Blocks.STONE.getDefaultState();
			}
			return bstate;
		}

		@Override
		protected void entityInit() {
		}

		private void moveUpEntitiesInAABB(AxisAlignedBB aabb, double offset) {
			for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(null, aabb)) {
				entity.setPositionAndUpdate(entity.posX, entity.posY + offset + 1.5d, entity.posZ);
			}
		}

		@Override
		public void onUpdate() {
			if (!this.affectedList.isEmpty()) {
				if (this.ticksExisted % 30 == 1) {
					this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation(("narutomod:rocks"))), 
					 5.0f, (this.rand.nextFloat() * 0.5f) + 0.3f);
				}
				BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
				for (int i = 0; i < this.blockChunk && this.vindex < (int)this.wallHeight; ) {
					Iterator<BlockPos> iter = this.affectedList.iterator();
					while (i < this.blockChunk && iter.hasNext()) {
						pos.setPos(iter.next().up());
						iter.remove();
						this.tempList.add(pos.toImmutable());
						if (pos.getY() < 255) {
							this.moveUpEntitiesInAABB(new AxisAlignedBB(pos), 1d);
							IBlockState newState = this.getNeightborEarthenBlock(ProcedureUtils.getGroundBelow(this.world, pos));
							((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, pos.getX()+0.5d,
							 pos.getY(), pos.getZ()+0.5d, 5, 0D, 0D, 0D, 0.15D, Block.getIdFromBlock(newState.getBlock()));
							IBlockState oldState = this.world.getBlockState(pos);
							if (oldState.getCollisionBoundingBox(this.world, pos) == null) {
								this.allBlocks.add(new Template.BlockInfo(pos.toImmutable(), oldState, null));
								this.world.setBlockState(pos, newState, 3);
							}
							++i;
						}
					}
					if (this.affectedList.isEmpty()) {
						this.affectedList = Lists.newArrayList(this.tempList);
						this.tempList.clear();
						++this.vindex;
					}
				}
				pos.release();
				if (this.vindex >= (int)this.wallHeight) {
					this.affectedList.clear();
				}
			} else if (!this.world.isRemote && this.dieOnDone) {
				this.removeTime = 1200;
				this.setDead();
			}
		}

		@Override
		public void setDead() {
			super.setDead();
			if (!this.world.isRemote && !this.allBlocks.isEmpty()) {
				this.allBlocks.sort(new ProcedureUtils.BlockInfoSorter(this.getPosition().up((int)this.wallHeight * 2)));
				new net.narutomod.event.EventSetBlocks(this.world, this.allBlocks, this.world.getTotalWorldTime() + this.removeTime, 0, false, false);
				this.allBlocks.clear();				
			}
		}

		public boolean isBlockBreakable(BlockPos pos) {
			IBlockState blockstate = this.world.getBlockState(pos);
			float hardness = blockstate.getBlockHardness(this.world, pos);
			return !blockstate.isTopSolid() && (blockstate.getMaterial().isLiquid() || (hardness >= 0f && hardness <= 5.0f));
		}

		public boolean isDone() {
			return !this.world.isRemote && (this.affectedList.isEmpty() || this.vindex > (int)this.wallHeight);
		}

		//public List<BlockPos> getAllBlocks() {
		//	return this.allBlocks;
		//}

		@Override
		protected void readEntityFromNBT(NBTTagCompound compound) {
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound compound) {
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				if (power >= 5f) {
					RayTraceResult rt = ProcedureUtils.raytraceBlocks(entity, 30d);
					if (rt != null && rt.typeOfHit == RayTraceResult.Type.BLOCK) {
						entity.world.spawnEntity(new EntityEarthWall(
						  entity.world, rt.hitVec.x, rt.hitVec.y, rt.hitVec.z, entity.rotationYaw, (double)power));
						return true;
					}
				}
				return false;
			}

			@Override
			public float getBasePower() {
				return 2.0f;
			}
	
			@Override
			public float getPowerupDelay() {
				return 15.0f;
			}
	
			@Override
			public float getMaxPower() {
				return 50.0f;
			}
		}
	}
}
