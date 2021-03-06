package buildcraft.core.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	private void onTileUpdate( EntityPlayer player, PacketTileUpdate packet) {
		World world = player.worldObj;

		if (!packet.targetExists(world))
			return;

		TileEntity entity = packet.getTarget(world);
		if (!(entity instanceof ISynchronizedTile))
			return;

		ISynchronizedTile tile = (ISynchronizedTile) entity;
		tile.handleUpdatePacket(packet);
		tile.postPacketHandling(packet);
	}

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {
		 DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {

			int packetID = data.read();
			switch (packetID) {
			case PacketIds.TILE_UPDATE:
				PacketTileUpdate packetT = new PacketTileUpdate();
				packetT.readData(data);
				onTileUpdate((EntityPlayer)player, packetT);
				break;

			case PacketIds.STATE_UPDATE:
				PacketTileState inPacket = new PacketTileState();
				inPacket.readData(data);
				World world = ((EntityPlayer)player).worldObj;
				TileEntity tile = world.getBlockTileEntity(inPacket.posX, inPacket.posY, inPacket.posZ);
				if (tile instanceof ISyncedTile){
					inPacket.applyStates(data, (ISyncedTile) tile);
				}
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
