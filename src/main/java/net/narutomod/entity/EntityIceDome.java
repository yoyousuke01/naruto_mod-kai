
package net.narutomod.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.item.ItemStack;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.procedure.ProcedureSync;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemHyoton;
import net.narutomod.Chakra;
import net.narutomod.NarutomodMod;
import net.narutomod.ElementsNarutomodMod;

import java.util.List;
import java.util.Iterator;
import com.google.common.collect.Lists;

@ElementsNarutomodMod.ModElement.Tag
public class EntityIceDome extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 224;
	public static final int ENTITYID_RANGED = 225;
	private static final float ENTITY_SCALE = 8.0f;

	public EntityIceDome(ElementsNarutomodMod instance) {
		super(instance, 535);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class)
		 .id(new ResourceLocation("narutomod", "ice_dome"), ENTITYID).name("ice_dome").tracker(64, 3, true).build());
	}

	@Override
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new EC.LivingHook());
	}

	public static class EC extends EntityShieldBase implements ItemJutsu.IJutsu {
		private final int talkTime = 26;
		private int shootSpearsTime;
		private List<EntityLivingBase> entitiesInside = Lists.newArrayList();
		private EntityLivingBase excludedEntity;

		public EC(World world) {
			super(world);
			this.isImmuneToFire = false;
			this.setSize(1.6f * ENTITY_SCALE, 0.8f * ENTITY_SCALE);
		}

		public EC(EntityLivingBase summonerIn, double x, double y, double z) {
			super(summonerIn, x, y, z);
			this.isImmuneToFire = false;
			this.setSize(1.6f * ENTITY_SCALE, 0.8f * ENTITY_SCALE);
		}

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.HYOTON;
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(100D);
			this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(400D);
		}

		@Override
		public void setDead() {
			super.setDead();
			if (!this.world.isRemote) {
				this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:ice_shoot_small")),
				 0.8f, this.rand.nextFloat() * 0.4f + 0.8f);
				for (int i = 0; i < this.rand.nextInt(50) + 100; i++) {
					EntityIceSpear.EC.spawnShatteredShard(this.world, this.posX + this.width * (this.rand.nextFloat()-0.5f), 
					 this.posY + this.height * this.rand.nextFloat(), this.posZ + this.width * (this.rand.nextFloat()-0.5f),
					 (this.rand.nextDouble()-0.5d) * 0.05d, 0d, (this.rand.nextDouble()-0.5d) * 0.05d);
				}
				EntityLivingBase summoner = this.getSummoner();
				if (summoner != null && summoner.isPotionActive(MobEffects.INVISIBILITY)) {
					summoner.removePotionEffect(MobEffects.INVISIBILITY);
				}
			}
		}

		@Override
		public boolean processInitialInteract(EntityPlayer entity, EnumHand hand) {
			return false;
		}

		public List<EntityLivingBase> getEntitiesInside() {
			return this.entitiesInside;
		}

		public void excludeEntity(EntityLivingBase entity) {
			this.excludedEntity = entity;
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			if (this.world.isRemote && this.ticksExisted % 40 == 1) {
				ProcedureSync.ResetBoundingBox.sendToServer(this);
			}
			if (!this.world.isRemote && this.ticksExisted == this.talkTime) {
				this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:ice_formation")), 1f, 1f);
			}
			EntityLivingBase summoner = this.getSummoner();
			if (!this.world.isRemote && summoner != null && this.ticksExisted % 20 == 1) {
				summoner.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 23, 0, false, false));
			}
			if (this.ticksExisted < 5) {
				for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox())) {
					if (entity.canBeCollidedWith() && entity instanceof EntityLivingBase && !entity.equals(summoner)
					 && !entity.equals(this.excludedEntity) && !this.entitiesInside.contains(entity)
					 && this.getEntityBoundingBox().intersect(entity.getEntityBoundingBox()).equals(entity.getEntityBoundingBox())) {
						this.entitiesInside.add((EntityLivingBase)entity);
					}
				}
			}
			if (!this.world.isRemote && this.shootSpearsTime > 0 && summoner != null) {
				for (EntityLivingBase entity : this.entitiesInside) {
					if (entity.isEntityAlive() && !summoner.isOnSameTeam(entity)) {
						double d0 = (this.rand.nextDouble()-0.5d) * this.width;
						double d1 = (this.rand.nextDouble()-0.5d) * this.width;
						new EntityIceSpear.EC.Jutsu().createJutsu(this.world, summoner, this.posX + d0 * 0.8d,
						 this.posY + this.height - 1.6d, this.posZ + d1 * 0.8d, entity.posX, entity.posY + entity.height/2,
						 entity.posZ, 0.95f, 0.25f);
					}
				}
				--this.shootSpearsTime;
			}
			if (!this.entitiesInside.isEmpty()) {
				Iterator<EntityLivingBase> iter = this.entitiesInside.iterator();
				while (iter.hasNext()) {
					EntityLivingBase entity = iter.next();
					if (!ItemJutsu.canTarget(entity)) {
						iter.remove();
					}
				}
			}
			if (!this.world.isRemote && this.ticksExisted % 20 == 0 && summoner != null
			 && !Chakra.pathway(summoner).consume(ItemHyoton.ICEDOME.chakraUsage)) {
				this.setDead();
			}
			this.clearActivePotions();
		}

		public void shootSpears() {
			this.shootSpearsTime = 100;
		}

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			if (source == DamageSource.DROWN || source == DamageSource.STARVE || source == DamageSource.WITHER) {
				return false;
			}
			Entity srcEntity = source.getImmediateSource();
			if (srcEntity instanceof EntityIceSpear.EC) {
				return false;
			}
			if (srcEntity instanceof EntityLivingBase && this.entitiesInside.contains(srcEntity)) {
				Vec3d vec2 = srcEntity.getPositionEyes(1f);
				Vec3d vec1 = vec2.add(srcEntity.getLookVec().scale(3d));
				RayTraceResult res = this.getEntityBoundingBox().calculateIntercept(vec1, vec2);
				if (res == null || res.sideHit == EnumFacing.DOWN) {
					return false;
				}
			}
			return super.attackEntityFrom(source, amount);
		}

		@Override
		protected void collideWithNearbyEntities() {
			for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(3.0d))) {
				if (entity instanceof EntityLivingBase) {
					this.collideWithEntity(entity);
				}
			}
		}

		@Override
		protected void collideWithEntity(Entity entityIn) {
			this.applyEntityCollision(entityIn);
		}

		@Override
		public void applyEntityCollision(Entity entity) {
			if (!this.isRidingSameEntity(entity) && !entity.isBeingRidden() && !entity.noClip && ItemJutsu.canTarget(entity)) {
				//ProcedureSync.MotionPacket.sendToServer(entity);
				this.doCollisions(entity);
			}
		}

		private List<EnumFacing> doCollisions(Entity entity) {
        	AxisAlignedBB bb1 = this.getEntityBoundingBox();
        	AxisAlignedBB bb2 = entity.getEntityBoundingBox();
            List<EnumFacing> list = Lists.newArrayList();
//System.out.println(">>> inside:"+this.entitiesInside.contains(entity)+", lastTickPos:"+new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ));
//System.out.println("            bb1:"+bb1);
//System.out.println("    before: bb2:"+bb2);
			if (!this.entitiesInside.contains(entity)) {
				if (entity.motionX > 0d && bb2.maxX > bb1.minX && entity.lastTickPosX - entity.width/2 < bb1.minX
				 && bb2.maxY > bb1.minY && bb2.minY < bb1.maxY && bb2.maxZ > bb1.minZ && bb2.minZ < bb1.maxZ) {
					list.add(EnumFacing.WEST);
			       	entity.motionX *= -0.1d;
			       	entity.posX = bb1.minX - entity.width * 0.5;
				}
				if (entity.motionX < 0d && bb2.minX < bb1.maxX && entity.lastTickPosX + entity.width/2 > bb1.maxX
				 && bb2.maxY > bb1.minY && bb2.minY < bb1.maxY && bb2.maxZ > bb1.minZ && bb2.minZ < bb1.maxZ) {
					list.add(EnumFacing.EAST);
			    	entity.motionX *= -0.1d;
			    	entity.posX = bb1.maxX + entity.width * 0.5;
				}
				if (entity.motionZ > 0d && bb2.maxZ > bb1.minZ && entity.lastTickPosZ - entity.width/2 < bb1.minZ
				 && bb2.maxY > bb1.minY && bb2.minY < bb1.maxY && bb2.maxX > bb1.minX && bb2.minX < bb1.maxX) {
					list.add(EnumFacing.NORTH);
		        	entity.motionZ *= -0.1d;
		        	entity.posZ = bb1.minZ - entity.width * 0.5;
				}
				if (entity.motionZ < 0d && bb2.minZ < bb1.maxZ && entity.lastTickPosZ + entity.width/2 > bb1.maxZ
				 && bb2.maxY > bb1.minY && bb2.minY < bb1.maxY && bb2.maxX > bb1.minX && bb2.minX < bb1.maxX) {
					list.add(EnumFacing.SOUTH);
	        		entity.motionZ *= -0.1d;
	        		entity.posZ = bb1.maxZ + entity.width * 0.5;
				}
				if (entity.motionY > 0d && bb2.maxY > bb1.minY && entity.lastTickPosY < bb1.minY
				 && bb2.maxZ > bb1.minZ && bb2.minZ < bb1.maxZ && bb2.maxX > bb1.minX && bb2.minX < bb1.maxX) {
					list.add(EnumFacing.DOWN);
	        		entity.motionY *= -0.8d;
	        		entity.posY = bb1.minY - entity.height;
				}
				if (entity.motionY < 0d && bb2.minY < bb1.maxY && entity.lastTickPosY + entity.height > bb1.maxY
				 && bb2.maxZ > bb1.minZ && bb2.minZ < bb1.maxZ && bb2.maxX > bb1.minX && bb2.minX < bb1.maxX) {
					list.add(EnumFacing.UP);
	        		entity.motionY = 0.0d;
	        		entity.posY = bb1.maxY;
	        		entity.onGround = true;
				}
			} else {
				if (bb2.minX < bb1.minX) {
					list.add(EnumFacing.WEST);
			       	entity.motionX *= -0.1d;
			       	entity.posX = bb1.minX + entity.width * 0.5;
				}
				if (bb2.maxX > bb1.maxX) {
					list.add(EnumFacing.EAST);
			    	entity.motionX *= -0.1d;
			    	entity.posX = bb1.maxX - entity.width * 0.5;
				}
				if (bb2.minZ < bb1.minZ) {
        			list.add(EnumFacing.NORTH);
		        	entity.motionZ *= -0.1d;
		        	entity.posZ = bb1.minZ + entity.width * 0.5;
				}
				if (bb2.maxZ > bb1.maxZ) {
        			list.add(EnumFacing.SOUTH);
	        		entity.motionZ *= -0.1d;
	        		entity.posZ = bb1.maxZ - entity.width * 0.5;
				}
				if (bb2.minY < bb1.minY) {
        			list.add(EnumFacing.DOWN);
	        		entity.motionY = 0.0d;
	        		entity.posY = bb1.minY;
	        		entity.onGround = true;
				}
				if (bb2.maxY > bb1.maxY) {
        			list.add(EnumFacing.UP);
	        		entity.motionY *= -0.8d;
	        		entity.posY = bb1.maxY - entity.height;
				}
        	}
        	if (!list.isEmpty()) {
       			entity.setPosition(entity.posX, entity.posY, entity.posZ);
        		entity.velocityChanged = true;
        	}
//System.out.println("    after:  bb2:"+entity.getEntityBoundingBox());
//System.out.println("    hitface: "+list);
			return list;
		}

		@Override
		public double getMountedYOffset() {
			return (double)this.height;
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				if (!entity.isRiding()) {
					this.createJutsu(entity, entity.posX, entity.posY - 0.1d, entity.posZ);
					return true;
				} else if (entity.getRidingEntity() instanceof EC) {
					((EC)entity.getRidingEntity()).shootSpears();
					return true;
				}
				return false;
			}

			public EC createJutsu(EntityLivingBase entity, double x, double y, double z) {
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ,
				 SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:makyohyosho")),
				 net.minecraft.util.SoundCategory.NEUTRAL, 1f, 0.9f);
				EC entity1 = new EC(entity, x, y, z);
				entity.world.spawnEntity(entity1);
				return entity1;
			}
		}

		public static class LivingHook {
			@SubscribeEvent
			public void onAttackedInsideDome(LivingAttackEvent event) {
				EntityLivingBase target = event.getEntityLiving();
				if (!target.world.isRemote && !(target instanceof EC)) {
					EC dome = (EC)target.world.findNearestEntityWithinAABB(EC.class, target.getEntityBoundingBox().grow(ENTITY_SCALE), target);
					if (dome != null) {
						Entity attacker = event.getSource().getTrueSource();
						EntityLivingBase summoner = dome.getSummoner();
						//if (target.equals(summoner) 
						// || (dome.entitiesInside.contains(target) == !dome.entitiesInside.contains(attacker)
					    //  && !summoner.equals(attacker))) {
						if (dome.entitiesInside.contains(target) == !dome.entitiesInside.contains(attacker)
						 && !summoner.equals(attacker) && !target.equals(summoner)) {
							event.setCanceled(true);
							dome.attackEntityFrom(event.getSource(), event.getAmount());
					    }
					}
				}
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
			RenderingRegistry.registerEntityRenderingHandler(EC.class, renderManager -> new CustomRender(renderManager));
		}

		@SideOnly(Side.CLIENT)
		public class CustomRender extends Render<EC> {
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/dome_ice.png");
			private final ModelDome model = new ModelDome();
			private final int growTime = 30;

			public CustomRender(RenderManager renderManagerIn) {
				super(renderManagerIn);
				this.shadowSize = 0.0f;
			}

			@Override
			public boolean shouldRender(EC entity, net.minecraft.client.renderer.culling.ICamera camera, double camX, double camY, double camZ) {
				return true;
			}

			@Override
			public void doRender(EC entity, double x, double y, double z, float entityYaw, float pt) {
				GlStateManager.pushMatrix();
				this.bindEntityTexture(entity);
				GlStateManager.translate(x, y, z);
				//GlStateManager.rotate(-entity.prevRotationYaw - (entity.rotationYaw - entity.prevRotationYaw) * pt, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(-180.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.enableAlpha();
				GlStateManager.enableBlend();
				GlStateManager.disableCull();
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				GlStateManager.disableLighting();
				GlStateManager.color(1.0f, 1.0f, 1.0f, MathHelper.clamp((float) entity.ticksExisted / (this.growTime + entity.talkTime), 0f, 1.0f));
				this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.enableLighting();
				GlStateManager.enableCull();
				GlStateManager.disableBlend();
				//GlStateManager.disableAlpha();
				GlStateManager.popMatrix();
			}

			@Override
			protected ResourceLocation getEntityTexture(EC entity) {
				return this.texture;
			}
		}

		// Made with Blockbench 3.8.3
		// Exported for Minecraft version 1.7 - 1.12
		// Paste this class into your mod and generate all required imports
		@SideOnly(Side.CLIENT)
		public class ModelDome extends ModelBase {
			private final ModelRenderer dome;
			private final ModelRenderer wall;
			private final ModelRenderer bone2;
			private final ModelRenderer bone20;
			private final ModelRenderer bone6;
			private final ModelRenderer bone21;
			private final ModelRenderer bone3;
			private final ModelRenderer bone22;
			private final ModelRenderer bone7;
			private final ModelRenderer bone25;
			private final ModelRenderer bone4;
			private final ModelRenderer bone23;
			private final ModelRenderer bone8;
			private final ModelRenderer bone26;
			private final ModelRenderer bone15;
			private final ModelRenderer bone24;
			private final ModelRenderer bone19;
			private final ModelRenderer bone27;
			private final ModelRenderer roof;
			private final ModelRenderer bone10;
			private final ModelRenderer bone40;
			private final ModelRenderer bone39;
			private final ModelRenderer bone41;
			private final ModelRenderer bone17;
			private final ModelRenderer bone13;
			private final ModelRenderer bone16;
			private final ModelRenderer bone30;
			private final ModelRenderer bone32;
			private final ModelRenderer bone14;
			private final ModelRenderer bone18;
			private final ModelRenderer bone33;
			private final ModelRenderer bone31;
			private final ModelRenderer bone12;
			private final ModelRenderer bone28;
			private final ModelRenderer bone29;
			private final ModelRenderer bottom;
			private final ModelRenderer bone38;
			private final ModelRenderer bone;
			private final ModelRenderer bone34;
			private final ModelRenderer bone9;
			private final ModelRenderer bone35;
			private final ModelRenderer bone5;
			private final ModelRenderer bone36;
			private final ModelRenderer bone11;
			private final ModelRenderer bone37;

			public ModelDome() {
				textureWidth = 16;
				textureHeight = 16;

				dome = new ModelRenderer(this);
				dome.setRotationPoint(0.0F, 0.0F, 0.0F);
				
		
				wall = new ModelRenderer(this);
				wall.setRotationPoint(0.0F, 0.0F, 0.0F);
				dome.addChild(wall);
				
		
				bone2 = new ModelRenderer(this);
				bone2.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone2);
				bone2.cubeList.add(new ModelBox(bone2, 0, 0, -4.0F, -8.0F, -13.5F, 8, 8, 0, 0.0F, false));
		
				bone20 = new ModelRenderer(this);
				bone20.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone20);
				setRotationAngle(bone20, 0.0F, -0.3927F, 0.0F);
				bone20.cubeList.add(new ModelBox(bone20, 0, 0, -4.0F, -8.0F, -13.5F, 8, 8, 0, 0.0F, false));
		
				bone6 = new ModelRenderer(this);
				bone6.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone6);
				bone6.cubeList.add(new ModelBox(bone6, 0, 0, -4.0F, -8.0F, 13.7F, 8, 8, 0, 0.0F, false));
		
				bone21 = new ModelRenderer(this);
				bone21.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone21);
				setRotationAngle(bone21, 0.0F, -0.3927F, 0.0F);
				bone21.cubeList.add(new ModelBox(bone21, 0, 0, -4.0F, -8.0F, 13.7F, 8, 8, 0, 0.0F, false));
		
				bone3 = new ModelRenderer(this);
				bone3.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone3);
				setRotationAngle(bone3, 0.0F, -0.7854F, 0.0F);
				bone3.cubeList.add(new ModelBox(bone3, 0, 0, -3.8891F, -8.0F, -13.5459F, 8, 8, 0, 0.0F, false));
		
				bone22 = new ModelRenderer(this);
				bone22.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone22);
				setRotationAngle(bone22, 0.0F, -1.1781F, 0.0F);
				bone22.cubeList.add(new ModelBox(bone22, 0, 0, -3.8891F, -8.0F, -13.5459F, 8, 8, 0, 0.0F, false));
		
				bone7 = new ModelRenderer(this);
				bone7.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone7);
				setRotationAngle(bone7, 0.0F, -0.7854F, 0.0F);
				bone7.cubeList.add(new ModelBox(bone7, 0, 0, -3.8891F, -8.0F, 13.6874F, 8, 8, 0, 0.0F, false));
		
				bone25 = new ModelRenderer(this);
				bone25.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone25);
				setRotationAngle(bone25, 0.0F, -1.1781F, 0.0F);
				bone25.cubeList.add(new ModelBox(bone25, 0, 0, -3.8891F, -8.0F, 13.6874F, 8, 8, 0, 0.0F, false));
		
				bone4 = new ModelRenderer(this);
				bone4.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone4);
				setRotationAngle(bone4, 0.0F, -1.5708F, 0.0F);
				bone4.cubeList.add(new ModelBox(bone4, 0, 0, -3.85F, -8.0F, -13.65F, 8, 8, 0, 0.0F, false));
		
				bone23 = new ModelRenderer(this);
				bone23.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone23);
				setRotationAngle(bone23, 0.0F, -1.9635F, 0.0F);
				bone23.cubeList.add(new ModelBox(bone23, 0, 0, -3.85F, -8.0F, -13.65F, 8, 8, 0, 0.0F, false));
		
				bone8 = new ModelRenderer(this);
				bone8.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone8);
				setRotationAngle(bone8, 0.0F, -1.5708F, 0.0F);
				bone8.cubeList.add(new ModelBox(bone8, 0, 0, -3.9F, -8.0F, 13.6F, 8, 8, 0, 0.0F, false));
		
				bone26 = new ModelRenderer(this);
				bone26.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone26);
				setRotationAngle(bone26, 0.0F, -1.9635F, 0.0F);
				bone26.cubeList.add(new ModelBox(bone26, 0, 0, -3.9F, -8.0F, 13.6F, 8, 8, 0, 0.0F, false));
		
				bone15 = new ModelRenderer(this);
				bone15.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone15);
				setRotationAngle(bone15, 0.0F, 0.7854F, 0.0F);
				bone15.cubeList.add(new ModelBox(bone15, 0, 0, -4.1316F, -8.0F, 13.7331F, 8, 8, 0, 0.0F, false));
		
				bone24 = new ModelRenderer(this);
				bone24.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone24);
				setRotationAngle(bone24, 0.0F, 0.3927F, 0.0F);
				bone24.cubeList.add(new ModelBox(bone24, 0, 0, -4.1316F, -8.0F, 13.7331F, 8, 8, 0, 0.0F, false));
		
				bone19 = new ModelRenderer(this);
				bone19.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone19);
				setRotationAngle(bone19, 0.0F, 0.7854F, 0.0F);
				bone19.cubeList.add(new ModelBox(bone19, 0, 0, -4.0609F, -8.0F, -13.5709F, 8, 8, 0, 0.0F, false));
		
				bone27 = new ModelRenderer(this);
				bone27.setRotationPoint(0.0F, 0.0F, 0.0F);
				wall.addChild(bone27);
				setRotationAngle(bone27, 0.0F, 0.3927F, 0.0F);
				bone27.cubeList.add(new ModelBox(bone27, 0, 0, -4.0609F, -8.0F, -13.5709F, 8, 8, 0, 0.0F, false));
		
				roof = new ModelRenderer(this);
				roof.setRotationPoint(0.0F, -16.0F, -9.5F);
				dome.addChild(roof);
				
		
				bone10 = new ModelRenderer(this);
				bone10.setRotationPoint(0.0F, 2.7F, 9.5F);
				roof.addChild(bone10);
				setRotationAngle(bone10, -1.5708F, 0.0F, 0.0F);
				bone10.cubeList.add(new ModelBox(bone10, 0, 8, -3.95F, -8.15F, 0.0F, 8, 8, 0, 0.0F, false));
		
				bone40 = new ModelRenderer(this);
				bone40.setRotationPoint(0.0F, 2.7F, 9.5F);
				roof.addChild(bone40);
				setRotationAngle(bone40, -1.5708F, 0.0F, 0.0F);
				bone40.cubeList.add(new ModelBox(bone40, 0, 8, -8.95F, -4.15F, 0.0F, 8, 8, 0, 0.0F, false));
		
				bone39 = new ModelRenderer(this);
				bone39.setRotationPoint(0.0F, 2.7F, 9.5F);
				roof.addChild(bone39);
				setRotationAngle(bone39, -1.5708F, 0.0F, 0.0F);
				bone39.cubeList.add(new ModelBox(bone39, 0, 8, -3.95F, -0.15F, 0.0F, 8, 8, 0, 0.0F, false));
		
				bone41 = new ModelRenderer(this);
				bone41.setRotationPoint(0.0F, 2.7F, 9.5F);
				roof.addChild(bone41);
				setRotationAngle(bone41, -1.5708F, 0.0F, 0.0F);
				bone41.cubeList.add(new ModelBox(bone41, 0, 8, 1.05F, -4.15F, 0.0F, 8, 8, 0, 0.0F, false));
		
				bone17 = new ModelRenderer(this);
				bone17.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone17);
				setRotationAngle(bone17, -0.7854F, 1.5708F, 0.0F);
				bone17.cubeList.add(new ModelBox(bone17, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bone13 = new ModelRenderer(this);
				bone13.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone13);
				setRotationAngle(bone13, -0.7854F, 2.0944F, 0.0F);
				bone13.cubeList.add(new ModelBox(bone13, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bone16 = new ModelRenderer(this);
				bone16.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone16);
				setRotationAngle(bone16, -0.7854F, 3.1416F, 0.0F);
				bone16.cubeList.add(new ModelBox(bone16, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bone30 = new ModelRenderer(this);
				bone30.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone30);
				setRotationAngle(bone30, -0.7854F, -2.618F, 0.0F);
				bone30.cubeList.add(new ModelBox(bone30, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bone32 = new ModelRenderer(this);
				bone32.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone32);
				setRotationAngle(bone32, -0.7854F, -2.0944F, 0.0F);
				bone32.cubeList.add(new ModelBox(bone32, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bone14 = new ModelRenderer(this);
				bone14.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone14);
				setRotationAngle(bone14, -0.7854F, -1.5708F, 0.0F);
				bone14.cubeList.add(new ModelBox(bone14, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bone18 = new ModelRenderer(this);
				bone18.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone18);
				setRotationAngle(bone18, -0.7854F, 2.618F, 0.0F);
				bone18.cubeList.add(new ModelBox(bone18, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bone33 = new ModelRenderer(this);
				bone33.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone33);
				setRotationAngle(bone33, -0.7854F, 1.0472F, 0.0F);
				bone33.cubeList.add(new ModelBox(bone33, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bone31 = new ModelRenderer(this);
				bone31.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone31);
				setRotationAngle(bone31, -0.7854F, 0.5236F, 0.0F);
				bone31.cubeList.add(new ModelBox(bone31, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bone12 = new ModelRenderer(this);
				bone12.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone12);
				setRotationAngle(bone12, -0.7854F, 0.0F, 0.0F);
				bone12.cubeList.add(new ModelBox(bone12, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bone28 = new ModelRenderer(this);
				bone28.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone28);
				setRotationAngle(bone28, -0.7854F, -0.5236F, 0.0F);
				bone28.cubeList.add(new ModelBox(bone28, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bone29 = new ModelRenderer(this);
				bone29.setRotationPoint(0.0F, 0.0F, 9.5F);
				roof.addChild(bone29);
				setRotationAngle(bone29, -0.7854F, -1.0472F, 0.0F);
				bone29.cubeList.add(new ModelBox(bone29, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
		
				bottom = new ModelRenderer(this);
				bottom.setRotationPoint(0.0F, 0.0F, 0.0F);
				dome.addChild(bottom);
				
		
				bone38 = new ModelRenderer(this);
				bone38.setRotationPoint(0.0F, 0.0F, 0.0F);
				bottom.addChild(bone38);
				setRotationAngle(bone38, 0.0F, -1.5708F, 0.0F);
				bone38.cubeList.add(new ModelBox(bone38, -8, 0, -4.0F, 0.0F, -4.0074F, 8, 0, 8, 0.0F, false));
		
				bone = new ModelRenderer(this);
				bone.setRotationPoint(0.0F, 0.0F, 0.0F);
				bottom.addChild(bone);
				setRotationAngle(bone, 0.0F, -0.7854F, 0.0F);
				bone.cubeList.add(new ModelBox(bone, -8, 0, -4.0F, 0.0F, 4.9926F, 8, 0, 8, 0.0F, false));
		
				bone34 = new ModelRenderer(this);
				bone34.setRotationPoint(0.0F, 0.0F, 0.0F);
				bottom.addChild(bone34);
				setRotationAngle(bone34, 0.0F, -1.5708F, 0.0F);
				bone34.cubeList.add(new ModelBox(bone34, -8, 0, -4.0F, 0.0F, 4.9926F, 8, 0, 8, 0.0F, false));
		
				bone9 = new ModelRenderer(this);
				bone9.setRotationPoint(0.0F, 0.0F, 0.0F);
				bottom.addChild(bone9);
				setRotationAngle(bone9, 0.0F, 0.7854F, 0.0F);
				bone9.cubeList.add(new ModelBox(bone9, -8, 0, -4.0F, 0.0F, 4.75F, 8, 0, 8, 0.0F, false));
		
				bone35 = new ModelRenderer(this);
				bone35.setRotationPoint(0.0F, 0.0F, 0.0F);
				bottom.addChild(bone35);
				bone35.cubeList.add(new ModelBox(bone35, -8, 0, -4.0F, 0.0F, 4.75F, 8, 0, 8, 0.0F, false));
		
				bone5 = new ModelRenderer(this);
				bone5.setRotationPoint(0.0F, 0.0F, 0.0F);
				bottom.addChild(bone5);
				setRotationAngle(bone5, 0.0F, -2.3562F, 0.0F);
				bone5.cubeList.add(new ModelBox(bone5, -8, 0, -4.0F, 0.0F, 4.75F, 8, 0, 8, 0.0F, false));
		
				bone36 = new ModelRenderer(this);
				bone36.setRotationPoint(0.0F, 0.0F, 0.0F);
				bottom.addChild(bone36);
				setRotationAngle(bone36, 0.0F, 3.1416F, 0.0F);
				bone36.cubeList.add(new ModelBox(bone36, -8, 0, -4.0F, 0.0F, 4.75F, 8, 0, 8, 0.0F, false));
		
				bone11 = new ModelRenderer(this);
				bone11.setRotationPoint(0.0F, 0.0F, 0.0F);
				bottom.addChild(bone11);
				setRotationAngle(bone11, 0.0F, 2.3562F, 0.0F);
				bone11.cubeList.add(new ModelBox(bone11, -8, 0, -4.0F, 0.0F, 4.75F, 8, 0, 8, 0.0F, false));
		
				bone37 = new ModelRenderer(this);
				bone37.setRotationPoint(0.0F, 0.0F, 0.0F);
				bottom.addChild(bone37);
				setRotationAngle(bone37, 0.0F, 1.5708F, 0.0F);
				bone37.cubeList.add(new ModelBox(bone37, -8, 0, -4.0F, 0.0F, 4.75F, 8, 0, 8, 0.0F, false));
			}

			@Override
			public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
				GlStateManager.pushMatrix();
				GlStateManager.scale(ENTITY_SCALE, ENTITY_SCALE, ENTITY_SCALE);
				dome.render(f5);
				GlStateManager.popMatrix();
			}

			public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
				modelRenderer.rotateAngleX = x;
				modelRenderer.rotateAngleY = y;
				modelRenderer.rotateAngleZ = z;
			}
		}
	}
}
