package poafs.spark;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.util.Base64;

import static spark.Spark.delete;
import static spark.Spark.path;

import javax.servlet.http.HttpServletResponse;

import poafs.Network;
import poafs.PoafsFileStream;
import poafs.lib.Reference;
import spark.Route;

public class SparkServer {
	
	private Network net;

	/**
	 * Create a spark server.
	 * @param net The reticulate network object.
	 */
	public SparkServer(Network net) {
		this.net = net;
		
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
		
	}
	
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

}
