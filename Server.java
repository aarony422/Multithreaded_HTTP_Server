// Names: Aakash Patel, Meghan McCreary, Aaron (Shang Wei) Young, Katherine Chan
// HC: We have adhered to the Honor Code for this assignment. A.P., M.M., A.Y., K.C.
// Assignment 2: Multi-Threaded HTTP Server
// CSCI 342 Fall 2015
// Professor R. Salter

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
		} catch (IllegalArgumentException e) {
			System.err.println("Error binding to port");
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
					continue;
				}
			}
		} finally {
			server_sock.close();
		}
	}

	public void handle_client(Socket sock) throws IOException {
		int statusCode = 500;
		String contentType = "";
		byte[] dat = new byte[2000];
		int len = 0;
		InputStream in = null;
		OutputStream out = null;
		StringBuffer content = new StringBuffer();
		try {
			in = sock.getInputStream();
			out = sock.getOutputStream();

		} catch (IOException e) {
			System.err.println("Error: receive fails");
			sock.close();
			return;
		}
		try {
			while ((len = in.read(dat)) != -1) {
				String s0 = new String(dat, 0, len);
				content.append(s0);
				//System.out.println("DATA: " + content);;
				if (content.toString().contains("\r\n\r\n")){break;}
			}
			if (len == -1) {
				System.err.println("Error: receive fails");
				sock.close();
				return; // TODO: need to close thread, not main process
			}

			// respond to the client
			//System.out.println(content);

			String[] temp = content.toString().split(" ");
			FileInputStream requestedFile = null;
			try {
				String tempEnding = temp[1];
				if(temp[1].endsWith("/")) {
					tempEnding+="index.html";
				}
				File myfile = new File(ROOT,tempEnding);
				//System.out.println("SENDING FILE NAME: " + myfile.getAbsoluteFile());
				requestedFile = new FileInputStream(myfile);

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
				else if(tempEnding.endsWith(".ico")) {
					contentType = "image/x-icon";
				}
				else {
					contentType = "";
				}
				//System.out.println("ContentType: " + contentType);
				//System.out.println(tempEnding);
				statusCode = 200;
			}
			catch(NullPointerException e) {
				//System.err.println("404 error");
				statusCode = 404;
			}
			catch(FileNotFoundException e) {
				//System.err.println("404 error");
				statusCode = 404;
			}



			// call method to respond to client
			respond(out, statusCode, contentType, requestedFile);

			//System.out.println(statusCode);
			//System.out.println(contentType);
			// TODO: call function to actually send reply


		} finally {
			sock.close();
		}
	}

	public void respond (OutputStream out, int statusCode, String contentType, FileInputStream requestedFile) {


		// construct Header
		String header = "HTTP/1.0 " + statusCode + " ";
		switch (statusCode) {
		case 200:
			header = header.concat("OK");
			break;
		case 404:
			header = header.concat("Not Found");
			break;
		default:
			header = header.concat("Internal Server Error");
			break;
		}

		// add 1 carriage return
		header = header.concat("\r\n");

		// content type
		if (statusCode == 200) {
			header = header.concat("Content-Type: " + contentType);
		}
		//System.out.println("Content type: " + contentType);


		// add 2 carriage returns
		header = header.concat("\r\n\r\n");

	//	System.out.println("THIS IS THE RESPONSE HEADER: \n" + header);

		try {
			out.write(header.getBytes());
			int b;
			while ((b = requestedFile.read()) != -1) {
				out.write(b);
			}
			//System.out.println("END OF DATA");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error: Sending Fails");
			return;
		} catch(NullPointerException e){
			return;
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