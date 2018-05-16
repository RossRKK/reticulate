var ReticulateAuth = artifacts.require("ReticulateAuth");
var ReticulateUsers = artifacts.require("ReticulateUsers");

module.exports = function(deployer) {
  deployer.deploy(ReticulateAuth);
  deployer.deploy(ReticulateUsers);
};
