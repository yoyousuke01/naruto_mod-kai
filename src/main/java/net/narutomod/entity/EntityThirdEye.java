
package net.narutomod.entity;

import net.narutomod.Chakra;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJiton;
import net.narutomod.NarutomodMod;
import net.narutomod.Particles;
import net.narutomod.PlayerInput;
import net.narutomod.procedure.ProcedureSync;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.item.ItemStack;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;

@ElementsNarutomodMod.ModElement.Tag
public class EntityThirdEye extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 518;
	public static final int ENTITYID_RANGED = 519;

	public EntityThirdEye(ElementsNarutomodMod instance) {
		super(instance, 935);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class)
				.id(new ResourceLocation("narutomod", "third_eye"), ENTITYID).name("third_eye").tracker(64, 3, true).egg(-1, -1).build());
	}

	public static class EC extends EntityAltCamView.EntityCustom implements PlayerInput.Hook.IHandler, ProcedureSync.RenderDistance.IHandler {
		private static final DataParameter<Integer> VIEWERID = EntityDataManager.<Integer>createKey(EC.class, DataSerializers.VARINT);
		private static final DataParameter<Integer> AGE = EntityDataManager.<Integer>createKey(EC.class, DataSerializers.VARINT);
		private final EntityAltCamView.PacketCameraPosition.Handler updater = new EntityAltCamView.PacketCameraPosition.Handler();
		private PlayerInput.Hook viewerInput = new PlayerInput.Hook();
		private int clientDummyId;
		private int serverMainId;
		private int oldRenderDistance;
		private ItemJiton.Type sandType = ItemJiton.Type.IRON;

		public EC(World world) {
			super(world);
			this.setSize(0.25f, 0.25f);
			this.isImmuneToFire = false;
			this.setNoGravity(true);
			this.noClip = false;
		}

		public EC(EntityPlayer player) {
			this(player.world);
			this.setViewer(player);
			this.setLocationAndAngles(player.posX, player.posY + 2.5d, player.posZ, player.rotationYaw, player.rotationPitch);
		}

		public EC(EntityPlayer player, ItemJiton.Type type) {
			this(player);
			this.sandType = type;
		}

		@Override
		protected void entityInit() {
			super.entityInit();
			this.dataManager.register(VIEWERID, Integer.valueOf(-1));
			this.dataManager.register(AGE, Integer.valueOf(0));
		}

		@Override @Nullable
		public EntityPlayer getViewer() {
			EntityPlayer viewer = super.getViewer();
			if (viewer != null) {
				return viewer;
			}
			Entity entity = this.world.getEntityByID(((Integer)this.getDataManager().get(VIEWERID)).intValue());
			viewer = entity instanceof EntityPlayer ? (EntityPlayer)entity : null;
			super.setViewer(viewer);
			return viewer;
		}

		@Override
		protected void setViewer(@Nullable EntityPlayer viewerPlayer) {
			if (!this.world.isRemote) {
				this.getDataManager().set(VIEWERID, Integer.valueOf(viewerPlayer.getEntityId()));
			}
			super.setViewer(viewerPlayer);
		}

		private int getAge() {
			return ((Integer)this.getDataManager().get(AGE)).intValue();
		}

		private void setAge(int age) {
			if (!this.world.isRemote) {
				this.getDataManager().set(AGE, Integer.valueOf(age));
			}
		}

		@Override
		protected void onSetDead() {
			if (!this.world.isRemote) {
				EntityPlayer viewer = this.getViewer();
				if (viewer instanceof EntityPlayerMP) {
					PlayerInput.Hook.copyInputFrom((EntityPlayerMP)viewer, this, false);
					this.updater.flushChunks((EntityPlayerMP)viewer);
					((EntityPlayerMP)viewer).connection.sendPacket(new SPacketCamera(viewer));
					ProcedureSync.RenderDistance.sendToSelf((EntityPlayerMP)viewer, this.oldRenderDistance, null);
					ProcedureSync.EntityDead.sendToSelf(this.clientDummyId, (EntityPlayerMP)viewer);
				}
				for (int i = 0; i < 100; i++) {
					Particles.spawnParticle(this.world, Particles.Types.FALLING_DUST, this.posX + (this.rand.nextFloat()-0.5f) * this.width,
					 this.posY + this.height * 0.5f, this.posZ + (this.rand.nextFloat()-0.5f) * this.width,
					 1, 0, 0, 0, 0, this.rand.nextFloat() * 0.3f - 0.1f, 0, this.sandType.getColor(), 0, 3);
				}
			}
		}

		@Override
		public void onUpdate() {
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			if (!this.world.isRemote) {
				int age = this.getAge();
				if (this.rand.nextFloat() < 0.5f) {
					Particles.spawnParticle(this.world, Particles.Types.FALLING_DUST, this.posX + (this.rand.nextFloat()-0.5f) * this.width,
					 this.posY + this.height * 0.5f, this.posZ + (this.rand.nextFloat()-0.5f) * this.width, 1, 0, 0, 0, 0, 0, 0,
					 this.sandType.getColor(), 0, 2);
				}
				EntityPlayer viewer = this.getViewer();
				if (viewer instanceof EntityPlayerMP && (this.ticksExisted % 20 != 0
				 || Chakra.pathway(viewer).consume(ItemJiton.THIRDEYE.chakraUsage * MathHelper.sqrt(this.getDistance(viewer)) * 0.02d))) {
					if (age == 0) {
						ProcedureSync.RenderDistance.sendToSelf((EntityPlayerMP)viewer, 6, this);
						PlayerInput.Hook.copyInputFrom((EntityPlayerMP)viewer, this, true);
					}
					this.updater.doChunkLoading((EntityPlayerMP)viewer, this.chunkCoordX, this.chunkCoordZ);
					if (this.viewerInput.hasNewMovementInput()) {
						this.viewerInput.handleMovement(this, 0.7f);
						this.isAirBorne = true;
					}
					if (this.viewerInput.hasNewMouseEvent()) {
						this.viewerInput.handleMouseEvent(this);
					}
					if (this.clientDummyId != 0) {
						CopyPositionAndRotation.sendTo(this, this.clientDummyId, (EntityPlayerMP)viewer);
					}
				} else {
					this.setDead();
				}
				this.setAge(++age);
			} else {
				this.onEntityUpdate();
			}
		}

		@SideOnly(Side.CLIENT)
		@Override
		public void onEntityUpdate() {
			if (this.serverMainId == 0 && this.clientDummyId == 0 && this.getAge() < 5) {
				Minecraft mc = Minecraft.getMinecraft();
				EntityPlayer viewer = this.getViewer();
				if (mc.player != null && mc.player == viewer) {
					EC dummy = new EC(viewer);
					this.clientDummyId = dummy.getEntityId();
					dummy.serverMainId = this.getEntityId();
					mc.world.spawnEntity(dummy);
					mc.setRenderViewEntity(dummy);
					CustomPacket.sendToServer(this.clientDummyId, dummy.serverMainId);
				}
			}
		}

		@Override
		public boolean canBeCollidedWith() {
			return !this.isDead;
		}

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			this.setDead();
			return true;
		}

		@Override
		public void handlePacket(@Nullable PlayerInput.Hook.MovementPacket movementPacket, @Nullable PlayerInput.Hook.MousePacket mousePacket) {
			if (movementPacket != null) {
				this.viewerInput.copyMovementInput(movementPacket);
			}
			if (mousePacket != null) {
				this.viewerInput.copyMouseInput(mousePacket);
			}
		}

		@Override
		public void handleClientPacket(EntityPlayer player, int oldChunkDistance) {
			this.oldRenderDistance = oldChunkDistance;
		}

		@Override
		protected void readEntityFromNBT(NBTTagCompound compound) {
			super.readEntityFromNBT(compound);
			this.sandType = ItemJiton.Type.getTypeFromId(compound.getInteger("sandType"));
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound compound) {
			super.writeEntityToNBT(compound);
			compound.setInteger("sandType", this.sandType.getID());
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			private static final String ID_KEY = "ThirdEyeIdKey";
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				Entity entity1 = entity.world.getEntityByID(entity.getEntityData().getInteger(ID_KEY));
				if (entity1 instanceof EC) {
					entity1.setDead();
				} else if (entity instanceof EntityPlayer) {
					entity1 = new EC((EntityPlayer)entity, ItemJiton.getSandType(stack));
					entity.world.spawnEntity(entity1);
					entity.getEntityData().setInteger(ID_KEY, entity1.getEntityId());
					return true;
				}
				return false;
			}

			@Override
			public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
			}
		}
	}

	public static class CustomPacket implements IMessage {
	    int from;
	    int to;
	
	    public CustomPacket() {}
	
	    public CustomPacket(int fromEntityId, int toEntityId) {
	    	this.from = fromEntityId;
	    	this.to = toEntityId;
	    }

	    @Override
	    public void toBytes(ByteBuf buf) {
	        buf.writeInt(this.from);
	        buf.writeInt(this.to);
	    }
	
	    @Override
	    public void fromBytes(ByteBuf buf) {
	        this.from = buf.readInt();
	        this.to = buf.readInt();
	    }

	    public static void sendToServer(int fromEntityId, int toEntityId) {
	    	NarutomodMod.PACKET_HANDLER.sendToServer(new CustomPacket(fromEntityId, toEntityId));
	    }
	
	    public static class Handler implements IMessageHandler<CustomPacket, IMessage> {
	    	@Override
	        public IMessage onMessage(CustomPacket msg, MessageContext ctx) {
	        	EntityPlayerMP player = ctx.getServerHandler().player;
	            player.getServerWorld().addScheduledTask(() -> {
	            	Entity toEntity = player.getServerWorld().getEntityByID(msg.to);
	            	if (toEntity instanceof EC) {
	            		((EC)toEntity).clientDummyId = msg.from;
	            	}
				});
	            return null;
	        }
	    }
	}

	public static class CopyPositionAndRotation implements IMessage {
		int to;
		ProcedureSync.PositionRotationPacket prp;

		public CopyPositionAndRotation() { }

		public CopyPositionAndRotation(EC fromEntity, int toId) {
			this.to = toId;
			this.prp = new ProcedureSync.PositionRotationPacket(fromEntity);
		}

		public static void sendTo(EC entity, int toId, EntityPlayerMP player) {
			NarutomodMod.PACKET_HANDLER.sendTo(new CopyPositionAndRotation(entity, toId), player);
		}

		public static class Handler implements IMessageHandler<CopyPositionAndRotation, IMessage> {
			@SideOnly(Side.CLIENT)
			@Override
			public IMessage onMessage(CopyPositionAndRotation message, MessageContext context) {
				Minecraft mc = Minecraft.getMinecraft();
				mc.addScheduledTask(() -> {
					Entity entity = null;
					for (Entity entity1 : mc.world.loadedEntityList) {
						if (entity1.getEntityId() == message.to) {
							entity = entity1;
							break;
						}
					}
					if (entity instanceof EC) {
						entity.setPositionAndRotationDirect(message.prp.posX, message.prp.posY, message.prp.posZ,
						 message.prp.rotationYaw, message.prp.rotationPitch, 3, false);
					}
				});
				return null;
			}
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(this.to);
			this.prp.toBytes(buf);
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.to = buf.readInt();
			this.prp = new ProcedureSync.PositionRotationPacket(buf);
		}
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		new Renderer().register();
		this.elements.addNetworkMessage(CustomPacket.Handler.class, CustomPacket.class, Side.SERVER);
		this.elements.addNetworkMessage(CopyPositionAndRotation.Handler.class, CopyPositionAndRotation.class, Side.CLIENT);
	}

	public static class Renderer extends EntityRendererRegister {
		@SideOnly(Side.CLIENT)
		@Override
		public void register() {
			RenderingRegistry.registerEntityRenderingHandler(EC.class, renderManager -> new RenderCustom(renderManager));
		}

		@SideOnly(Side.CLIENT)
		public class RenderCustom extends Render<EC> {
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/eyeball.png");
			protected final ModelEyeball model;
	
			public RenderCustom(RenderManager renderManager) {
				super(renderManager);
				this.model = new ModelEyeball();
			}
	
			@Override
			public void doRender(EC entity, double x, double y, double z, float entityYaw, float partialTicks) {
				if (entity.serverMainId != 0 || (this.renderManager.options.thirdPersonView == 0 && this.renderManager.renderViewEntity instanceof EC)) {
					return;
				}
				this.bindEntityTexture(entity);
				GlStateManager.pushMatrix();
				GlStateManager.translate(x, y + 0.125D, z);
				GlStateManager.rotate(180F - entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(-entity.prevRotationPitch - (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 1.0F, 0.0F, 0.0F);
				GlStateManager.scale(0.8F, 0.8F, 0.8F);
				this.model.render(entity, 0, 0, partialTicks + entity.ticksExisted, 0.0F, 0.0F, 0.0625F);
				GlStateManager.popMatrix();
			}
	
			@Override
			protected ResourceLocation getEntityTexture(EC entity) {
				return this.texture;
			}
		}

		// Made with Blockbench 4.12.4
		// Exported for Minecraft version 1.7 - 1.12
		// Paste this class into your mod and generate all required imports
		@SideOnly(Side.CLIENT)
		public class ModelEyeball extends ModelBase {
			private final ModelRenderer bone;
			private final ModelRenderer octagon;
			private final ModelRenderer octagon_r1;
			private final ModelRenderer octagon_r2;
			private final ModelRenderer octagon_r3;
			private final ModelRenderer octagon2;
			private final ModelRenderer octagon_r4;
			private final ModelRenderer octagon_r5;
			private final ModelRenderer octagon_r6;
			private final ModelRenderer octagon3;
			private final ModelRenderer octagon_r7;
			private final ModelRenderer octagon_r8;
			private final ModelRenderer octagon_r9;
			private final ModelRenderer octagon4;
			private final ModelRenderer octagon_r10;
			private final ModelRenderer octagon_r11;
			private final ModelRenderer octagon_r12;
			
			public ModelEyeball() {
				textureWidth = 32;
				textureHeight = 32;

				bone = new ModelRenderer(this);
				bone.setRotationPoint(0.0F, 0.0F, 0.0F);
				
		
				octagon = new ModelRenderer(this);
				octagon.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone.addChild(octagon);
				octagon.cubeList.add(new ModelBox(octagon, 0, 0, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon_r1 = new ModelRenderer(this);
				octagon_r1.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon.addChild(octagon_r1);
				setRotationAngle(octagon_r1, -2.3562F, 0.0F, 0.0F);
				octagon_r1.cubeList.add(new ModelBox(octagon_r1, 14, 0, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon_r2 = new ModelRenderer(this);
				octagon_r2.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon.addChild(octagon_r2);
				setRotationAngle(octagon_r2, -1.5708F, 0.0F, 0.0F);
				octagon_r2.cubeList.add(new ModelBox(octagon_r2, 0, 14, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon_r3 = new ModelRenderer(this);
				octagon_r3.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon.addChild(octagon_r3);
				setRotationAngle(octagon_r3, -0.7854F, 0.0F, 0.0F);
				octagon_r3.cubeList.add(new ModelBox(octagon_r3, 0, 7, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon2 = new ModelRenderer(this);
				octagon2.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone.addChild(octagon2);
				setRotationAngle(octagon2, 0.0F, 0.0F, 0.7854F);
				
		
				octagon_r4 = new ModelRenderer(this);
				octagon_r4.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon2.addChild(octagon_r4);
				setRotationAngle(octagon_r4, -2.3562F, 0.0F, 0.0F);
				octagon_r4.cubeList.add(new ModelBox(octagon_r4, 14, 0, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon_r5 = new ModelRenderer(this);
				octagon_r5.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon2.addChild(octagon_r5);
				setRotationAngle(octagon_r5, -1.5708F, 0.0F, 0.0F);
				octagon_r5.cubeList.add(new ModelBox(octagon_r5, 0, 14, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon_r6 = new ModelRenderer(this);
				octagon_r6.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon2.addChild(octagon_r6);
				setRotationAngle(octagon_r6, -0.7854F, 0.0F, 0.0F);
				octagon_r6.cubeList.add(new ModelBox(octagon_r6, 0, 7, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon3 = new ModelRenderer(this);
				octagon3.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone.addChild(octagon3);
				setRotationAngle(octagon3, 0.0F, 0.0F, 1.5708F);
				
		
				octagon_r7 = new ModelRenderer(this);
				octagon_r7.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon3.addChild(octagon_r7);
				setRotationAngle(octagon_r7, -2.3562F, 0.0F, 0.0F);
				octagon_r7.cubeList.add(new ModelBox(octagon_r7, 14, 0, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon_r8 = new ModelRenderer(this);
				octagon_r8.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon3.addChild(octagon_r8);
				setRotationAngle(octagon_r8, -1.5708F, 0.0F, 0.0F);
				octagon_r8.cubeList.add(new ModelBox(octagon_r8, 0, 14, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon_r9 = new ModelRenderer(this);
				octagon_r9.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon3.addChild(octagon_r9);
				setRotationAngle(octagon_r9, -0.7854F, 0.0F, 0.0F);
				octagon_r9.cubeList.add(new ModelBox(octagon_r9, 0, 7, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon4 = new ModelRenderer(this);
				octagon4.setRotationPoint(0.0F, 0.0F, 0.0F);
				bone.addChild(octagon4);
				setRotationAngle(octagon4, 0.0F, 0.0F, 2.3562F);
				
		
				octagon_r10 = new ModelRenderer(this);
				octagon_r10.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon4.addChild(octagon_r10);
				setRotationAngle(octagon_r10, -2.3562F, 0.0F, 0.0F);
				octagon_r10.cubeList.add(new ModelBox(octagon_r10, 14, 0, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon_r11 = new ModelRenderer(this);
				octagon_r11.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon4.addChild(octagon_r11);
				setRotationAngle(octagon_r11, -1.5708F, 0.0F, 0.0F);
				octagon_r11.cubeList.add(new ModelBox(octagon_r11, 0, 14, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
		
				octagon_r12 = new ModelRenderer(this);
				octagon_r12.setRotationPoint(0.0F, 0.0F, 0.0F);
				octagon4.addChild(octagon_r12);
				setRotationAngle(octagon_r12, -0.7854F, 0.0F, 0.0F);
				octagon_r12.cubeList.add(new ModelBox(octagon_r12, 0, 7, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
			}
	
			@Override
			public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
				bone.render(f5);
			}
	
			public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
				modelRenderer.rotateAngleX = x;
				modelRenderer.rotateAngleY = y;
				modelRenderer.rotateAngleZ = z;
			}
	
			@Override
			public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
				bone.rotateAngleX = headPitch * 0.017453292F;
			}
		}
	}
}
