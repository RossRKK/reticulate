package xyz.reticulate.auth;

public interface IUsers {
	public String getAddressForUserName(String userName);

    public String getUserNameForAddress(String addr);

    //get the public ket for a specific user
    public byte[] getPublicKeyForUser(String addr);

    //get the public key for a user by using their user name
    public byte[] getPublicKeyForUserByName(String userName);

    //get the file that represents this users root directory
    public String getRootDirForUser(String addr);

    public String getRootDirForUserByName(String userName);

    ///add a file to the contracts register
    public boolean registerUser(String username, byte[] pubKey, String rootDir);

    ///check whether a username has been taken
    public boolean isUserNameTaken(String username);
}
