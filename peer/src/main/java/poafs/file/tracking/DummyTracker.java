package poafs.file.tracking;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import poafs.Application;
import poafs.exception.ProtocolException;
import poafs.lib.Reference;

public class DummyTracker implements ITracker {

	@Override
	public InetSocketAddress getHostForPeer(String peerId) throws ProtocolException {
		return new InetSocketAddress("localhost", Reference.DEFAULT_PORT);
	}

	@Override
	public List<String> findBlock(String fileId, int blockIndex) throws ProtocolException {
		List<String> list = new LinkedList<String>();
		list.add(Application.getPropertiesManager().getPeerId());
		return list;
	}

	@Override
	public void registerTransfer(String fileId, int index) throws ProtocolException {
		// TODO Auto-generated method stub

	}

}
