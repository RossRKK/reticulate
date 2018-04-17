pragma solidity ^0.4.0;


contract ReticulateAuth {

    /*uint READ = 1;
    uint WRITE = 2;
    uint ADMIN = 3;*/

    ///the lminimum level considered an admin
    uint private constant ADMIN = 3;
    uint private constant WRITE = 2;

    //definition for a file type
    struct File {
        //the number of blocks in this file
        uint length;
        //the encrypted keys for each user with access
        mapping(address => Permission) permissions;
        //the sha256 checksums for specified blocks (optional)
        mapping(uint => bytes) checkSums;
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

    //get the length of the file
    function getFileLength(string fileId) public view returns(uint) {
        return files[fileId].length;
    }

    //get a users access level for a file
    function getAccessLevel(string fileId, address user) public view returns(uint) {
        return files[fileId].permissions[user].level;
    }

    ///add a file to the contracts register
    function addFile(string fileId, bytes key, uint length) public {
        files[fileId] = File({
            length: length
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
    function modifyAccessLevel(string fileId, address user, uint level) public {
        File storage file = files[fileId];
        if (file.permissions[msg.sender].level >= ADMIN
            && file.permissions[msg.sender].level >= level) {
            file.permissions[user].level = level;
        }
    }

    //increase the length of a file
    function updateFileLength(string fileId, uint newLength) public {
        File storage file = files[fileId];

        if (file.permissions[msg.sender].level >= WRITE) {
            //set the checksum
            file.length = newLength;
        }
    }

    ///add the checkSum for a specific block in a file
    function updateCheckSum(string fileId, uint blockIndex, bytes checkSum) public {
        File storage file = files[fileId];

        //only allow the checksum to be set by someone with write permission
        if (file.permissions[msg.sender].level >= WRITE) {
            //set the checksum
            file.checkSums[blockIndex] = checkSum;
        }
    }

    //get the checksum for a specified block in a file
    function getCheckSum(string fileId, uint blockIndex) public view returns (bytes) {
        return files[fileId].checkSums[blockIndex];
    }

    //compare a provided checkSum to the recorded checksum
    function compareCheckSum(string fileId, uint blockIndex, bytes checkSum) public view returns (bool) {
        bytes storage actual = files[fileId].checkSums[blockIndex];
        if (checkSum.length == actual.length) {
            //check every byte
            for (uint i = 0; i < actual.length; i++) {
                //if any byte is wrong return false
                if (actual[i] != checkSum[i]) {
                    return false;
                }
            }
            //all bytes matched so return true
            return true;
        } else {
            //length mismatch
            return false;
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
