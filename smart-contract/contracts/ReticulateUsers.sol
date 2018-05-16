pragma solidity ^0.4.0;


contract ReticulateUsers {

    //struct representing a user
    struct User {
        //the username assigned to this user
        string userName;
        //the public key for this user
        bytes publicKey;
        //the id of the file that represents this users root directory
        string rootDir;
    }

    struct UserName {
        address user;
        bool isTaken;
    }

    ///the owner of the contract
    address private owner;

    mapping(string => UserName) private userNames;

    mapping(address => User) private users;

    ///create a new
    function ReticulateUsers() public {
        owner = msg.sender;
    }

    ///turn a username into an actual address
    function getAddressForUserName(string userName) public view returns(address) {
        return userNames[userName].user;
    }

    function getUserNameForAddress(address addr) public view returns(string) {
        return users[addr].userName;
    }

    //get the public ket for a specific user
    function getPublicKeyForUser(address addr) public view returns(bytes) {
        return users[addr].publicKey;
    }

    //get the public key for a user by using their user name
    function getPublicKeyForUserByName(string userName) public view returns(bytes) {
        address addr = getAddressForUserName(userName);
        return getPublicKeyForUser(addr);
    }

    //get the file that represents this users root directory
    function getRootDirForUser(address addr) public view returns(string) {
        return users[addr].rootDir;
    }

    function getRootDirForUserByName(string userName) public view returns(string) {
        address addr = getAddressForUserName(userName);
        return getRootDirForUser(addr);
    }

    ///add a file to the contracts register
    function registerUser(string username, bytes pubKey, string rootDir) public returns(bool) {
        users[msg.sender] = User({
            userName: username,
            publicKey: pubKey,
            rootDir: rootDir
        });

        if (!isUserNameTaken(username)) {
            userNames[username] = UserName({
                user: msg.sender,
                isTaken: true
            });
            return true;
        } else {
            users[msg.sender].userName = "";
            return false;
        }
    }

    ///check whether a username has been taken
    function isUserNameTaken(string username) public view returns(bool) {
        return userNames[username].isTaken;
    }

    ///destroy the contract
    function destruct() public {
        //only the owner can destroy the contract
        if (msg.sender == owner) {
            selfdestruct(owner);
        }
    }
}
