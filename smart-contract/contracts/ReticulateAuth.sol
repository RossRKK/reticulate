pragma solidity ^0.4.0;


contract ReticulateAuth {

    /*uint READ = 1;
    uint WRITE = 2;
    uint ADMIN = 3;*/

    ///the lminimum level considered an admin
    uint private constant ADMIN = 3;

    //definition for a file type
    struct File {
        //the firendly name of the file
        string name;
        //the encrypted keys for each user with access
        mapping(address => Permission) permissions;
    }

    //represents a users Permission on a file
    struct Permission {
        bytes key;
        uint level;
    }

    ///the owner of the contract
    address private owner;

    ///all of the files on the network
    mapping(string => File) private files;

    ///create a new BallotBox
    function ReticulateAuth() public {
        owner = msg.sender;
    }

    ///get the key for a file
    function getKeyForFile(string fileId) public view returns(bytes) {
        return files[fileId].permissions[msg.sender].key;
    }

    //get the name of the file
    function getFileName(string fileId) public view returns(string) {
        return files[fileId].name;
    }

    //get a users access level for a file
    function getAccessLevel(string fileId, address user) public view returns(uint) {
        return files[fileId].permissions[user].level;
    }

    ///add a file to the contracts register
    function addFile(string fileId, string name, bytes key) public {
        files[fileId] = File({
            name: name
        });

        files[fileId].permissions[msg.sender] = Permission(key, ADMIN);
    }

    ///remove a file from the network
    function removeFile(string fileId) public {
        if (files[fileId].permissions[msg.sender].level >= ADMIN) {
            delete files[fileId];
        }
    }

    ///share a file with another user
    function shareFile(string fileId, address recipient, bytes recipientKey, uint level) public {
        File storage file = files[fileId];
        //check that the sender has permission to perfrom this operation
        if (file.permissions[msg.sender].level >= ADMIN && file.permissions[msg.sender].level >= level) {
            files[fileId].permissions[recipient] = Permission(recipientKey, level);
        }
    }

    ///revoke a share to a file
    function revokeShare(string fileId, address revokee) public {
        File storage file = files[fileId];
        if (file.permissions[msg.sender].level >= ADMIN
            && file.permissions[msg.sender].level >= files[fileId].permissions[revokee].level) {
            delete files[fileId].permissions[revokee];
        }
    }

    ///modify the access level of a user
    function modifyAccesLevel(string fileId, address user, uint level) public {
        File storage file = files[fileId];
        if (file.permissions[msg.sender].level >= ADMIN
            && file.permissions[msg.sender].level >= level) {
            file.permissions[user].level = level;
        }
    }

    ///destroy the contract
    function destruct() public {
        //only the owner can destroy the contract
        if (msg.sender == owner) {
            selfdestruct(owner);
        }
    }
}
