# Reticulate
Reticulate is a peer to peer file sharing system based on my previous work on POAFS. Peer's can upload files to a network of peers, each file is secured by an AES key that is generated when a file is added to the network. User's wrap this key using an RSA private key (by default derived from an ethereum wallet file) and add it to an Ethereum smart contract (currently 0xaC2D7F31249d35442c8e317c156102a37f422BEA on the Rinkeby network). This smart contract is used to manage keys, permissions on files (currently once a file is added it can only be read, write opertions are planned for the future).

This allows users to backup a file (and share it with other users) securely without using a centralised or cloud based file sharing platform.
