package xyz.reticulate.spark;

import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.options;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.staticFiles;
import static spark.Spark.stop;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Base64;
import java.util.HashSet;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import spark.Route;
import xyz.reticulate.Application;
import xyz.reticulate.Network;
import xyz.reticulate.auth.IUsers;
import xyz.reticulate.lib.Reference;

public class SparkServer {
	
	private Network net;
	
	private IUsers users;
	
	private HashSet<String> trustedHosts = new HashSet<String>();
	
	private boolean isTrusted(String host) {
		if (host != null) {
			for (String pattern:trustedHosts) {
				if (host.matches(pattern)) {
					return true;
				}
			}
		} else {
			//this isn't a cross origin so it should be allowed
			return true;
		}
		
		return false;
	}

	/**
	 * Create a spark server.
	 * @param net The reticulate network object.
	 */
	public SparkServer(Network net, IUsers users) {
		this.net = net;
		this.users = users;
		
		trustedHosts.add(".+\\.reticulate\\.xyz.*"); //trust all reticulate domains
		trustedHosts.add("null"); //if the origin is a file
		
		
		staticFiles.location("/static");
		
		
		options("/*", (request, response) -> {
            String accessControlRequestHeaders = request
                    .headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers",
                        accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request
                    .headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods",
                        accessControlRequestMethod);
            }

            return "OK";
        });

		before((request, response) -> {
			String host = request.headers("origin");
			
			if (isTrusted(host)) {
				response.header("Access-Control-Allow-Origin", host);
			} else {
				System.out.println(host);
				halt(401);
			}
		});

		
		//before(new BasicAuthenticationFilter("", new AuthenticationDetails(Application.getPropertiesManager().getWebUsername(), Application.getPropertiesManager().getWebPassword())));

		
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
			
			//get stuff by user name
			path("/name/:username", () -> {
				get("/key", getUserKeyByName);
				get("/rootDir", getUserRootDirByName);
				get("/addr", getUserAddress);
			});
			
			//get stuff by user address
			path("/addr/:address", () -> {
				get("/key", getUserKeyByAddr);
				get("/rootDir", getUserRootDirByAddr);
				get("/username", getUsername);
			});
			
			get("/:username", isUsernameTaken);
		});
		
		exception(Exception.class, (ex, req, res) -> {
			System.err.println("Error processing " + req.contextPath());
		    ex.printStackTrace();
		    
		    res.status(500);
		    
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    
		    ex.printStackTrace(new PrintStream(out));
		    
		    
		    res.body(new String((out.toByteArray())));
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
		InputStream stream = net.fetchFile(req.params(":fileId"));
		
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
		String id = UUID.randomUUID().toString();
		//register the file with the network
		net.registerFile(id, Base64.getDecoder().decode(req.body()));
		return id;
	};
	
	/**
	 * Handle the creation of a file.
	 */
	private Route writeFile = (req, res) -> {
		//register the file with the network
		//net.updateFileContent(req.params(":fileId"), Base64.getDecoder().decode(req.body()));
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
		return users.registerUser(req.queryParams("username"), key, req.queryParams("rootDir"));

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
	
	
	private Route getUserKeyByName = (req, res) -> {
		byte[] key = users.getPublicKeyForUserByName(req.params(":username"));
		
		if (key != null) {
			return Base64.getEncoder().encodeToString(key);
		} else {
			return null;
		}
	};
	
	private Route getUserKeyByAddr = (req, res) -> {
		byte[] key =  users.getPublicKeyForUser(req.params(":address"));
		
		if (key != null) {
			return Base64.getEncoder().encodeToString(key);
		} else {
			return null;
		}
	};
	
	private Route getUserRootDirByName = (req, res) -> {
		return users.getRootDirForUserByName(req.params(":username"));
	};
	
	private Route getUserRootDirByAddr = (req, res) -> {
		return users.getRootDirForUser(req.params(":address"));
	};

	public void shutdown() {
		stop();
	}
}
