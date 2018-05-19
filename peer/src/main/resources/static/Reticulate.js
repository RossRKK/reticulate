/*
 * This is a utility file designed for making
 * integrating web apps with the reticulate network easier.
 * It includes utility functions for interacting with the network.
 *
 * Requires jQuery
 *
 * Author: Ross Kelso
*/

const ROOT_DIR = "../dir/\n";

var Reticulate = (function () {

    let domain = "http://localhost:4567";

    function setDomain(d) {
        domain = d;
    }

    function getDomain() {
        return domain;
    }

    //module for handling the peer
    var Peer = (function () {
        //get the id of the peer
        async function peerId() {
            return await $.ajax({
                url: domain + "/peer/id",
                method: "GET"
            });
        }

        //get the wallet address of the logged in user
        async function addr() {
            return await $.ajax({
                url: domain + "/peer/addr",
                method: "GET"
            });
        }

        //get the public key of the logged in user (base64)
        async function key() {
            return await $.ajax({
                url: domain + "/peer/key",
                method: "GET"
            });
        }

        return {
            peerId,
            addr,
            key,
        }
    })();

    //module for hadnling the network
    var Net = (function () {
        //add a file to the network with specified contents (endcoded in base64)
        async function addFile(content) {
            return await $.ajax({
                url: domain + "/file",
                method: "POST",
                contentType: "application/base64",
                data: content
            });
        }
        
        //get the contents of a file
        async function getFile(fileId) {
            return await $.ajax({
                url: domain + "/file/" + encodeURIComponent(fileId),
                method: "GET"
            });
        }

        //update the contents of a file
        async function updateFile(fileId, content) {
            return await $.ajax({
                url: domain + "/file/" + encodeURIComponent(fileId),
                method: "PUT",
                data: content
            });
        }

        //remove a file from the network
        async function deleteFile(fileId) {
            return await $.ajax({
                url: domain + "/file/" + encodeURIComponent(fileId),
                method: "DELETE"
            });
        }

        //share a file with someone
        async function share(fileId, userAddress, userKey, accessLevel) {
            return await $.ajax({
                url: domain + "/file/" + encodeURIComponent(fileId) + "/share?userAddress=" + encodeURIComponent(userAddress) + "&userKey=" + encodeURIComponent(userKey) + "&accessLevel=" + encodeURIComponent(accessLevel),
                method: "POST"
            });
        }

        //get a users access level to a file
        async function getAccess(fileId, userAddress) {
            return await $.ajax({
                url: domain + "/file/" + encodeURIComponent(fileId) + "/share/" + encodeURIComponent(userAddress),
                method: "GET"
            });
        }

        //revoke a users access to a file
        async function revokeShare(fileId, userAddress) {
            return await $.ajax({
                url: domain + "/file/" + encodeURIComponent(fileId) + "/share/" + encodeURIComponent(userAddress),
                method: "DELETE"
            });
        }

        //modify a users access level
        async function modifyAccess(fileId, userAddress, accessLevel) {
            return await $.ajax({
                url: domain + "/file/" + encodeURIComponent(fileId) + "/share/" + encodeURIComponent(userAddress) + "&accessLevel=" + encodeURIComponent(accessLevel),
                method: "PUT"
            });
        }

        return {
            addFile,
            getFile,
            updateFile,
            deleteFile,
            share,
            getAccess,
            revokeShare,
            modifyAccess,
            setDomain,
            getDomain,
        }
    })();

    //module for handling user requests
    var Users = (function () {

        //register a new user
        async function registerUser(username, userKey, rootDir) {

            let taken = await isUserNameTaken(username);
            if (!taken) {
                return (await $.ajax({
                    url: domain + "/user?username=" + encodeURIComponent(username) + "&userKey=" + encodeURIComponent(userKey) + "&rootDir=" + encodeURIComponent(rootDir),
                    method: "POST"
                })) == "true";
            } else {
                return false;
            }
        }

        //register the current user with a username
        async function registerCurrentUser(username) {
            let rootDir = await Net.addFile(btoa(ROOT_DIR));

            let userKey = await Peer.key();

            return registerUser(username, userKey, rootDir);
        }

        async function getAddressForUserName(username) {
            return await $.ajax({
                url: domain + "/user/name/" + encodeURIComponent(username) + "/addr",
                method: "GET"
            });
        }

        async function getUserNameForAddress(addr) {
            return await $.ajax({
                url: domain + "/user/addr/" + encodeURIComponent(addr) + "/username",
                method: "GET"
            });
        }

        async function getKeyForUser(nameOrAddr, isAddr) {
            let path = isAddr ? "/user/addr/" : "/user/name/";
            return await $.ajax({
                url: domain + path + encodeURIComponent(nameOrAddr) + "/key",
                method: "GET"
            });
        }

        async function getRootDirForUser(nameOrAddr, isAddr) {
            let path = isAddr ? "/user/addr/" : "/user/name/";
            return await $.ajax({
                url: domain + path + encodeURIComponent(nameOrAddr) + "/rootDir",
                method: "GET"
            });
        }

        async function isUserNameTaken(username) {
            return (await $.ajax({
                url: domain + "/user/" + encodeURIComponent(username),
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

    //module for handling directories
    var Directory = (function () {

        class entry {
            constructor(name, type, id) {
                this.text = name + "/" + type + "/" + id + "\n";

                this.name = name;
                this.type = type;
                this.id = id;
            }
        }

        class dir {
            constructor(id, content) {
                this.id = id;
                this.content = content;

                this._parseEntries();
            }

            //add a new directory or file to this directory
            addEntry(entry) {
                if (this.getFileId(entry.name) == null) {
                    this.entries.push(entry);
                    this.content += entry.text;

                    Net.updateFile(this.id, this.content);

                    return true;
                } else {
                    return false;
                }
            }

            //remove an entry from this directory, this doesn't delete the file
            removeEntry(id) {
                //remove the entry
                this.entries = this.entries.filter(e => e.id !== id);

                //reset the content
                this.content = "";

                //add each entry back to the content
                this.entries.forEach(entry => {
                    this.content += entry.text;
                });

                //upload the updated content
                Net.updateFile(this.id, this.content);
            }

            //get the id of a file in this directory with the given name
            getFileId(name) {
                let matches = this.entries.filter(e => e.name === name);
                if (matches.length > 0) {
                    return matches[0];
                } else {
                    return null;
                }
            }

            //turn the text content of the directory into entry objects
            _parseEntries() {
                this.entries = [];

                let entryStrs = this.content.split("\n");

                this.entries = entryStrs.filter(str => str !== "").map(line => {
                    let fields = line.split("/");
                    return new entry(fields[0], fields[1], fields[2]);
                });
            }
        }

        return {
            entry,
            dir,
        }
    })();

    //public exports
    return {
        setDomain,
        Users,
        Net,
        Peer,
        Directory,
    }
})();
