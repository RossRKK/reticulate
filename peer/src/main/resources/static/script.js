const MAX_LENGTH = 20;

$(async function () {
    let peerId = await Reticulate.peerId();
    $("#peer-id").text(peerId);

    let addr = await Reticulate.addr();
    $("#addr").text(addr);

    let key = await Reticulate.key();
    $("#key").val(key);
    $("#key").text(key.substring(0, MAX_LENGTH) + "...");
});
