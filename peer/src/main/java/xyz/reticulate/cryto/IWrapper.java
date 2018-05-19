package xyz.reticulate.cryto;

import java.security.Key;

public interface IWrapper {
	/**
	 * Wrap a key using this wrapper.
	 * @param key The key to be wrapped.
	 * @return The wrapped key.
	 */
	public byte[] wrap(Key key);
}
