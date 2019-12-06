package logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;

public class HelloMessage implements Serializable{
	//This message needs to be serializable to allow its representation as a sequence of bytes
	private Node node;

	public HelloMessage(Node node) {
		this.node = node;
	}
	
	public byte[] serializeHelloMessage () throws IOException {
		ByteArrayOutputStream message = new ByteArrayOutputStream();
        ObjectOutputStream object = new ObjectOutputStream(message);
        object.writeObject((Object)this);
        object.flush();
        object.close();
        message.close();
        return message.toByteArray();
	}
	
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
}
