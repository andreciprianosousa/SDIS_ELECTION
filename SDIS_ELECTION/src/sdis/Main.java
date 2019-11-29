package sdis;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;

public class Main {
	
	public static void main(String[] args) throws IOException {
		if (args[0].equals("1")) { 

			// Which port should we listen to
			int port = 5000;
			// Which address
			String group = "225.4.5.6";

			// Create the socket and bind it to port 'port'.
			MulticastSocket s = new MulticastSocket(port);

			// join the multicast group
			s.joinGroup(InetAddress.getByName(group));
			// Now the socket is set up and we are ready to receive packets

			// Create a DatagramPacket and do a receive
			byte buf[] = new byte[100];
			DatagramPacket pack = new DatagramPacket(buf, buf.length);
			s.receive(pack);

			// Finally, let us do something useful with the data we just received,
			// like print it on stdout :-)
			System.out.println("Received data from: " + pack.getAddress().toString() + ":" + pack.getPort()
					+ " with length: " + pack.getLength());
			System.out.println(pack.toString());
			System.out.println(pack.getLength());
			
			String message = new String(pack.getData(), 0, pack.getLength());
			
			System.out.println(message);
		

			// And when we have finished receiving data leave the multicast group and
			// close the socket
			s.leaveGroup(InetAddress.getByName(group));
			s.close();

		} else {

			// Which port should we send to
			int port = 5000;
			// Which address
			String group = "225.4.5.6";

			// Create the socket but we don't bind it as we are only going to send data
			MulticastSocket s = new MulticastSocket();

			// Note that we don't have to join the multicast group if we are only
			// sending data and not receiving

			// Fill the buffer with some data
			String message = "Ola";
			byte[] buf = message.getBytes();
			
			// Create a DatagramPacket
			DatagramPacket pack = new DatagramPacket(buf, buf.length, InetAddress.getByName(group), port);
			// Do a send. Note that send takes a byte for the ttl and not an int.
			s.send(pack);

			// And when we have finished sending data close the socket
			s.close();

		}
		
	}
	
	static String byteArrayToString(byte[] in) {
	    char out[] = new char[in.length * 2];
	    for (int i = 0; i < in.length; i++) {
	        out[i * 2] = "0123456789ABCDEF".charAt((in[i] >> 4) & 15);
	        out[i * 2 + 1] = "0123456789ABCDEF".charAt(in[i] & 15);
	    }
	    return new String(out);
	}
}
