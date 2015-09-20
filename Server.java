import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    
    public String ROOT;
	
	
	public Server(int port, String root) throws IOException {
	   
	    ROOT = root;
		
		ServerSocket server_sock = null;
		
		try {
			server_sock = new ServerSocket(port);
			server_sock.setReuseAddress(true);
		} catch (IOException e) {
			System.err.println("Creating socket failed");
			System.exit(1);
		}
		try {
			while (true) {
				try {
					Socket sock = server_sock.accept();
					// create thread, run()
					// handle_client(sock);
					requestHandler rH = new requestHandler(sock);
					Thread t = new Thread(rH);
					t.start();
				} catch (IOException e) {
					System.err.println("Error accepting connection");
					System.exit(1);
				}
			}
		} finally {
			server_sock.close();
		}
	}
	
	public void handle_client(Socket sock) throws IOException {
	    	int statusCode = -1;
	    	String contentType = "";
		byte[] dat = new byte[2000];
		int len = 0;
		InputStream in = null;
		StringBuffer content = new StringBuffer();
		try {
			in = sock.getInputStream();
		} catch (IOException e) {
			System.err.println("Receive failed");
			System.exit(1);
		}
		try {
			while ((len = in.read(dat)) != -1) {
				String s0 = new String(dat, 0, len);
				content.append(s0);
				if (s0.contains("\r\n\r\n")) break; // done reading GET request
			}
			if (len == -1) {
				System.err.println("receiving request failed");
				System.exit(1); // TODO: need to close thread, not main process
			}
			
			// respond to the client
			System.out.println(content);
			
			String[] temp = content.toString().split(" ");
			Scanner input = null;
			try {
			    String tempEnding = temp[1];
			    if(temp[1].endsWith("/")) {
				tempEnding+="index.html";
			    }
			    File myfile = new File(ROOT,tempEnding);
			    input = new Scanner(myfile);
			    
			    if(tempEnding.endsWith(".html")) {
				contentType = "text/html";
			    }
			    else if(tempEnding.endsWith(".txt")){
				contentType = "text/plain";
			    }
			    else if(tempEnding.endsWith(".pdf")){
				contentType = "application/pdf";
			    }
			    else if(tempEnding.endsWith(".png")) {
				contentType = "image/png";
			    }
			    else if(tempEnding.endsWith(".jpg") || tempEnding.endsWith(".jpeg")) {
				contentType = "image/jpeg";
			    }
			    else if(tempEnding.endsWith(".gif") ) {
				contentType = "image/gif";
			    }
			    else {
				contentType = "";
			    }
			    
			    statusCode = 200;
			}
			catch(NullPointerException e) {
				System.err.println("404 error");
				statusCode = 404;
			}
			catch(FileNotFoundException e) {
				System.err.println("404 error");
				statusCode = 404;
			}
			
			
			System.out.println(statusCode);
			System.out.println(contentType);
			// TODO: call function to actually send reply
			
			
		} finally {
			sock.close();
		}
	}
	
	public static void main(String[] arg) throws IOException { 
	    	if(arg.length!=2) {
	    	    System.err.println("Usage: hw2 <port> <folder>");
	    	    System.exit(-1);
	    	}
	    	int port = 0;
	    	try {
	    	    port = Integer.parseInt(arg[0]);
	    	    if (port<=0 || port>65535) {
		    	    System.err.println("Usage: hw2 <port> <folder>. Invalid port number.");
		    	    System.exit(-1);
	    	    }
	    	}
	    	catch(NumberFormatException e) {
	    	    System.err.println("Usage: hw2 <port> <folder>. Invalid port number.");
	    	    System.exit(-1);
	    	}
	    	
		Server myserver = new Server(port, arg[1]);
	}
	
	public class requestHandler implements Runnable {
	    Socket socket;
	    
	    public requestHandler(Socket socket) {
		this.socket = socket;
	    }
	    
	    // override run()
	    @Override
	    public void run() {
		// respond to the client
		try {
		    handle_client(socket);
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		
	    }
	}
}