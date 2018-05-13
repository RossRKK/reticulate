# Reticulate
Reticulate is a secure peer to peer file sharing system. Peer's can upload files to a network of peers, each file is secured by an AES key that is generated when a file is added to the network. User's wrap this key using an RSA private key (by default derived from an ethereum wallet file) and add it to an Ethereum smart contract along with the files checksum. This smart contract is used to manage keys and permissions on files (who has read, write and admin access).

This allows users to backup a file (and share it with other users) securely without using a centralised or cloud based file sharing platform.

Reticulate currently runs on the **Rinkeby** network.

To setup a release version Reticulate node:
1. Download and extract the .zip for the latest release.
2. Run either bin/peer on Unix systems or bin/peer.bat on Windows, this will generate the default config.properties file.

To setup the very latest version (for unix based systems):
1. Create a directory to hold the node.
2. Download the setup.sh, build.sh and start.sh and put them in the folder.
3. Run ./setup.sh, this will clone the git repo
4. Run ./build.sh this will update and build the repo.
5. Run ./start.sh this will start the node.
