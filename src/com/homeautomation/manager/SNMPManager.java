package com.homeautomation.manager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import com.homeautomation.Constants;
import com.homeautomation.SnmpMessage;

public class SNMPManager {

	Snmp snmp = null;
	String address = "";
	Integer32 requestId = new Integer32(0);

	/**
	 * Constructor
	 * 
	 * @param address
	 */
	public SNMPManager(String address) {
		this.address = address;
		/*TrapReceiver snmp4jTrapReceiver = new TrapReceiver();
		try {
			String trapaddress = Constants.clientIp + "/" + Constants.trapPort;
			snmp4jTrapReceiver.listen(new UdpAddress(trapaddress));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	public static void main(String[] args) throws IOException {

		String clientAddress = "udp:" + Constants.clientIp + "/"
				+ Constants.otherPort;
		SNMPManager client = new SNMPManager(clientAddress);
		client.start();
		// To set the OID value to the Agent
		String interfacesTable = "1.3.6.1.2.1.132.2.1.3.3";
		OID oid = new OID(interfacesTable);
		client.set(oid, "Drying");
		// To Get the OID value from the agent
		
		interfacesTable = "1.3.6.1.2.1.132.2.1.3.3";
		oid = new OID(interfacesTable); 
		SnmpMessage reply = client.getMessage(oid);
		PDU pdu = reply.getPdu();
		Variable variable= pdu.getVariable(oid);
		System.out.println(variable.toString());
		 
	}

	/**
	 * Start the Snmp session. If you forget the listen() method you will not
	 * get any answers because the communication is asynchronous and the
	 * listen() method listens for answers.
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		TransportMapping transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);
		transport.listen();
	}

	/**
	 * Method which takes a single OID and returns the response from the agent
	 * as a String.
	 * 
	 * @param oid
	 * @return
	 * @throws IOException
	 */
	public String getAsString(OID oid) throws IOException {
		ResponseEvent event = get(new OID[] { oid });
		return event.getResponse().get(0).getVariable().toString();
	}

	public List getTableAsStrings(OID[] oids) {
		TableUtils tUtils = new TableUtils(snmp, new DefaultPDUFactory());

		@SuppressWarnings("unchecked")
		List<TableEvent> events = tUtils
				.getTable(getTarget(), oids, null, null);

		List list = new ArrayList();
		for (TableEvent event : events) {
			if (event.isError()) {
				throw new RuntimeException(event.getErrorMessage());
			}
			List<String> strList = new ArrayList<String>();
			list.add(strList);
			for (VariableBinding vb : event.getColumns()) {
				strList.add(vb.getVariable().toString());
			}
		}
		return list;
	}

	public SnmpMessage set(OID oid, String str) throws IOException {
		try {
			PDU pdu = new PDU();
			Variable var = new OctetString(str);
			VariableBinding varBinding = new VariableBinding(oid, var);
			pdu.add(varBinding);
			pdu.setType(PDU.SET);
			pdu.setRequestID(requestId);
			requestId.setValue(requestId.getValue() + 1);
			SnmpMessage message = new SnmpMessage(pdu);

			Socket s = new Socket(Constants.clientIp, Constants.clientPort);
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(message);

			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			SnmpMessage response = (SnmpMessage) ois.readObject();
			ois.close();
			return response;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public SnmpMessage set(OID oid, int value) throws IOException {
		try {
			PDU pdu = new PDU();
			Variable var = new Integer32(value);
			VariableBinding varBinding = new VariableBinding(oid, var);
			pdu.add(varBinding);
			pdu.setType(PDU.SET);
			pdu.setRequestID(requestId);
			requestId.setValue(requestId.getValue() + 1);
			SnmpMessage message = new SnmpMessage(pdu);

			Socket s = new Socket(Constants.clientIp, Constants.clientPort);
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(message);

			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			SnmpMessage response = (SnmpMessage) ois.readObject();
			ois.close();
			return response;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method is capable of handling multiple OIDs
	 * 
	 * @param oids
	 * @return
	 * @throws IOException
	 */
	public ResponseEvent get(OID oids[]) throws IOException {
		PDU pdu = new PDU();
		for (OID oid : oids) {
			pdu.add(new VariableBinding(oid));
		}
		pdu.setType(PDU.GET);
		ResponseEvent event = snmp.send(pdu, getTarget(), null);

		if (event != null) {
			return event;
		}
		throw new RuntimeException("GET timed out");
	}

	public SnmpMessage getMessage(OID oid) {
		try {
			PDU pdu = new PDU();
			pdu.add(new VariableBinding(oid));
			pdu.setType(PDU.GET);
			pdu.setRequestID(requestId);
			requestId.setValue(requestId.getValue() + 1);
			SnmpMessage message = new SnmpMessage(pdu);

			Socket s = new Socket(Constants.clientIp, Constants.clientPort);
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(message);
			Thread.sleep(1000);
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			SnmpMessage response = (SnmpMessage) ois.readObject();
			ois.close();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method returns a Target, which contains information about where the
	 * data should be fetched and how.
	 * 
	 * @return
	 */
	private Target getTarget() {
		Address targetAddress = GenericAddress.parse("udp:"+Constants.clientIp+"/"+Constants.clientPort);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setAddress(targetAddress);
		target.setRetries(2);
		target.setTimeout(1500);
		target.setVersion(SnmpConstants.version2c);

		return target;
	}

}