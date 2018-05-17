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

    //the assigned username for the current user
    let username;

    //the registered root directory for the current user.
    let rootDir;


    //the directory object for the current dir
    let currentDir;

    function init() {
        Reticulate.Peer.peerId().then(function (result) {
            peerId = result;
            View.setPeerId(peerId);
        });
        Reticulate.Peer.addr().then(function (result) {
            addr = result;
            View.setUserAddress(addr);

            //get the users username
            Reticulate.Users.getUserNameForAddress(addr).then(function (result) {
                username = result;
                View.setUsername(result);
            });
            //get the users root dir
            Reticulate.Users.getRootDirForUser(addr, true).then(async function (result) {
                rootDir = result;

                View.setRootDir(rootDir);

                let dirContent = await Reticulate.Net.getFile(rootDir);

                currentDir = new Reticulate.Directory.dir(rootDir, dirContent);

                View.clearDirectroyEntries();

                //update the view with the contents of the current directory
                currentDir.entries.forEach(View.addDirectroyEntry);
            });
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

        request = Reticulate.Net.getAccess(fileId, addr);

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

    function setUsername(username) {
        $("#username").text(username);
    }

    function setRootDir(rootDir) {
        $("#rootDir").text(rootDir);
    }

    function addDirectroyEntry(entry) {
        /*<div class="list-group-item list-group-item-action">
            <div class="row">
                <div class="col-1"><i class="far fa-folder"></i></div>
                <div class="col-8">Example File</div>
                <div class="col-1"><i class="icon fa fa-edit"></i></div>
                <div class="col-1"><i class="icon fa fa-share-square"></i></div>
                <div class="col-1"><i class="icon fa fa-cog"></i></div>
            </div>
        </div>*/

        let listItem = $("<div>", {
            id: "dir-entry-" + entry.id,
            class: "list-group-item list-group-item-action",
        });
        listItem.on("click", function () {
            //TODO
        });

        $("#dir-entries").append(listItem);

        let row = $("<div>", {
            class: "row",
        });

        listItem.append(row);

        //function for making icons
        function makeIcon(faIcon) {
            let iconCol = $("<div>", {
                class: "col-1",
            });

            let icon = $("<i>", {
                class: faIcon,
            });

            iconCol.append(icon);

            return iconCol;
        }

        row.append(makeIcon("far " + (entry.type === "dir" ? "fa-folder" : "fa-file")));

        let nameCol = $("<div>", {
            class: "col-8",
        });

        row.append(nameCol);

        let name = $("<div>");
        name.text(entry.name);

        nameCol.append(name);

        row.append(makeIcon("icon fa fa-edit"));
        row.append(makeIcon("icon fa fa-share-square"));
        row.append(makeIcon("icon fa fa-cog"));
    }

    function clearDirectroyEntries() {
        $("#dir-entries").empty();
    }

    return {
        setPeerId,
        setUserAddress,
        setPublicKey,
        updateAccess,
        updateFileId,
        setRootDir,
        setUsername,
        addDirectroyEntry,
        clearDirectroyEntries,
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
