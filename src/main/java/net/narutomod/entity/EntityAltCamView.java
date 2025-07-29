
package net.narutomod.entity;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;

import net.narutomod.ElementsNarutomodMod;
import net.narutomod.NarutomodMod;

import io.netty.buffer.ByteBuf;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import com.google.common.collect.ImmutableList;

@ElementsNarutomodMod.ModElement.Tag
public class EntityAltCamView extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 60;
	public static final int CAMERA_RADIUS = 4;

	public EntityAltCamView(ElementsNarutomodMod instance) {
		super(instance, 268);
	}

	public void initElements() {
		this.elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class)
				.id(new ResourceLocation("narutomod", "altcamviewentity"), ENTITYID).name("altcamviewentity").tracker(64, 1, true).build());
	}

	public static class EntityCustom extends Entity {
		//private static final DataParameter<Integer> VIEWERID = EntityDataManager.<Integer>createKey(EntityCustom.class, DataSerializers.VARINT);
		private EntityPlayer cachedViewer;

		public EntityCustom(World world) {
			super(world);
			this.setSize(0.1F, 0.1F);
			this.setNoGravity(true);
			this.setEntityInvulnerable(true);
			this.isImmuneToFire = true;
			this.noClip = true;
		}

		public EntityCustom(EntityPlayer player) {
			this(player.world);
			this.setViewer(player);
			this.copyLocationAndAnglesFrom(player);
		}

		@Override
		protected void entityInit() {
			//this.dataManager.register(VIEWERID, Integer.valueOf(-1));
		}

		@Nullable
		public EntityPlayer getViewer() {
			//if (this.cachedViewer != null) {
				return this.cachedViewer;
			//}
			//Entity entity = this.world.getEntityByID(((Integer)this.getDataManager().get(VIEWERID)).intValue());
			//return entity instanceof EntityPlayer ? (EntityPlayer)entity : null;
		}

		protected void setViewer(EntityPlayer viewerPlayer) {
			//if (!this.world.isRemote) {
			//	this.getDataManager().set(VIEWERID, Integer.valueOf(viewerPlayer.getEntityId()));
			//}
			this.cachedViewer = viewerPlayer;
		}

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			return false;
		}

		@Override
		public void onUpdate() {
			if (this.world.isRemote) {
				this.onEntityUpdate();
		        // send chunk coordinates to server to load chunks and track entities
		        PacketCameraPosition.sendToServer(this);
			}
		}

		@SideOnly(Side.CLIENT)
		@Override
		public void onEntityUpdate() {
			EntityPlayer viewer = this.getViewer();
			if (viewer instanceof EntityPlayerSP) {
				EntityPlayerSP player = (EntityPlayerSP)viewer;
	            player.moveStrafing = player.movementInput.moveStrafe;
	            player.moveForward = player.movementInput.moveForward;
	            player.setJumping(player.movementInput.jump);
	            player.prevRenderArmYaw = player.renderArmYaw;
	            player.prevRenderArmPitch = player.renderArmPitch;
	            player.renderArmPitch = (float)((double)player.renderArmPitch + (double)(player.rotationPitch - player.renderArmPitch) * 0.5D);
	            player.renderArmYaw = (float)((double)player.renderArmYaw + (double)(player.rotationYaw - player.renderArmYaw) * 0.5D);

	            double minY = player.getEntityBoundingBox().minY;
	            double d0 = player.posX - (double)ObfuscationReflectionHelper.getPrivateValue(EntityPlayerSP.class, player, 4);//player.lastReportedPosX;
	            double d1 = minY - (double)ObfuscationReflectionHelper.getPrivateValue(EntityPlayerSP.class, player, 5);//player.lastReportedPosY;
	            double d2 = player.posZ - (double)ObfuscationReflectionHelper.getPrivateValue(EntityPlayerSP.class, player, 6);//player.lastReportedPosZ;
	            double d3 = (double)(player.rotationYaw - (float)ObfuscationReflectionHelper.getPrivateValue(EntityPlayerSP.class, player, 7));//player.lastReportedYaw);
	            double d4 = (double)(player.rotationPitch - (float)ObfuscationReflectionHelper.getPrivateValue(EntityPlayerSP.class, player, 8));//player.lastReportedPitch);
	            int positionUpdateTicks = (int)ObfuscationReflectionHelper.getPrivateValue(EntityPlayerSP.class, player, 12) + 1;
	            ObfuscationReflectionHelper.setPrivateValue(EntityPlayerSP.class, player, positionUpdateTicks, 12);//player.positionUpdateTicks;
	            boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || positionUpdateTicks >= 20;
	            boolean flag3 = d3 != 0.0D || d4 != 0.0D;
	            if (player.isRiding()) {
	                player.connection.sendPacket(new CPacketPlayer.PositionRotation(player.motionX, -999.0D, player.motionZ, player.rotationYaw, player.rotationPitch, player.onGround));
	                flag2 = false;
	            } else if (flag2 && flag3) {
	                player.connection.sendPacket(new CPacketPlayer.PositionRotation(player.posX, minY, player.posZ, player.rotationYaw, player.rotationPitch, player.onGround));
	            } else if (flag2) {
	                player.connection.sendPacket(new CPacketPlayer.Position(player.posX, minY, player.posZ, player.onGround));
	            } else if (flag3) {
	                player.connection.sendPacket(new CPacketPlayer.Rotation(player.rotationYaw, player.rotationPitch, player.onGround));
	            } else if ((boolean)ObfuscationReflectionHelper.getPrivateValue(EntityPlayerSP.class, player, 9) != player.onGround) {
	                player.connection.sendPacket(new CPacketPlayer(player.onGround));
	            }
	            if (flag2) {
	                ObfuscationReflectionHelper.setPrivateValue(EntityPlayerSP.class, player, player.posX, 4);//player.lastReportedPosX = player.posX;
	                ObfuscationReflectionHelper.setPrivateValue(EntityPlayerSP.class, player, minY, 5);//player.lastReportedPosY = axisalignedbb.minY;	                
	                ObfuscationReflectionHelper.setPrivateValue(EntityPlayerSP.class, player, player.posZ, 6);//player.lastReportedPosZ = player.posZ;
	                ObfuscationReflectionHelper.setPrivateValue(EntityPlayerSP.class, player, 0, 12);//player.positionUpdateTicks = 0;
	            }
	            if (flag3) {
	                ObfuscationReflectionHelper.setPrivateValue(EntityPlayerSP.class, player, player.rotationYaw, 7);//player.lastReportedYaw = player.rotationYaw;
	                ObfuscationReflectionHelper.setPrivateValue(EntityPlayerSP.class, player, player.rotationPitch, 8);//player.lastReportedPitch = player.rotationPitch;
	            }
	            Minecraft mc = Minecraft.getMinecraft();
	            ObfuscationReflectionHelper.setPrivateValue(EntityPlayerSP.class, player, player.onGround, 9);//player.prevOnGround = player.onGround;
	            ObfuscationReflectionHelper.setPrivateValue(EntityPlayerSP.class, player, mc.gameSettings.autoJump, 30);//player.autoJumpEnabled = player.mc.gameSettings.autoJump;
			//} else {
			//	this.setDead();
			}
		}

		protected void onSetDead() {
			if (this.world.isRemote) {
				PacketCameraPosition.sendToServer(this);
			}
		}

		@Override
		public void setDead() {
			super.setDead();
			this.onSetDead();
		}

		@Override
		protected void readEntityFromNBT(NBTTagCompound compound) {
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound compound) {
		}
	}

	public static class PacketCameraPosition implements IMessage {
	    int x;
	    int z;
	    boolean flushAll;
	
	    public PacketCameraPosition() {}
	
	    public PacketCameraPosition(Entity entity) {
	    	this.x = entity.chunkCoordX;
	    	this.z = entity.chunkCoordZ;
	    	this.flushAll = entity.isDead;
	    }

	    @Override
	    public void toBytes(ByteBuf buf) {
	        buf.writeInt(this.x);
	        buf.writeInt(this.z);
	        buf.writeBoolean(this.flushAll);
	    }
	
	    @Override
	    public void fromBytes(ByteBuf buf) {
	        this.x = buf.readInt();
	        this.z = buf.readInt();
	        this.flushAll = buf.readBoolean();
	    }

	    public static void sendToServer(Entity entity) {
	    	NarutomodMod.PACKET_HANDLER.sendToServer(new PacketCameraPosition(entity));
	    }
	
	    public static class Handler implements IMessageHandler<PacketCameraPosition, IMessage> {
			private final Set<ChunkPos> cameraLoadedChunks = new HashSet<>();
			private final Map<Integer, EntityTrackerEntry> entityTrackers = new HashMap<>();
			private static final Method playerChunkMap$getOrCreateEntry;
	    	static {
	    		try {
	    			playerChunkMap$getOrCreateEntry = ObfuscationReflectionHelper.findMethod(PlayerChunkMap.class, "func_187302_c", PlayerChunkMapEntry.class, int.class, int.class);
	    			playerChunkMap$getOrCreateEntry.setAccessible(true);
	    		} catch (Exception e) {
	    			throw new RuntimeException("Failed to find PlayerChunkMap$getOrCreateEntry(int, int)", e);
	    		}
	    	}
	    	
	    	@Override
	        public IMessage onMessage(PacketCameraPosition msg, MessageContext ctx) {
	        	EntityPlayerMP player = ctx.getServerHandler().player;
	            player.getServerWorld().addScheduledTask(() -> {
	            	if (msg.flushAll) {
	            		this.flushChunks(player);
	            	} else {
	            		this.doChunkLoading(player, msg.x, msg.z);
	            	}
				});
	            return null;
	        }

	        public void doChunkLoading(EntityPlayerMP player, int chunkX, int chunkZ) {
                WorldServer world = player.getServerWorld();
		        Set<ChunkPos> cameraChunks = new HashSet<>();
			    for (int dx = -CAMERA_RADIUS; dx <= CAMERA_RADIUS; dx++) {
			        for (int dz = -CAMERA_RADIUS; dz <= CAMERA_RADIUS; dz++) {
		               	cameraChunks.add(new ChunkPos(chunkX + dx, chunkZ + dz));
			        }
			    }
			    PlayerChunkMap playerchunkmap = world.getPlayerChunkMap();
			    for (ChunkPos pos : cameraChunks) {
		           	if (!world.isChunkGeneratedAt(pos.x, pos.z) || !playerchunkmap.isPlayerWatchingChunk(player, pos.x, pos.z)) {
		           		try {
							PlayerChunkMapEntry entry = (PlayerChunkMapEntry)playerChunkMap$getOrCreateEntry.invoke(playerchunkmap, pos.x, pos.z);
		 					entry.addPlayer(player);
		           		} catch (ReflectiveOperationException e) {
							throw new RuntimeException("Failed to create PlayerChunkMapEntry via playerChunkMap$getOrCreateEntry.invoke", e);
						}
			            this.cameraLoadedChunks.add(pos);
			        }
			    }
			    Set<ChunkPos> toUnload = new HashSet<>(this.cameraLoadedChunks);
			    toUnload.removeAll(cameraChunks);
			    for (ChunkPos pos : toUnload) {
	            	PlayerChunkMapEntry entry = playerchunkmap.getEntry(pos.x, pos.z);
	            	if (entry != null) {
	            		entry.removePlayer(player);
	            	}
			        this.cameraLoadedChunks.remove(pos);
			    }
			    // update camera loaded chunk's entities
	            Set<Integer> currentEntityIds = new HashSet<>();
			    for (ChunkPos pos : this.cameraLoadedChunks) {
			       	if (world.isChunkGeneratedAt(pos.x, pos.z)) {
						for (ClassInheritanceMultiMap<Entity> list : world.getChunkFromChunkCoords(pos.x, pos.z).getEntityLists()) {
						    for (Entity e : list) {
						    	if (!this.entityTrackers.containsKey(e.getEntityId())) {
		                       		this.entityTrackers.put(e.getEntityId(), new EntityTrackerEntry(e, 1024, 1024, 2, false));
						    	}
						    	currentEntityIds.add(e.getEntityId());
						    }
						}
			       	}
			    }
				List<EntityPlayer> list = ImmutableList.of(player);
				Iterator<Map.Entry<Integer, EntityTrackerEntry>> iter = this.entityTrackers.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<Integer, EntityTrackerEntry> entry = iter.next();
					if (currentEntityIds.contains(entry.getKey())) {
						entry.getValue().updatePlayerList(list);
					} else {
						entry.getValue().setMaxRange(-1);
						entry.getValue().updatePlayerEntity(player);
						iter.remove();
					}
				}
	        }

	        public void flushChunks(EntityPlayerMP player) {
	        	PlayerChunkMap playerchunkmap = player.getServerWorld().getPlayerChunkMap();
			    for (ChunkPos pos : this.cameraLoadedChunks) {
	            	PlayerChunkMapEntry entry = playerchunkmap.getEntry(pos.x, pos.z);
	            	if (entry != null) {
	            		entry.removePlayer(player);
	            	}
			    }
			    for (EntityTrackerEntry entry : this.entityTrackers.values()) {
					entry.setMaxRange(-1);
					entry.updatePlayerEntity(player);
			    }
			    this.cameraLoadedChunks.clear();
			    this.entityTrackers.clear();
	        }
	    }
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		new Renderer().register();
		this.elements.addNetworkMessage(PacketCameraPosition.Handler.class, PacketCameraPosition.class, Side.SERVER);
	}

	public static class Renderer extends EntityRendererRegister {
		@SideOnly(Side.CLIENT)
		@Override
		public void register() {
			RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, renderManager -> new RenderAltCamView(renderManager));
		}

		@SideOnly(Side.CLIENT)
		public class RenderAltCamView extends Render<EntityCustom> {
			public RenderAltCamView(RenderManager renderManagerIn) {
				super(renderManagerIn);
			}
	
			@Override
			public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
			}
	
			@Override
			protected ResourceLocation getEntityTexture(EntityCustom entity) {
				return null;
			}
		}
	}
}
