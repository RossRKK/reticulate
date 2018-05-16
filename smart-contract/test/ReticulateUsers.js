// Specifically request an abstraction for MetaCoin
const ReticulateUsers = artifacts.require("ReticulateUsers");

contract('ReticulateUsers', function(accounts) {
    let reticulateUsers;

    const userName = "test-user";

    const pubKey = [0, 1, 2, 3, 4];
    const pubKey2 = [4, 3, 2, 1, 0];

    const rootDir = "rootDir";
    const rootDir2 = "anotherRootDir";

    beforeEach('setup contract for each test', async function () {
        reticulateUsers = await ReticulateUsers.new();

        //register a user for testing
        await reticulateUsers.registerUser(userName, pubKey, rootDir);
    });

    it('should return the users username', async function () {
        assert.equal(await reticulateUsers.getUserNameForAddress(accounts[0]), userName);
    });

    it('should return the users rootDir', async function () {
        assert.equal(await reticulateUsers.getRootDirForUser(accounts[0]), rootDir);
        assert.equal(await reticulateUsers.getRootDirForUserByName(userName), rootDir);
    });

    it('should return the users public key', async function () {
        assert.equal(await reticulateUsers.getPublicKeyForUserByName(userName), web3.toHex(pubKey));
        assert.equal(await reticulateUsers.getPublicKeyForUser(accounts[0]), web3.toHex(pubKey));
    });

    it('should show the user name as taken', async function () {
        assert.equal(await reticulateUsers.isUserNameTaken(userName), true);
    });

    it('shouldn\'t allow another user to take an existing username', async function () {
        let result = await reticulateUsers.registerUser(userName, pubKey2, rootDir2, { from: accounts[1] });

        assert.equal(await reticulateUsers.getAddressForUserName(userName), accounts[0]);
        assert.equal(await reticulateUsers.getUserNameForAddress(accounts[1]), "");

        //the key and root dir should still be correect
        assert.equal(await reticulateUsers.getRootDirForUser(accounts[1]), rootDir2);
        assert.equal(await reticulateUsers.getPublicKeyForUser(accounts[1]), web3.toHex(pubKey2));
    });
});
