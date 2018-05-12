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
            url: "/file/" + fileId + "/share?userAddress=" + userAddress,
            method: "GET"
        });
    }

    //revoke a users access to a file
    async function revokeShare(fileId, userAddress) {
        return await $.ajax({
            url: "/file/" + fileId + "/share?userAddress=" + userAddress,
            method: "DELETE"
        });
    }

    //modify a users access level
    async function modifyAccess(fileId, userAddress, accessLevel) {
        return await $.ajax({
            url: "/file/" + fileId + "/share?userAddress=" + userAddress + "&accessLevel=" + accessLevel,
            method: "PUT"
        });
    }

    //public exports
    return {
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
