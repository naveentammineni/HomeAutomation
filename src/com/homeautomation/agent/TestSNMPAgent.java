package com.homeautomation.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.snmp4j.PDU;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import com.homeautomation.Constants;
import com.homeautomation.SnmpMessage;
import com.homeautomation.manager.SNMPManager;

public class TestSNMPAgent implements Runnable {
	static final OID sysDescr = new OID("1.3.6.1.2.1.1.1.0");
	static final OID interfacesTable = new OID("1.3.6.1.2.1.132.2.1");
	SNMPAgent agent = null;
	SNMPManager client = null;
	String address = null;
	ServerSocket server = null;
	static String fileName = "agent.config";
	HashMap<String, String> mib = new HashMap<String, String>();

	public TestSNMPAgent(String address) {
		this.address = address;
		File file = new File(fileName);
		try {
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				String line = "";
				while ((line = br.readLine()) != null) {
					String[] str = line.split("=");
					String oid = str[0];
					String value = str[1];
					OID oidreal = new OID(oid);
					mib.put(oid, value);
				}
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			server = new ServerSocket(Constants.serverPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		String serveraddress = "udp:" + Constants.serverIp + "/"
				+ Constants.serverPort;
		TestSNMPAgent client = new TestSNMPAgent(serveraddress);
		// client.init();
		new Thread(client).start();
	}

	private void init() throws IOException {
		agent = new SNMPAgent("0.0.0.0/2001");
		/*
		 * agent.registerManagedObject(MOCreator.createReadOnly(sysDescr,
		 * "This Description is set By NaveenT"));
		 */

		// Build a table. This example is taken from TestAgent and sets up
		// two physical interfaces

		MOTableBuilder builder = new MOTableBuilder(interfacesTable)
				.addColumnType(SMIConstants.SYNTAX_OCTET_STRING,
						MOAccessImpl.ACCESS_READ_ONLY)
				.addColumnType(SMIConstants.SYNTAX_INTEGER,
						MOAccessImpl.ACCESS_READ_ONLY)
				.addColumnType(SMIConstants.SYNTAX_OCTET_STRING,
						MOAccessImpl.ACCESS_READ_ONLY);
		try {

			File file = new File(fileName);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				String line = "";
				while ((line = br.readLine()) != null) {
					String[] str = line.split("=");
					String oid = str[0];
					String value = str[1];
					OID oidreal = new OID(oid);

					if (oidreal.get(oidreal.size() - 1) == 2) {
						builder.addRowValue(new Integer32(Integer
								.parseInt(value)));
					} else {
						builder.addRowValue(new OctetString(value));
					}
				}
				br.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		agent.registerManagedObject(builder.build());
		agent.start();
		System.out.println("Agent is running now...");
	}

	@Override
	public void run() {
		while (true) {
			try {
				Socket socket = server.accept();
				ObjectInputStream ois = new ObjectInputStream(
						socket.getInputStream());
				SnmpMessage message = (SnmpMessage) ois.readObject();
				if (message != null) {
					PDU pdu = message.getPdu();
					PDU responsePDU = new PDU();
					if (pdu.getType() == PDU.GET) {
						Variable variable;
						VariableBinding variableBinding = pdu
								.getVariableBindings().get(0);
						OID oid = variableBinding.getOid();
						if (mib.containsKey(oid.toString())) {
							variable = new OctetString((String) mib.get(oid
									.toString()));
						} else {
							responsePDU.setErrorStatus(2);
							variable = new OctetString("");
						}
						responsePDU.add(new VariableBinding(oid, variable));
						message = new SnmpMessage(responsePDU);
						ObjectOutputStream oos = new ObjectOutputStream(
								socket.getOutputStream());
						oos.writeObject(message);
						oos.close();
					} else if (pdu.getType() == PDU.SET) {
						VariableBinding variableBinding = pdu
								.getVariableBindings().get(0);
						OID oid = variableBinding.getOid();
						Variable variable = variableBinding.getVariable();
						if (mib.containsKey(oid.toString())) {
							mib.put(oid.toString(), variable.toString());
							responsePDU.setErrorStatus(0);
						} else {
							responsePDU.setErrorStatus(2);
						}
						message = new SnmpMessage(responsePDU);
						ObjectOutputStream oos = new ObjectOutputStream(
								socket.getOutputStream());
						oos.writeObject(message);
						oos.close();
						updateFile();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateFile() {
		File originalFile = new File("agent.config");
		File tempFile = new File("tempfile.txt");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(originalFile));
			// Construct the new file that will later be renamed to the original
			// filename.
			
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			String line = null;
			// Read from the original file and write to the new
			// unless content matches data to be removed.
			for (String key : mib.keySet()) {
				line = key + "=" + mib.get(key);
				pw.println(line);
			}
			pw.flush();
			pw.close();
			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Delete the original file
		if (!originalFile.delete()) {
			System.out.println("Could not delete file");
			return;
		}

		// Rename the new file to the filename the original file had.
		if (!tempFile.renameTo(originalFile))
			System.out.println("Could not rename file");
	}
}