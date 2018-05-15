//constants for access levels
const NONE = 0;
const READ = 1;
const WRITE = 2;
const ADMIN = 3;

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

    function init() {
        Reticulate.peerId().then(function (result) {
            peerId = result;
            View.setPeerId(peerId);
        });
        Reticulate.addr().then(function (result) {
            addr = result;
            View.setUserAddress(addr);
        });
        Reticulate.key().then(function (result) {
            key = result;
            View.setPublicKey(key);
        });

        updateFileId(fileId);
    }

    //update the current file
    async function updateFileId(id) {
        fileId = id;

        request = Reticulate.getAccess(fileId, addr);

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

    return {
        init,
        updateFileId,
        copyPeer,
        copyAddr,
        copyKey,
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

    return {
        setPeerId,
        setUserAddress,
        setPublicKey,
        updateAccess,
        updateFileId,
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
    }

    function handleFileId(e) {
        Model.updateFileId($("#file-id-in").val());
    }

    return {
        init,
    }
})();
