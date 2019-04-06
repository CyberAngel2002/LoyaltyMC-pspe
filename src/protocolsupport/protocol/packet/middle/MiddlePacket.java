package protocolsupport.protocol.packet.middle;

import java.util.Arrays;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.ConnectionImpl;
import protocolsupport.protocol.storage.netcache.NetworkDataCache;
import protocolsupport.utils.Utils;

public abstract class MiddlePacket {

	protected final ConnectionImpl connection;
	protected final NetworkDataCache cache;
	protected final ProtocolVersion version;
	public MiddlePacket(ConnectionImpl connection) {
		this.connection = connection;
		this.cache = connection.getCache();
		this.version = connection.getVersion();
	}

	@Override
	public String toString() {
		return Utils.toStringAllFields(this, Arrays.asList("connection", "cache"));
	}

}
