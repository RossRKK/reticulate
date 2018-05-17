/*
 * This is a utility file designed for making
 * integrating web apps with the reticulate network easier.
 * It includes utility functions for interacting with the network.
 *
 * Requires jQuery
 *
 * Author: Ross Kelso
*/

const EMPTY_DIR = ".. dir ";

var Reticulate = (function () {

    //get the id of the peer
    async function peerId() {
        return await $.ajax({
            url: "/peer/id",
            method: "GET"
        });
    }

    //get the wallet address of the logged in user
    async function addr() {
        return await $.ajax({
            url: "/peer/addr",
            method: "GET"
        });
    }

    //get the public key of the logged in user (base64)
    async function key() {
        return await $.ajax({
            url: "/peer/key",
            method: "GET"
        });
    }

    //add a file to the network with specified contents
    async function addFile(content) {
        return await $.ajax({
            url: "/file",
            method: "POST",
            data: content
        });
    }

    //get the contents of a file
    async function getFile(fileId) {
        return await $.ajax({
            url: "/file/" + encodeURIComponent(fileId),
            method: "GET"
        });
    }

    //update the contents of a file
    async function updateFile(fileId, content) {
        return await $.ajax({
            url: "/file/" + encodeURIComponent(fileId),
            method: "PUT",
            data: content
        });
    }

    //remove a file from the network
    async function deleteFile(fileId) {
        return await $.ajax({
            url: "/file/" + encodeURIComponent(fileId),
            method: "DELETE"
        });
    }

    //share a file with someone
    async function share(fileId, userAddress, userKey, accessLevel) {
        return await $.ajax({
            url: "/file/" + encodeURIComponent(fileId) + "/share?userAddress=" + encodeURIComponent(userAddress) + "&userKey=" + encodeURIComponent(userKey) + "&accessLevel=" + encodeURIComponent(accessLevel),
            method: "POST"
        });
    }

    //get a users access level to a file
    async function getAccess(fileId, userAddress) {
        return await $.ajax({
            url: "/file/" + encodeURIComponent(fileId) + "/share/" + encodeURIComponent(userAddress),
            method: "GET"
        });
    }

    //revoke a users access to a file
    async function revokeShare(fileId, userAddress) {
        return await $.ajax({
            url: "/file/" + encodeURIComponent(fileId) + "/share/" + encodeURIComponent(userAddress),
            method: "DELETE"
        });
    }

    //modify a users access level
    async function modifyAccess(fileId, userAddress, accessLevel) {
        return await $.ajax({
            url: "/file/" + encodeURIComponent(fileId) + "/share/" + encodeURIComponent(userAddress) + "&accessLevel=" + encodeURIComponent(accessLevel),
            method: "PUT"
        });
    }


    //functions for handling user requests
    var Users = (function () {

        //register a new user
        async function registerUser(username, userKey, rootDir) {

            let taken = await isUserNameTaken(username);
            if (!taken) {
                return (await $.ajax({
                    url: "/user?username=" + encodeURIComponent(username) + "&userKey=" + encodeURIComponent(userKey) + "&rootDir=" + encodeURIComponent(rootDir),
                    method: "POST"
                })) == "true";
            } else {
                return false;
            }
        }

        //register the current user with a username
        async function registerCurrentUser(username) {
            let rootDir = await addFile(EMPTY_DIR + "root\n");

            let userKey = await key();

            return registerUser(username, userKey, rootDir);
        }

        async function getAddressForUserName(username) {
            return await $.ajax({
                url: "/user/name/" + encodeURIComponent(username) + "/addr",
                method: "GET"
            });
        }

        async function getUserNameForAddress(addr) {
            return await $.ajax({
                url: "/user/addr/" + encodeURIComponent(addr) + "/username",
                method: "GET"
            });
        }

        async function getKeyForUser(nameOrAddr, isAddr) {
            let path = isAddr ? "/user/addr/" : "/user/name/";
            return await $.ajax({
                url: path + encodeURIComponent(nameOrAddr) + "/key",
                method: "GET"
            });
        }

        async function getRootDirForUser(nameOrAddr, isAddr) {
            let path = isAddr ? "/user/addr/" : "/user/name/";
            return await $.ajax({
                url: path + encodeURIComponent(nameOrAddr) + "/rootDir",
                method: "GET"
            });
        }

        async function isUserNameTaken(username) {
            return (await $.ajax({
                url: "/user/" + encodeURIComponent(username),
                method: "GET"
            })) == "true";
        }

        return {
            registerUser,
            registerCurrentUser,
            getAddressForUserName,
            getUserNameForAddress,
            getKeyForUser,
            getRootDirForUser,
            isUserNameTaken,
        }
    })();

    //public exports
    return {
        Users,
        peerId,
        addr,
        key,
        addFile,
        getFile,
        updateFile,
        deleteFile,
        share,
        getAccess,
        revokeShare,
        modifyAccess
    }
})();
