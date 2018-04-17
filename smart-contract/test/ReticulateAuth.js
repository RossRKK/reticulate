// Specifically request an abstraction for MetaCoin
const ReticulateAuth = artifacts.require("ReticulateAuth");

contract('ReticulateAuth', function(accounts) {
    let reticulateAuth;

    const fileId = "test-file";
    const fileLength = 1;
    const dummyKey = [0, 1, 2, 3, 4];
    const dummyCheckSum = [0, 1, 2, 3, 4];

    const admin = 3;
    const read = 1;
    const none = 0;

    beforeEach('setup contract for each test', async function () {
        reticulateAuth = await ReticulateAuth.new();
        await reticulateAuth.addFile(fileId, dummyKey, fileLength, {from: accounts[0]});
    });

    it('should return the file name', async function () {
        assert.equal(await reticulateAuth.getFileLength(fileId), fileLength);
    });

    it('should give the owner admin permission', async function () {
        assert.equal(await reticulateAuth.getAccessLevel(fileId, accounts[0]), admin);
    });

    it('should return the owners key', async function () {
        assert.equal(await reticulateAuth.getKeyForFile(fileId, {from: accounts[0]}), web3.toHex(dummyKey));
    });

    it('a shared file should give the recipient the specified access level', async function () {
        await reticulateAuth.shareFile(fileId, accounts[1], dummyKey, read, {from: accounts[0]});

        assert.equal(await reticulateAuth.getAccessLevel(fileId, accounts[1]), read);
    });

    it('modifying a users access level should change the reported access level', async function () {
        await reticulateAuth.shareFile(fileId, accounts[1], dummyKey, read, {from: accounts[0]});
        await reticulateAuth.modifyAccessLevel(fileId, accounts[1], admin, {from: accounts[0]});

        assert.equal(await reticulateAuth.getAccessLevel(fileId, accounts[1]), admin);
    });

    it('a user thats not admin shouldn\'t be able to share a file', async function () {
        await reticulateAuth.shareFile(fileId, accounts[1], dummyKey, read, {from: accounts[0]});
        await reticulateAuth.shareFile(fileId, accounts[2], dummyKey, read, {from: accounts[1]});

        assert.equal(await reticulateAuth.getAccessLevel(fileId, accounts[2]), none);
    });

    it('a user thats not admin shouldn\'t be able to modify access levels', async function () {
        await reticulateAuth.shareFile(fileId, accounts[1], dummyKey, read, {from: accounts[0]});
        await reticulateAuth.modifyAccessLevel(fileId, accounts[1], admin, {from: accounts[1]});

        assert.equal(await reticulateAuth.getAccessLevel(fileId, accounts[1]), read);
    });

    it('a user shouldn\'t be able to crant access above their own', async function () {
        await reticulateAuth.shareFile(fileId, accounts[1], dummyKey, admin + 1, {from: accounts[0]});

        assert.equal(await reticulateAuth.getAccessLevel(fileId, accounts[1]), none);
    });

    it('an admin should be able to remove a file', async function () {
        await reticulateAuth.removeFile(fileId, {from: accounts[0]});

        assert.equal(await reticulateAuth.getFileLength(fileId), 0);
    });

    it('a non-admin shouldn\'t be able to remove a file', async function () {
        await reticulateAuth.shareFile(fileId, accounts[1], dummyKey, read, {from: accounts[0]});
        await reticulateAuth.removeFile(fileId, {from: accounts[1]});

        assert.equal(await reticulateAuth.getFileLength(fileId), fileLength);
    });

    it('files should be able to have check sums for blocks', async function () {
        await reticulateAuth.updateCheckSum(fileId, 0, dummyCheckSum, {from: accounts[0]});

        assert.equal(await reticulateAuth.compareCheckSum(fileId, 0, dummyCheckSum), true);
    });
});
