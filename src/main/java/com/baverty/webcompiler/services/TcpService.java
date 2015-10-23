package com.baverty.webcompiler.services;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.springframework.stereotype.Service;

/**
 * Small service used to send data through tcp.
 */
@Service
class TcpService {

	/**
	 * Send data through a socket.
	 * 
	 * @param host
	 *            the host of the tcp server
	 * @param port
	 *            the port to connect to
	 */
	public void sendData(String host, Integer port, String data) {
		try {
			Socket s = new Socket(host, port);
			OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
			os.write(data);
			os.flush();
			s.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
