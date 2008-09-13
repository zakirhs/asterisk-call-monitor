package org.jmik.asterisk.model.impl;

import java.util.Date;

/**
 * This channel is more or less equivalent to Connection in JTAPI model.
 * @author Michele La Porta
 *
 */
public class Channel {

	private Descriptor descriptor;
	
	public Channel(Descriptor descriptor){
		this.descriptor = descriptor;
	}

	public Descriptor getDescriptor() {
		return this.descriptor;
	}

	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}

	public static class Descriptor {

		private java.util.Date creationTime;
		private CallEndpoint endpoint;
		private String channel;
		private String extension;
		
		public Descriptor(String channel,Date date,CallEndpoint endpoint) {
			this.channel = channel;
			this.endpoint = endpoint;
			this.creationTime = date;
		}

		public Descriptor(String extension) {
			this.extension = extension;
		}

		public String getId() {
			return channel;
		}

		public void setId(String id) {
			this.channel = id;
		}
		
		public java.util.Date getCreationTime() {
			return creationTime;
		}

		public CallEndpoint getEndpoint() {
			return endpoint;
		}

		public String getChannel() {
			return channel;
		}

		public String getExtension() {
			return extension;
		}
	}
	
	@Override
	public boolean equals(Object other) {
        if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof Channel) ) return false;
		 Channel castOther = ( Channel ) other; 
        
		 return (this.getDescriptor().getChannel() == castOther.getDescriptor().getChannel());
		 	/*&& (this.getDescriptor().getEndpoint().getCallId() == castOther.getDescriptor().getEndpoint().getCallId()
		 			);*/
	}
  
	@Override
	public int hashCode() {
        int result = 17;
        result = 37 * result + (int)this.getDescriptor().getCreationTime().getTime();
        return result;
  }   	
	@Override
	public String toString() {
		return super.toString()+"[channel="+this.getDescriptor().getChannel()+",endpoint="+this.getDescriptor().getEndpoint()+"]";
	}
	
}
