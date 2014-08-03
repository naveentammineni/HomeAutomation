package com.homeautomation;

import java.io.Serializable;

import org.snmp4j.PDU;

public class SnmpMessage implements Serializable {
	private PDU pdu;
	private String agentType;

	public SnmpMessage(PDU pdu) {
		this.pdu = pdu;
	}
	public SnmpMessage(PDU pdu, String type){
		this.pdu = pdu;
		this.agentType = type;
	}
	
	public PDU getPdu() {
		return pdu;
	}

	public void setPdu(PDU pdu) {
		this.pdu = pdu;
	}

	public String getAgentType() {
		return agentType;
	}

	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}
}
