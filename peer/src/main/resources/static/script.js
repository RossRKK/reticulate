"use strict";

//constants for access levels
const NONE = 0;
const READ = 1;
const WRITE = 2;
const ADMIN = 3;

const FILE_PATH = "/file/";

const DIR = "dir";
const FILE = "file";

$(function () {
    Model.init();
    Controller.init();
});

/*Model, view controller pattern*/

var Model = (function () {
    //the id of the current peer
    let peerId;

    //the address of the current user
    let addr;

    //the public key of the current user
    let key;

    //the current file id
    let fileId;

    //the access level to the current file id
    let accessLevel;

    //the assigned username for the current user
    let username;

    //the registered root directory for the current user.
    let rootDir;

    //the directory object for the current dir
    let currentDir;

    //the list of entries we went through to get here
    let breadCrumbs = [];

    function init() {
        Reticulate.Peer.peerId().then(function (result) {
            peerId = result;
            View.setPeerId(peerId);
        });
        Reticulate.Peer.addr().then(async function (result) {
            addr = result;
            View.setUserAddress(addr);

            username = await Reticulate.Users.getUserNameForAddress(addr);
            View.setUsername(username);

            if (username) {
                //get the users root dir
                Reticulate.Users.getRootDirForUser(addr, true).then(async function (result) {
                    rootDir = result;

                    View.setRootDir(rootDir);
                });
            } else {
                //allow the user to register
                View.switchToRegisterUsername();
            }
        });
        Reticulate.Peer.key().then(function (result) {
            key = result;
            View.setPublicKey(key);
        });



        updateFileId(fileId);
    }

    //update the current file
    async function updateFileId(id) {
        fileId = id;

        let request = Reticulate.Net.getAccess(fileId, addr);

        request.then(function (strLevel) {
            accessLevel = parseInt(strLevel);
        });

        request.catch(function () {
            accessLevel = NONE;
        })

        request.finally(function () {
            View.updateAccess(accessLevel);
            View.updateFileId(fileId);
        });
    }

    function copyToClipboard (str) {
        const el = document.createElement('textarea');
        el.value = str;
        document.body.appendChild(el);
        el.select();
        document.execCommand('copy');
        document.body.removeChild(el);
    }

    function copyPeer() {
        copyToClipboard(peerId);
    }

    function copyAddr() {
        copyToClipboard(addr);
    }

    function copyKey() {
        copyToClipboard(key);
    }

    async function registerUsername(username) {
        let isTaken = await Reticulate.Users.isUserNameTaken(username);

        if (isTaken) {
            alert("Username is already taken, choose another");
        } else {
            $("#username-btn").off("click");

            View.usernameSpinner();

            //this bit should really be in the model
            if (await Reticulate.Users.registerCurrentUser(username)) {

                //reinitialise the model after registration succeeds
                Model.init();
            } else {
                alert("Registration failed");
            }
        }
    }

    return {
        init,
        updateFileId,
        copyPeer,
        copyAddr,
        copyKey,
        registerUsername,
    }
})();

var View = (function () {
    //the mximum number of characters that should be visible in the key
    const MAX_LENGTH = 20;



    function setPeerId(id) {
        $("#peer-id").text(id);
    }

    function setUserAddress(addr) {
        $("#addr").text(addr);
    }

    function setPublicKey(key) {
        $("#key").text(key.substring(0, MAX_LENGTH) + "...");
    }

    function updateAccess(accessLevel) {
        $(".file-action").hide();
        switch (accessLevel) {
            //all adimin levels
            default:
            case ADMIN:
                $("#file-share").show();
                $("#file-settings").show();
            //write
            case WRITE:
                $("#file-write").show();
            //read
            case READ:
                $("#file-download").show();
            //no access
            case NONE:
        }

        switch (accessLevel) {
            //all adimin levels
            default:
            case ADMIN:
                $("#file-access-level").text("Admin");
                break;
            //write
            case WRITE:
                $("#file-access-level").text("Write");
                break;
            //read
            case READ:
                $("#file-access-level").text("Read");
                break;
            //no access
            case NONE:
                $("#file-access-level").text("None");
                break;
        }
    }

    function updateFileId(fileId) {
        document.getElementById("file-download").href = "/file/" + fileId;
    }


    //function for making icons
    function makeIconCol(faIcon) {
        let iconCol = $("<div>", {
            class: "col-1",
        });

        iconCol.append(makeIcon(faIcon));

        return iconCol;
    }

    function makeIcon(faIcon) {
        return $("<i>", {
            class: faIcon,
        });
    }

    function switchToRegisterUsername() {
        //hide the
        $("#userExists").hide();

        $("#registerUser").show();
    }

    function usernameSpinner() {
        $("#username-btn").empty();
        $("#username-btn").append(makeIcon("fas fa-lg fa-spin fa-spinner"));
    }

    function setUsername(username) {
        $("#username").text(username);
    }

    function setRootDir(rootDir) {
        $("#rootDir").text(rootDir);
    }


    return {
        setPeerId,
        setUserAddress,
        setPublicKey,
        updateAccess,
        updateFileId,
        setRootDir,
        setUsername,
        switchToRegisterUsername,
        usernameSpinner,
    }
})();

var Controller = (function () {

    function init() {
        //register event handlers
        $("#file-id-in").on("change keypress", handleFileId);

        //copy buttons
        $("#copy-peer").on("click", Model.copyPeer);
        $("#copy-addr").on("click", Model.copyAddr);
        $("#copy-key").on("click", Model.copyKey);

        $("#username-in").on("change keypress", updateUsername);
        $("#username-btn").on("click", submitUsername);
    }

    async function submitUsername() {
        let username = $("#username-in").val();

        Model.registerUsername(username);
    }

    //update the colour of the username button
    async function updateUsername(e) {
        let username = $("#username-in").val();

        //this bit should really be in the model
        let isTaken = username ? await Reticulate.Users.isUserNameTaken(username) : true;

        //tghis bit should really be in the view
        if (!isTaken) {
            $("#username-btn").removeClass("btn-outline-danger");
            $("#username-btn").addClass("btn-outline-success");
        } else {
            $("#username-btn").removeClass("btn-outline-success");
            $("#username-btn").addClass("btn-outline-danger");
        }
    }

    function handleFileId(e) {
        let fileId = $("#file-id-in").val();
        if (fileId) {
            Model.updateFileId(fileId);
        }
    }


    return {
        init,
        handleFileId,
    }
})();
