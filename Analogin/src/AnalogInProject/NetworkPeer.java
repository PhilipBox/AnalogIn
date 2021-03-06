package AnalogInProject;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * 
 * @author LEaps
 *
 *         Consumer Provider
 * 
 *         Receiver / Sender Receiver -> only out Sender -> only in
 */
public class NetworkPeer {
	private Socket socket = null;
	private ObjectOutputStream out = null;
	public HashMap<String, Object> networkJar = new HashMap<String, Object>();

	private Object senderLock = new Object();
	private ArrayList<Object> handlers = new ArrayList<Object>();
	private Receiver receiver;

	/**
	 * @return Ensure Connection about Network
	 */

	public NetworkPeer(Socket _socket) {
		socket = _socket;
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		receiver = new Receiver(this);
		receiver.start();
	}

	public Handler systemHandler(String jobType) {
		Handler h = new Handler(jobType, networkJar);
		synchronized (this.handlers) {
			this.handlers.add(h);
		}
		h.start();
		System.out.println("[SystemHandler] request : " + jobType);
		return h;
	}

	public boolean imageRequest() {
		for (String imgName : GIM.imageName) {
			File f = new File(GIM.dir + imgName);
			System.out.println("SAve DIR : " + f.getAbsolutePath());
			if (f.isFile())
				continue;
			byte[] response;
			if ((response = (byte[]) Sender(imgName, "FILE_REQUEST_BUFFEREDIMAGE", true)) == null) {
				System.out.println("[IMAGEREQUEST] RESPONSE NULL");
				return false;
			}
			try {

				FileOutputStream fos = new FileOutputStream(f);
				fos.write(response);
				System.out.println("imgName = " + f);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		SenderNormal("FILE_REQUEST_BUFFEREDIMAGE COMPLETE");
		return true;
	}

	public boolean SenderNormal(String commend) {
		synchronized (senderLock) {
			try {
				out.writeObject(commend);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public boolean mapInfoSend() {
		synchronized (senderLock) {
			try {
				System.out.println("[GAME_LOADING] INFO SENDING...");
				out.reset();
				System.out.println("[SENDING...] gamename : " + GIM.gmaeName);
				out.writeObject("PEER_GAME_NAME " + GIM.gmaeName);
				out.reset();
				out.writeObject("PEER_RULE " + GIM.rule);
				out.reset();
				out.writeObject(GIM.loadedBlockInfo);
				out.reset();
				GIM.imageName.add(0, "PEER_IMAGE_NAME");
				out.writeObject(GIM.imageName);
				GIM.imageName.remove(0);
				System.out.println("[GAME_LOADING] INFO SENDING OK!");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	// MyHandler YourHandler req
	public boolean mapSynchronization() {
		Handler handlerGameName = new Handler("PEER_GAME_NAME", networkJar);
		Handler handlerRule = new Handler("PEER_RULE", networkJar);
		Handler handlerBlockInfo = new Handler("PEER_BLOCK_INFO", networkJar);
		Handler handlerImageName = new Handler("PEER_IMAGE_NAME", networkJar);

		handlerGameName.setName("PEER_GAME_NAME");
		handlerRule.setName("PEER_RULE");
		handlerBlockInfo.setName("PEER_BLOCK_INFO");
		handlerImageName.setName("PEER_IMAGE_NAME");

		handlerGameName.start();
		handlerRule.start();
		handlerBlockInfo.start();
		handlerImageName.start();

		try {
			handlerGameName.join();
			handlerRule.join();
			handlerBlockInfo.join();
			handlerImageName.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synchronized (networkJar) {
			GIM.gmaeName = (String) networkJar.get("PEER_GAME_NAME");
			GIM.rule = (String) networkJar.get("PEER_RULE");
			GIM.loadedBlockInfo = (ArrayList<BlockInformation>) networkJar.get("PEER_BLOCK_INFO");
			GIM.imageName = (ArrayList<String>) networkJar.get("PEER_IMAGE_NAME");
		}
		
		GIM.dir = GIM.gmaeName + "\\\\";
		
		for(int i=0;i!=GIM.loadedBlockInfo.size();i++){
			GIM.loadedBlockInfo.get(i).imagePath = GIM.dir+GIM.imageName.get(i);
		}
		// 있는지 체크, 없다면?
		File dir = new File(GIM.dir);
		dir.mkdir();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GIM.dir + "map.ain"));
			oos.reset();
			oos.writeObject(GIM.gmaeName);
			oos.reset();
			oos.writeObject(GIM.rule);
			oos.reset();
			oos.writeObject(GIM.loadedBlockInfo);
			oos.reset();
			oos.writeObject(GIM.imageName);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Object Sender(String commend, String jobType, boolean isSyn) {
		Handler h = new Handler(jobType, networkJar);
		synchronized (handlers) {
			if (isSyn)
				handlers.add(h);
		}
		String handlerName;
		if (jobType.equals("p2p"))
			handlerName = h.getName();
		else {
			handlerName = jobType;
			h.setName(jobType);
		}
		h.start();
		String req = h.getName() + " " + commend;
		System.out.println("[p2pSender] request : " + req);
		synchronized (senderLock) {
			try {
				out.writeObject(req);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				synchronized (handlers) {
					h.interrupt();
					handlers.remove(h);
				}
				return null;
			}
		}
		if (isSyn) {
			try {
				h.join(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			h.interrupt();
			Object res = null;
			synchronized (networkJar) {
				res = networkJar.get(handlerName);
				networkJar.remove(handlerName);
			}
			synchronized (handlers) {
				h.interrupt();
				handlers.remove(h);
			}
			return res;
		}
		return null;
	}

	/**
	 * @author LEaps Response Pattern -> Thread-Name Content
	 */
	private static class Receiver extends Thread {
		ObjectInputStream in;

		NetworkPeer networkPeer;
		HashMap<String, Object> networkJar;

		public Receiver(NetworkPeer _NetworkPeer) {
			networkPeer = _NetworkPeer;
			networkJar = networkPeer.networkJar;
			System.out.println("[PeerResponse] On");
			try {
				in = new ObjectInputStream(networkPeer.socket.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void stringResponse(String reqFromServer) {
			System.out.println("[PeerReceiver] : " + reqFromServer);
			String[] res = reqFromServer.split(" ", 2);
			synchronized (networkJar) {
				networkJar.put(res[0], res[1]);
				networkJar.notifyAll();
			}
			System.out.println("[PeerReceiver] put [" + res[1] + "] in networkJar of " + res[0]);
		}

		public void hashResponse(Object reqFromServer) {
			System.out.println("[PeerReceiver] : HashData Receive");
			synchronized (networkJar) {
				networkJar.put("ROOM_INFO", reqFromServer);
				networkJar.notifyAll();
			}
			System.out.println("[PeerReceiver] put [RoomInfo] in networkJar of ROOM_INFO");
		}

		public void arrayListStringResponse(Object reqFromServer) {
			System.out.println("[PeerReceiver] : ArrayList String Receive");
			ArrayList<String> al = (ArrayList<String>) reqFromServer;
			String handlerName = al.get(0);
			al.remove(0);
			synchronized (networkJar) {
				networkJar.put(handlerName, reqFromServer);
				networkJar.notifyAll();
			}
			System.out.println("[PeerReceiver] put ArrayList String in networkJar of " + handlerName);
		}

		public void arrayListBlockInfoResponse(Object reqFromServer) {
			System.out.println("[PeerReceiver] : ArrayList BlockInfo Receive");
			synchronized (networkJar) {
				networkJar.put("PEER_BLOCK_INFO", reqFromServer);
				networkJar.notifyAll();
			}
			System.out.println("[PeerReceiver] put ArrayList BLOCKINFO in networkJar of PEER_BLOCK_INFO");
		}

		public void bufferedImageResponse(Object reqFromServer) {
			System.out.println("[PeerReceiver] : Image Receive");
			synchronized (networkJar) {
				networkJar.put("FILE_REQUEST_BUFFEREDIMAGE", reqFromServer);
				networkJar.notifyAll();
			}
			System.out.println("[PeerReceiver] put [SOME_FILE] in networkJar of FILE_REQUEST_BUFFEREDIMAGE");
		}

		public void byteResponse(Object reqFromServer) {
			System.out.println("[PeerReceiver] : Byte Receive");
			synchronized (networkJar) {
				networkJar.put("FILE_JAR", reqFromServer);
				networkJar.notifyAll();
			}
			System.out.println("[PeerReceiver] put [SOME_FILE] in networkJar of FILE_JAR");
		}

		@Override
		public void run() {
			do {
				try {
					Object reqFromServer = in.readObject();
					// String
					if (reqFromServer instanceof String) {
						stringResponse((String) reqFromServer);
					} else if (reqFromServer instanceof HashMap) {
						hashResponse(reqFromServer);
					} else if (reqFromServer instanceof byte[]) {
						bufferedImageResponse(reqFromServer);
					} else if (reqFromServer instanceof ArrayList<?>) {
						if (((ArrayList<?>) reqFromServer).get(0) instanceof BlockInformation) {
							arrayListBlockInfoResponse(reqFromServer);
						} else {
							arrayListStringResponse(reqFromServer);
						}
					} else {
						System.out.println("[PeerReceiver] Don't know data");
						break;
					}
				} catch (IOException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("[PeerReceiver] Socket IO Exception");
					try {
						networkPeer.socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				}
				System.out.println("[PeerReceiver] Complete Request");
			} while (true);
			System.out.println("[PeerReceiver] Receiver End");
		}
	}

	public void SenderObject(Object obj) {
		synchronized (senderLock) {
			try {
				out.writeObject(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
