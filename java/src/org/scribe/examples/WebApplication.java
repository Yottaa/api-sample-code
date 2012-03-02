package org.scribe.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This program demonstrates a simple multi-threaded web server. A more advanced
 * version of this server can be implemented using NIO and/or thread pooling.
 */
public class WebApplication {

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: WebApplication <port> <document-dir> <log=true|false>");
		} else {
			int port = Integer.parseInt(args[0]);
			File documentDir = new File(args[1]);
			boolean log = Boolean.valueOf(args[2]).booleanValue();
			httpd(port, documentDir, log);
		}
	}

	private static void httpd(int port, File documentDir, boolean log) {
		if (!documentDir.exists()) {
			System.err.println("No such document-dir exists: "
					+ documentDir.getAbsolutePath());
		} else if (!documentDir.isDirectory()) {
			System.err.println("Document-dir " + documentDir.getAbsolutePath()
					+ " is not a directory");
		} else {
			try {
				ServerSocket serverSocket = new ServerSocket(port);
				try {
					while (true) {
						// wait for the next client to connect and get its socket connection
						Socket socket = serverSocket.accept();
						// handle the socket connection by a handler in a new thread
						new Thread(new Handler(socket, documentDir, log)).start();
					}
				} catch (IOException e) {
					System.err.println("Error while accepting connection on port "
							+ port);
				} finally {
					serverSocket.close();
				}
			} catch (IOException e) {
				System.err.println("Failed to bind to port " + port);
			}
		}
	}

	private static final class Handler implements Runnable {
		private static final Pattern REQUEST_PATTERN = Pattern.compile("^GET (/.*) HTTP/1.[01]$");
		private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
		"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		private final File documentDir;
		private final Socket socket;
		private final boolean log;
		public Handler(Socket socket, File documentDir, boolean log) {
			this.socket = socket;
			this.documentDir = documentDir;
			this.log = log;
		}
		
		private String readRequestPath() throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					this.socket.getInputStream()));
			String firstLine = reader.readLine();
			if (firstLine == null) {
				return null;
			}
			Matcher matcher = REQUEST_PATTERN.matcher(firstLine);
			return matcher.matches() ? matcher.group(1) : null;
		}
		
		private OutputStream sendResponseHeaders(int status, String message, long len) throws IOException {
			StringBuffer response = new StringBuffer();
			response.append("HTTP/1.0 ");
			response.append(status).append(' ').append(message).append("\r\n");
			response.append("Content-Length: ").append(len).append("\r\n\r\n");
			OutputStream out = this.socket.getOutputStream();
			out.write(response.toString().getBytes());
			out.flush();
			return out;
		}
		
		private int sendErrorResponse(int status, String message) throws IOException {
			OutputStream out = sendResponseHeaders(status, message,
					message.length());
			out.write(message.getBytes());
			out.flush();
			return status;
		}
		
		private long sendFile(File file) throws IOException {
			long len = file.length();
			OutputStream out = sendResponseHeaders(200, "OK", len);
			InputStream in = new FileInputStream(file);
			try {
				byte[] buffer = new byte[1024];
				int nread = 0;
				while ((nread = in.read(buffer)) > 0) {
					out.write(buffer, 0, nread);
				}
			} finally {
				in.close();
			}
			out.flush();
			return len;
		}
		
		private long sendString(String str) throws IOException {
			long len = str.length();
			OutputStream out = sendResponseHeaders(200, "OK", len);
			out.write(str.getBytes());
			out.flush();
			return len;
		}
		
		// this is the main entry point into this handler
		public void run() {
			// initialize logging information
			long time = System.currentTimeMillis();
			int status = 200;
			long len = 0;
			String host = this.socket.getInetAddress().getHostName();
			String path = null;
			// handle request
			try {
				path = readRequestPath();
				if (path == null) {
					status = sendErrorResponse(400, "Bad Request");
				}
				
				// len>=0 if path is special path, else len<0 and visit local path  
				else if((len=ProcessSpecialPaths(path))<0 ){
					
					File file = new File(this.documentDir, path);
					if (!file.getAbsolutePath().startsWith(
							this.documentDir.getAbsolutePath())
							|| (file.exists() && (!file.isFile() || !file.canRead()))) {
						// only allow readable files under document root
						status = sendErrorResponse(403, "Forbidden");
					} else if (!file.exists()) {
						status = sendErrorResponse(404, "Not Found");
					} else {
						len = sendFile(file);
					}
					
				}
			} catch (IOException e) {
				System.err.println("Error while serving request for [" + path
						+ "] from [" + host + "]: " + e.getMessage());
				e.printStackTrace();
			} finally {
				try {
					this.socket.close();
				} catch (IOException e) {
					System.err.println("Error while closing socket to " + host
							+ ": " + e.getMessage());
				}
			}
		
			if (this.log) {
				StringBuffer sb = new StringBuffer();
				sb.append(DATE_FORMAT.format(new Date(time))).append(' ');
				sb.append(host).append(' ');
				sb.append(path == null ? "" : path).append(' ');
				sb.append(status).append(' ');
				sb.append(len).append(' ');
				sb.append(System.currentTimeMillis() - time);
				System.out.println(sb);
			}
		}
		
		private String extractParameter(String path, String name)
		{
			String REGEX = String.format(".*%s=([^&]+).*", name);
			Pattern pat = Pattern.compile(REGEX);
			Matcher mat = pat.matcher(path);
			if(mat.matches()) return mat.group(1);
			else return null;
		}
		
		private long ProcessSpecialPaths(String path) throws IOException {

			long len = -1;
			String response = null;
			
			if(path.equals("/")) {
				response = "<h1>Hello, I am a client of Yottaa API Service</h1><h3><a href=\"%s\">Connect with Yottaa -></a></h3>";
				String authorizationUrl = ClientExample.getAuthorizationUrl();				
				response = String.format(response, authorizationUrl);
			} 

			else if (path.startsWith("/callback")) {
				String code = extractParameter(path, "code");
				if(code==null) response = "Failed to get authentication code!";
				else {
					response = "<h1>This is OAuth2 callback</h1>"
						+"<h3>Grant success</h3>"
						+"<h3>code: %s</h3>"
						+"<a href=\"%s\">Get tokens now</a>";
					response = String.format(response, code, "/get_tokens?code="+code);
				}
			} 
			
			else if (path.startsWith("/get_tokens")) {
				String code = extractParameter(path, "code");
				if(code==null) response = "Failed to get authentication code!";
				else {
					String[] tokens = ClientExample.getToken_from_code(code);
					response = "<h1>Get tokens success</h1>"
						+"<h3>access_token: %s</h3>"
						+"<h3>refresh_token: %s</h3>"
						+"<a href=\"%s\">Now try the API to visit a resource(your email)</a>";
					response = String.format(response, tokens[0], tokens[1], "/private_api_test?access_token=" + tokens[0] + "&refresh_token=" + tokens[1]);
				}
			}
			
			else if (path.startsWith("/private_api_test")) {
				String accessToken = extractParameter(path, "access_token");
				String refreshToken = extractParameter(path, "refresh_token");
				if(accessToken==null || refreshToken==null) response = "Failed to get tokens!";
				else {
					String ss = ClientExample.getResource(accessToken, ClientExample.PROTECTED_RESOURCE_URL1);
					response = "<h3>%s</h3><a href=\"%s\">If access token expires, use refresh token to get the new tokens</a>";
					response = String.format(response, ss, "/refresh_tokens?refresh_token=" + refreshToken);
				}
			}
				
			else if (path.startsWith("/refresh_tokens")) {
				String refreshToken = extractParameter(path, "refresh_token");
				if(refreshToken==null) response = "Failed to get refresh token!";
				else {
					String[] tokens = ClientExample.getToken_from_refresh(refreshToken);
					response = "<h1>Get token success</h1>"
						+"<h3>access_token: %s</h3>"
						+"<h3>refresh_token: %s</h3>"
						+"<a href=\"%s\">Now try the API to visit another resource(your sites)</a>";
					response = String.format(response, tokens[0], tokens[1], "/api_test?access_token=" + tokens[0]);
				}
			}

			else if (path.startsWith("/api_test")) {
				String accessToken = extractParameter(path, "access_token");
				if(accessToken==null) response = "Failed to get access token!";
				else {
					response = ClientExample.getResource(accessToken, ClientExample.PROTECTED_RESOURCE_URL2);
				}
			}
			
			if(response != null) return sendString(response);
			else return -1;
		}
		
	}
}
