
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
import net.minecraft.world.chunk.Chunk;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.inventory.EntityEquipmentSlot;

import net.narutomod.ElementsNarutomodMod;
import net.narutomod.NarutomodMod;

import io.netty.buffer.ByteBuf;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

@ElementsNarutomodMod.ModElement.Tag
public class EntityAltCamView extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 60;
	private static final int CAMERA_RADIUS = 5;

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
		private final Set<ChunkPos> cameraLoadedChunks = new HashSet<>();

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

		@SideOnly(Side.CLIENT)
		@Override
		public void onUpdate() {
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

		        // load additional chunks for the camera
		        Set<ChunkPos> cameraChunks = new HashSet<>();
		        for (int dx = -CAMERA_RADIUS; dx <= CAMERA_RADIUS; dx++) {
		            for (int dz = -CAMERA_RADIUS; dz <= CAMERA_RADIUS; dz++) {
	                	cameraChunks.add(new ChunkPos(this.chunkCoordX + dx, this.chunkCoordZ + dz));
		            }
		        }
		        for (ChunkPos pos : cameraChunks) {
	            	if (!this.cameraLoadedChunks.contains(pos) && !this.world.getChunkProvider().isChunkGeneratedAt(pos.x, pos.z)) {
		                PacketCameraChunkRequest.sendToServer(pos.x, pos.z, (byte)0);
		                this.cameraLoadedChunks.add(pos);
		            }
		        }
		        Set<ChunkPos> toUnload = new HashSet<>(this.cameraLoadedChunks);
		        toUnload.removeAll(cameraChunks);
		        for (ChunkPos pos : toUnload) {
		            this.unloadChunk(pos);
		            this.cameraLoadedChunks.remove(pos);
		        }
		        // update camera loaded chunk's entities on the server
		        for (ChunkPos pos : this.cameraLoadedChunks) {
		        	if (this.world.getChunkProvider().isChunkGeneratedAt(pos.x, pos.z)) {
		        		PacketCameraChunkRequest.sendToServer(pos.x, pos.z, (byte)1);
		        	}
		        }
			//} else {
			//	this.setDead();
			}
		}

		@SideOnly(Side.CLIENT)
		private void unloadChunk(ChunkPos pos) {
			WorldClient worldclient = (WorldClient)this.world;
        	worldclient.doPreChunk(pos.x, pos.z, false);
        	PacketCameraChunkRequest.sendToServer(pos.x, pos.z, (byte)2);
		    // 2) Remove any entities left behind in those chunks
		    /*Set<Entity> toRemove = new HashSet<>();
		    for (Entity e : worldclient.loadedEntityList) {
		        if (e.chunkCoordX == pos.x && e.chunkCoordZ == pos.z && !(e instanceof EntityPlayer)) {
		            toRemove.add(e);
		        }
		    }
		    // 3) Purge them from the client world
		    for (Entity e : toRemove) {
		        worldclient.removeEntityFromWorld(e.getEntityId());
		    }*/
		}

		@SideOnly(Side.CLIENT)
		@Override
		public void setDead() {
			super.setDead();
	        for (ChunkPos pos : this.cameraLoadedChunks) {
	        	this.unloadChunk(pos);
	        }
			this.cameraLoadedChunks.clear();
		}

		@Override
		protected void readEntityFromNBT(NBTTagCompound compound) {
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound compound) {
		}
	}

	/*@Override
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new ChunkEventHook());
	}

	public static class ChunkEventHook {
		@SubscribeEvent
		public void onChunkLoad(ChunkEvent.Load event) {
	        if (event.getWorld().isRemote) {
	            System.out.println("++++++ [Camera] Chunk loaded at " + event.getChunk().getPos());
	        }
		}

		@SubscribeEvent
		public void onChunkUnload(ChunkEvent.Unload event) {
	        if (event.getWorld().isRemote) {
	            System.out.println("++++++ [Camera] Chunk "+event.getChunk().getPos()+" unloaded");
	        }
		}
	}*/

	public static class PacketCameraChunkRequest implements IMessage {
	    int chunkX, chunkZ;
	    byte operation; // 0: load chunk and spawn entities; 1: update only; 2: unload chunk and destroy entities
	
	    public PacketCameraChunkRequest() {}
	
	    public PacketCameraChunkRequest(int x, int z, byte op) {
	        this.chunkX = x;
	        this.chunkZ = z;
	        this.operation = op;
	    }
	
	    @Override
	    public void toBytes(ByteBuf buf) {
	        buf.writeInt(chunkX);
	        buf.writeInt(chunkZ);
	        buf.writeByte(operation);
	    }
	
	    @Override
	    public void fromBytes(ByteBuf buf) {
	        this.chunkX = buf.readInt();
	        this.chunkZ = buf.readInt();
	        this.operation = buf.readByte();
	    }

	    public static void sendToServer(int x, int z, byte op) {
//System.out.println(">>>>>> sending packet to server, requesting chunk:("+x+", "+z+"), op:"+op);
	    	NarutomodMod.PACKET_HANDLER.sendToServer(new PacketCameraChunkRequest(x, z, op));
	    }
	
	    public static class Handler implements IMessageHandler<PacketCameraChunkRequest, IMessage> {
	    	private Map<ChunkPos, Set<Integer>> chunkEntityMap = new HashMap<>();
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
	        public IMessage onMessage(PacketCameraChunkRequest msg, MessageContext ctx) {
	        	EntityPlayerMP player = ctx.getServerHandler().player;
                WorldServer world = player.getServerWorld();	
	            world.addScheduledTask(() -> {
	            	Chunk chunk = world.getChunkFromChunkCoords(msg.chunkX, msg.chunkZ);
	            	ChunkPos pos = chunk.getPos();
//System.out.println("====== server received packet, player:"+player.getName()+", requested chunk:("+msg.chunkX+", "+msg.chunkZ+"), op:"+msg.operation);
	            	Set<Integer> previousEntityIds = this.chunkEntityMap.getOrDefault(pos, new HashSet<>());
	            	Set<Integer> currentEntityIds = new HashSet<>();
	            	if (msg.operation == 0) {
	            		try {
							PlayerChunkMapEntry entry = (PlayerChunkMapEntry)playerChunkMap$getOrCreateEntry.invoke(world.getPlayerChunkMap(), pos.x, pos.z);
	 						entry.addPlayer(player);
							entry.sendToPlayer(player);
	            		} catch (ReflectiveOperationException e) {
							throw new RuntimeException("Failed to create PlayerChunkMapEntry via playerChunkMap$getOrCreateEntry.invoke", e);
						}
						for (ClassInheritanceMultiMap<Entity> list : chunk.getEntityLists()) {
						    for (Entity e : list) {
							    // 1) Get and send the spawn packet
							    Packet<?> spawnPkt = FMLNetworkHandler.getEntitySpawningPacket(e);
							    if (spawnPkt == null) {
								    if (e instanceof EntityPlayer) {
								      	spawnPkt = new SPacketSpawnPlayer((EntityPlayer)e);
								    } else if (e instanceof EntityLivingBase) {
								        spawnPkt = new SPacketSpawnMob((EntityLivingBase)e);
								    }
							    }
							    if (spawnPkt != null) {
							        player.connection.sendPacket(spawnPkt);
							    }
		                        // properties, equipment slots
		                        if (e instanceof EntityLivingBase) {
		                            EntityLivingBase living = (EntityLivingBase) e;
		                            player.connection.sendPacket(new SPacketEntityProperties(e.getEntityId(), living.getAttributeMap().getAllAttributes()));
		                            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
		                                if (!living.getItemStackFromSlot(slot).isEmpty()) {
		                                    player.connection.sendPacket(new SPacketEntityEquipment(e.getEntityId(), slot, living.getItemStackFromSlot(slot)));
		                                }
		                            }
		                        }
							    // 2) Send metadata (health, custom flags, etc.)
							    player.connection.sendPacket(new SPacketEntityMetadata(e.getEntityId(), e.getDataManager(), true));
		                        // velocity always
		                        player.connection.sendPacket(new SPacketEntityVelocity(e));
	                           	currentEntityIds.add(e.getEntityId());
						    }
						}
	            	} else if (msg.operation == 1) {
						for (ClassInheritanceMultiMap<Entity> list : chunk.getEntityLists()) {
						    for (Entity e : list) {
		                        long dx = EntityTracker.getPositionLong(e.posX) - EntityTracker.getPositionLong(e.prevPosX);
		                        long dy = EntityTracker.getPositionLong(e.posY) - EntityTracker.getPositionLong(e.prevPosY);
		                        long dz = EntityTracker.getPositionLong(e.posZ) - EntityTracker.getPositionLong(e.prevPosZ);
		                        int i = MathHelper.floor(e.rotationYaw * 256.0F / 360.0F);
		                        int j = MathHelper.floor(e.rotationPitch * 256.0F / 360.0F);
		                        //if (dx != 0L || dy != 0L || dz != 0L || i != MathHelper.floor(e.prevRotationYaw * 256.0F / 360.0F) || j != MathHelper.floor(e.prevRotationPitch * 256.0F / 360.0F)) {
		                        	Packet<?> pkt = Math.abs(dx) < 32768L && Math.abs(dy) < 32768L && Math.abs(dz) < 32768L
		                        	 ? new SPacketEntity.S17PacketEntityLookMove(e.getEntityId(), dx, dy, dz, (byte)i, (byte)j, e.onGround)
			                         : new SPacketEntityTeleport(e);
		                        	player.connection.sendPacket(pkt);
		                        //}
		                        if (e instanceof EntityLivingBase) {
		                        	int k = MathHelper.floor(e.getRotationYawHead() * 256.0F / 360.0F);
		                        	if (k != MathHelper.floor(((EntityLivingBase)e).prevRotationYawHead * 256.0F / 360.0F)) {
		                        		player.connection.sendPacket(new SPacketEntityHeadLook(e, (byte)k));
		                        	}
		                        }
		                        // velocity always
		                        player.connection.sendPacket(new SPacketEntityVelocity(e));
	                           	currentEntityIds.add(e.getEntityId());
						    }
						}
	            	} else {
	            		PlayerChunkMapEntry entry = world.getPlayerChunkMap().getEntry(pos.x, pos.z);
	            		if (entry != null) {
	            			entry.removePlayer(player);
	            		}
						for (ClassInheritanceMultiMap<Entity> list : chunk.getEntityLists()) {
						    for (Entity e : list) {
	                           	currentEntityIds.add(e.getEntityId());
						    }
						}
					}
					// Determine which entities have been removed
					Set<Integer> removedEntityIds = new HashSet<>(previousEntityIds);
					removedEntityIds.removeAll(currentEntityIds);
					if (msg.operation == 2) {
						removedEntityIds.addAll(currentEntityIds);
						currentEntityIds.clear();
					}
					// Send destroy packets for removed entities
					if (!removedEntityIds.isEmpty()) {
					    int[] idsToDestroy = removedEntityIds.stream().mapToInt(Integer::intValue).toArray();
					    player.connection.sendPacket(new SPacketDestroyEntities(idsToDestroy));
					}
					// Update the record for the next comparison
					if (!currentEntityIds.isEmpty()) {
						this.chunkEntityMap.put(pos, currentEntityIds);
					} else {
						this.chunkEntityMap.remove(pos);
					}
				});
	            return null;
	        }
	    }
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		new Renderer().register();
		this.elements.addNetworkMessage(PacketCameraChunkRequest.Handler.class, PacketCameraChunkRequest.class, Side.SERVER);
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
