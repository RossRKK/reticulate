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

                openDirectory(rootDir);
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

    //open an entry in the
    function openEntry(entry) {
        if (entry.type === DIR) {
            breadCrumbs.push(entry);

            openDirectory(entry.id);
        } else {
            //download the file
            window.open(Reticulate.Net.getDomain() + FILE_PATH + entry.id);
        }
    }

    //open a directory
    async function openDirectory(dirId) {
        if (dirId !== "") {
            let dirContent = await Reticulate.Net.getFile(dirId);

            currentDir = new Reticulate.Directory.dir(dirId, dirContent);

            View.displayDirectory(currentDir);
        }
    }

    function addFileEntry(file) {
        let reader = new FileReader();

        reader.onload = async function (event) {
            console.log(event.target.result);

            let fileId = await Reticulate.Net.addFile(new Int8Array(event.target.result));
            console.log("File ID: " + fileId);

            let entry = new Reticulate.Directory.entry(file.name, FILE, fileId);
            currentDir.addEntry(entry);

            View.displayDirectory(currentDir);
        };

        reader.readAsArrayBuffer(file);
    }

    return {
        init,
        updateFileId,
        copyPeer,
        copyAddr,
        copyKey,
        openEntry,
        addFileEntry,
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

    function displayDirectory(dir) {
        clearDirectroyEntries();

        //update the view with the contents of the current directory
        dir.entries.forEach(addDirectroyEntry);

        /*<div class="list-group-item list-group-item-action centered">
            <i class="fas fa-lg fa-plus"></i>
        </div>*/
        let listItem = $("<div>", {
            class: "list-group-item centered",
        });
        listItem.append(makeIcon("fas fa-lg fa-plus"));

        listItem.on("dragover", Controller.dragOverHandler);
        listItem.on("drop", Controller.dropHandler);

        $("#dir-entries").append(listItem);
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
            "data-id": entry.id,
            "data-type": entry.type,
            "data-name": entry.name,
            class: "list-group-item list-group-item-action",
        });

        listItem.on("click", function (e) {
            //open the clicked entry
            let entry = new Reticulate.Directory.entry(e.currentTarget.dataset.name, e.currentTarget.dataset.type, e.currentTarget.dataset.id)
            Model.openEntry(entry);
        });

        $("#dir-entries").append(listItem);

        let row = $("<div>", {
            class: "row",
        });

        listItem.append(row);

        row.append(makeIconCol("far " + (entry.type === DIR ? "fa-folder" : "fa-file")));

        let nameCol = $("<div>", {
            class: "col-8",
        });

        row.append(nameCol);

        let name = $("<div>");
        name.text(entry.name);

        nameCol.append(name);

        if (entry.type !== DIR) {
            row.append(makeIconCol("icon fa fa-edit"));
        }
        row.append(makeIconCol("icon fa fa-share-square"));
        row.append(makeIconCol("icon fa fa-cog"));
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
        displayDirectory,
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

    function dropHandler(e) {
        e = e.originalEvent;
        console.log(e);

        // Prevent default behavior (Prevent file from being opened)
        e.preventDefault();

        if (e.dataTransfer.items) {
            // Use DataTransferItemList interface to access the file(s)
            for (var i = 0; i < e.dataTransfer.items.length; i++) {
                // If dropped items aren't files, reject them
                if (e.dataTransfer.items[i].kind === 'file') {
                    var file = e.dataTransfer.items[i].getAsFile();

                    Model.addFileEntry(file);
                }
            }
        } else {
            // Use DataTransfer interface to access the file(s)
            for (var i = 0; i < e.dataTransfer.files.length; i++) {
                Model.addFile(e.dataTransfer.files[i]);
            }
        }

        // Pass event to removeDragData for cleanup
        removeDragData(e)
    }

    function removeDragData(e) {

        if (e.dataTransfer.items) {
            // Use DataTransferItemList interface to remove the drag data
            e.dataTransfer.items.clear();
        } else {
            // Use DataTransfer interface to remove the drag data
            e.dataTransfer.clearData();
        }
    }

    function dragOverHandler(e) {
        e.preventDefault();
    }

    return {
        init,
        dropHandler,
        dragOverHandler,
    }
})();
