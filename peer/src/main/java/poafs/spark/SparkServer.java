package poafs.spark;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.staticFiles;

import java.util.Base64;

import javax.servlet.http.HttpServletResponse;

import poafs.Application;
import poafs.Network;
import poafs.PoafsFileStream;
import poafs.auth.IUsers;
import poafs.lib.Reference;
import spark.Route;

public class SparkServer {
	
	private Network net;
	
	private IUsers users;

	/**
	 * Create a spark server.
	 * @param net The reticulate network object.
	 */
	public SparkServer(Network net, IUsers users) {
		this.net = net;
		
		staticFiles.location("/static");
		
		path("/peer", () -> {
			get("/id", peerId);
			
			get("/addr", addr);
			
			get("/key", pubKey);
		});
	
		
		post("/file", addFile);
		
		path("/file", () -> {
			get("/:fileId", getFile);
			
			put("/:fileId", writeFile);
			
			delete("/:fileId", removeFile);
			
			post("/:fileId/share", share);
			
			//functions for handling sharing
			path("/:fileId/share", () -> {
				get("/:userAddress", getAccess);
				
				delete("/:userAddress", revokeShare);
				
				put("/:userAddress", modifyAccess);
			});
		});
		
		post("/user", registerUser);
		
		path("/user", () -> {
			get("/:username/key", getUserKey);
			get("/:username/rootDir", getUserRootDir);
			get("/:username/addr", getUserAddress);
			get("/:address/username", getUsername);
			
			get("/:username", isUsernameTaken);
		});
		
	}
	
	private Route peerId = (req,res) -> {
		return Application.getPropertiesManager().getPeerId();
	};
	
	private Route addr = (req,res) -> {
		return net.getCreds().getAddress();
	};
	
	private Route pubKey = (req,res) -> {
		return Base64.getEncoder().encodeToString(net.getKeyStore().getPublicKey().getEncoded());
	};
	
	/**
	 * Handle requests for files.
	 */
	private Route getFile = (req, res) -> {
		PoafsFileStream stream = net.fetchFile(req.params(":fileId"));
		
		HttpServletResponse raw = res.raw();
		
		int index = 1;
		int currentByte = stream.read();
		while (currentByte != -1) {
			
			
			raw.getOutputStream().write(currentByte - 128);
			
			if (index % Reference.BLOCK_SIZE == 0) {
				
				raw.getOutputStream().flush();
			}
			currentByte = stream.read();
			index ++;
		}
		raw.getOutputStream().flush();
		raw.getOutputStream().close();
		
		return res.raw();
	};
	
	/**
	 * Handle the creation of a file.
	 */
	private Route addFile = (req, res) -> {
		//register the file with the network
		return net.registerFile(req.bodyAsBytes());
	};
	
	/**
	 * Handle the creation of a file.
	 */
	private Route writeFile = (req, res) -> {
		//register the file with the network
		net.updateFileContent(req.params(":fileId"), req.bodyAsBytes());
		return "Success";
	};
	
	
	/**
	 * Remove a file from the network.
	 */
	private Route removeFile = (req, res) -> {
		return net.removeFile(req.params(":fileId"));
	};
	
	/**
	 * Get the access level for a user on a specific file.
	 */
	private Route getAccess = (req, res) -> {
		return net.getAccessLevel(req.params(":fileId"), req.params(":userAddress"));
	};
	
	/**
	 * Revoke a user's share to a file.
	 */
	private Route revokeShare = (req, res) -> {
		return net.revokeShare(req.params(":fileId"), req.params(":userAddress"));
	};
	
	/**
	 * Modify a user's access level for a file.
	 */
	private Route modifyAccess = (req, res) -> {
		try {
			return net.modifyAccessLevel(req.params(":fileId"), req.params(":userAddress"), Integer.parseInt(req.queryParams("accessLevel")));
		} catch (NumberFormatException e) {
			res.status(400);
			return "Malformed access level";
		}
	};
	
	/**
	 * Share a file with another user.
	 */
	private Route share = (req, res) -> {
		try {
			byte[] key = Base64.getDecoder().decode(req.queryParams("userKey"));
			return net.share(req.params(":fileId"), req.queryParams("userAddress"), key, Integer.parseInt(req.queryParams("accessLevel")));
		} catch (NumberFormatException e) {
			res.status(400);
			return "Malformed access level";
		}
	};
	
	/**
	 ** User Routes
	 **/
	
	private Route registerUser = (req, res) -> {
		byte[] key = Base64.getDecoder().decode(req.queryParams("userKey"));
		return users.registerUser(req.params("username"), key, req.params("rootDir"));

	};
	
	private Route getUsername = (req, res) -> {
		return users.getUserNameForAddress(req.params(":address"));
	};
	
	private Route getUserAddress = (req, res) -> {
		return users.getAddressForUserName(req.params(":username"));
	};
	
	private Route isUsernameTaken = (req, res) -> {
		return users.isUserNameTaken(req.params(":username"));
	};
	
	
	private Route getUserKey = (req, res) -> {
		String nameOrAddr = req.params(":username");
		
		if (nameOrAddr.startsWith("0x")) {
			//address
			return users.getPublicKeyForUser(nameOrAddr);
		} else {
			//name
			return users.getPublicKeyForUserByName(nameOrAddr);
		}
	};
	
	private Route getUserRootDir = (req, res) -> {
		String nameOrAddr = req.params(":username");
		
		if (nameOrAddr.startsWith("0x")) {
			//address
			return users.getRootDirForUser(nameOrAddr);
		} else {
			//name
			return users.getRootDirForUserByName(nameOrAddr);
		}
	};
}
