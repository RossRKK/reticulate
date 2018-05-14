/*
 * This is a utility file designed for making
 * integrating web apps with the reticulate network easier.
 * It includes utility functions for interacting with the network.
 *
 * Requires jQuery
 *
 * Author: Ross Kelso
*/

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
            url: "/file/" + fileId,
            method: "GET"
        });
    }

    //update the contents of a file
    async function updateFile(fileId, content) {
        return await $.ajax({
            url: "/file/" + fileId,
            method: "PUT",
            data: content
        });
    }

    //remove a file from the network
    async function deleteFile(fileId) {
        return await $.ajax({
            url: "/file/" + fileId,
            method: "DELETE"
        });
    }

    //share a file with someone
    async function share(fileId, userAddress, userKey, accessLevel) {
        return await $.ajax({
            url: "/file/" + fileId + "/share?userAddress=" + userAddress + "&userKey=" + userKey + "&accessLevel=" + accessLevel,
            method: "POST"
        });
    }

    //get a users access level to a file
    async function getAccess(fileId, userAddress) {
        return await $.ajax({
            url: "/file/" + fileId + "/share/" + userAddress,
            method: "GET"
        });
    }

    //revoke a users access to a file
    async function revokeShare(fileId, userAddress) {
        return await $.ajax({
            url: "/file/" + fileId + "/share/" + userAddress,
            method: "DELETE"
        });
    }

    //modify a users access level
    async function modifyAccess(fileId, userAddress, accessLevel) {
        return await $.ajax({
            url: "/file/" + fileId + "/share/" + userAddress + "&accessLevel=" + accessLevel,
            method: "PUT"
        });
    }

    //public exports
    return {
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
