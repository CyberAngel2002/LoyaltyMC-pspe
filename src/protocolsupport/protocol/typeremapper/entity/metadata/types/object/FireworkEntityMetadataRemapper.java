package protocolsupport.protocol.typeremapper.entity.metadata.types.object;

import protocolsupport.protocol.packet.middleimpl.clientbound.play.v_pe.EntityMetadata.PeMetaBase;
import protocolsupport.protocol.typeremapper.entity.metadata.types.base.BaseEntityMetadataRemapper;
import protocolsupport.protocol.typeremapper.entity.metadata.value.IndexValueRemapperNoOp;
import protocolsupport.protocol.utils.ProtocolVersionsHelper;
import protocolsupport.protocol.utils.datawatcher.DataWatcherObjectIndex;

public class FireworkEntityMetadataRemapper extends BaseEntityMetadataRemapper {

	public FireworkEntityMetadataRemapper() {
		addRemap(new IndexValueRemapperNoOp(DataWatcherObjectIndex.Firework.ITEM, PeMetaBase.FIREWORK_TYPE), ProtocolVersionsHelper.ALL_PE);

		// Unfortunately, does not really help :(
//		addRemap(new FirstDataWatcherUpdateObjectAddRemapper(PeMetaBase.MINECART_OFFSET, new DataWatcherObjectVarInt(1)), ProtocolVersionsHelper.ALL_PE);
//		addRemap(new FirstDataWatcherUpdateObjectAddRemapper(PeMetaBase.MINECART_DISPLAY, new DataWatcherObjectByte((byte) 1)), ProtocolVersionsHelper.ALL_PE);

		addRemap(new IndexValueRemapperNoOp(DataWatcherObjectIndex.Firework.ITEM, 6), ProtocolVersionsHelper.RANGE__1_10__1_13_2);
		addRemap(new IndexValueRemapperNoOp(DataWatcherObjectIndex.Firework.ITEM, 5), ProtocolVersionsHelper.ALL_1_9);
		addRemap(new IndexValueRemapperNoOp(DataWatcherObjectIndex.Firework.ITEM, 8), ProtocolVersionsHelper.BEFORE_1_9);
		addRemap(new IndexValueRemapperNoOp(DataWatcherObjectIndex.Firework.USER, 7), ProtocolVersionsHelper.RANGE__1_11_1__1_13_2);
	}

}
