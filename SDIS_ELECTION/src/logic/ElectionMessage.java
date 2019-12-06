package logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ElectionMessage extends Message implements Serializable{
	
	//This message needs to be serializable to allow its representation as a sequence of bytes
		private ComputationIndex cp;
		private int incomingId;

		public ElectionMessage(int incomingId, ComputationIndex cp) {
			this.cp = cp;
			this.incomingId = incomingId;
		}
		
		public byte[] serializeElectionMessage () throws IOException {
			ByteArrayOutputStream message = new ByteArrayOutputStream();
	        ObjectOutputStream object = new ObjectOutputStream(message);
	        object.writeObject((Object)this);
	        object.flush();
	        object.close();
	        message.close();
	        return message.toByteArray();
		}
		
		public int getNodeID() {
			return this.incomingId;
		}

		public void setincomingId(int incomingId) {
			this.incomingId = incomingId;
		}
		public ComputationIndex getComputationIndex() {
			return this.cp;
		}
}
