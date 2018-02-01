package poafs.gui.video;

import java.io.IOException;
import java.io.InputStream;

import uk.co.caprica.vlcj.player.media.callback.nonseekable.NonSeekableInputStreamMedia;

public class PoafsStreamMedia extends NonSeekableInputStreamMedia {
	
	private InputStream stream;
	
	public PoafsStreamMedia(InputStream video) {
		this.stream = video;
	}

	@Override
	protected InputStream onOpenStream() throws IOException {
		return stream;
	}

	@Override
	protected void onCloseStream(InputStream inputStream) throws IOException {
		inputStream.close();
	}

	@Override
	protected long onGetSize() {
		return 0;
	}

}
