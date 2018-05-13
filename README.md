# Reticulate
Reticulate is a secure peer to peer file sharing system. Peer's can upload files to a network of peers, each file is secured by an AES key that is generated when a file is added to the network. User's wrap this key using an RSA private key (by default derived from an ethereum wallet file) and add it to an Ethereum smart contract along with the files checksum. This smart contract is used to manage keys and permissions on files (who has read, write and admin access).

This allows users to backup a file (and share it with other users) securely without using a centralised or cloud based file sharing platform.
