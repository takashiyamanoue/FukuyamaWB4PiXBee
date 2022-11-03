
package org.yamaLab.XBeeConnector;

import org.yamaLab.pukiwikiCommunicator.language.InterpreterInterface;
import org.yamaLab.pukiwikiCommunicator.language.Util;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBeeMessage;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IDataReceiveListener;

//import com.pi4j.jni.I2C;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.JTabbedPane;


public class XBee implements InterpreterInterface {
    Thread me;
    int counter;
    int frequency;
    int pie;
    boolean debug=false;  /* */
    static InterpreterInterface controller;
    String readingLine="";
    static XBee_GUI gui;
    JTabbedPane tabbedPane;
    
	// TODO Replace with the port where your module is connected.
	//private static final String PORT = "COM1";
	private static final String PORT = "/dev/ttyUSB0";
	// TODO Replace with the baud rate of your module.
	private static final int BAUD_RATE = 9600;
	
	private static final Scanner s = new Scanner(System.in);
	
	private static DataReceiveListener listener = new DataReceiveListener();
	XBeeDevice myDevice=null;
	XBeeNetwork network = null;

   public XBee(InterpreterInterface ctlr){
		controller=ctlr;
		myDevice = new XBeeDevice(PORT, BAUD_RATE);
		
		try {
			myDevice.open();
			
			network = myDevice.getNetwork();
			network.startDiscoveryProcess();
			
			System.out.println("\nLocal XBee: " + myDevice.getNodeID());
			
			System.out.println("\nScanning the network, please wait...");
			while (network.isDiscoveryRunning()) {
				sleep(100);
			}
			
			System.out.println("Devices found:");
			for (RemoteXBeeDevice remote : network.getDevices()) {
				System.out.println(" - " + remote.getNodeID());
				String vline="";
				vline="return xbee-device="+remote.getNodeID();
       		    controller.parseCommand("println "+vline);
       	        controller.parseCommand("sendSensorNetworkValue "+vline);
				gui.addText(vline);
			}
			
			System.out.println("\nType your messages here:\n");
			
			myDevice.addDataListener(listener);
		}
		catch(Exception e){
		}
			
 //       start();     // comment out 20191002
        System.out.println(" ... complete the GPIO #02 circuit and see the listener feedback here in the console.");
        gui=new XBee_GUI(this);

}
	public String getOutputText() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public boolean isTracing() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}
	
	public void setGui(JTabbedPane g){
	    	this.tabbedPane=g;
	    	tabbedPane.add("XBee",gui.getPanel());
	}

	public String parseCommand(String x) {
		// TODO 自動生成されたメソッド・スタブ
		String[] rest=new String[1];
		gui.addText(x+"\n");
		if(Util.parseKeyWord(x,"get-a-0",rest)){
			               return ""+frequency;
		}
		else
		if(parseXBee(x)) return "OK";

		return "error";
	}
	public boolean parseXBee(String x){
		String[] rest=new String[1];
		String[] command=new String[1];
		String[] namex=new String[1];
		if(Util.parseKeyWord(x,"xbee ",rest)){ 
			String subc=Util.skipSpace(rest[0]);
			if(Util.parseKeyWord(subc, "send ", rest)){ //i2c use 0 or i2c use 1
				subc=Util.skipSpace(rest[0]);
				System.out.println("sending "+subc);
				if(Util.parseStrConst(subc, command, rest)){
					String sending=command[0];
					String where=Util.skipSpace(rest[0]);
					if(Util.parseKeyWord(where, "to ", rest)){
						subc=Util.skipSpace(rest[0]);
						if(Util.parseName(subc, namex, rest)){
							where=namex[0];
							if(where.equals("all")){
								if(myDevice==null){
									System.out.println("myDevice==null");
								}
								else{
									try{
								      myDevice.sendBroadcastData(sending.getBytes());
									}
									catch(Exception e){
										System.out.println("error:"+e+" while broadcasting "+sending+".");
									}
								}
							}
							else{
								RemoteXBeeDevice remote = network.getDevice(where);
								if (remote != null) {
									try{
									  myDevice.sendData(remote, sending.getBytes());
									}
									catch(Exception e){
										System.out.println("error:"+e+" while sending "+sending+" to "+where+".");										
									}
								} else {
									System.err.println("Could not find the module " + where + " in the network.");
								}								
							}
							return true;
						}
					}
					else
					if(Util.parseKeyWord(where, ".", rest)){
						if(myDevice==null){
							System.out.println("myDevice==null");
						}
						else{
							try{
						       myDevice.sendBroadcastData(sending.getBytes());
							}
							catch(Exception e){
								System.out.println("error:"+e+" while broadcasting "+sending+".");								
							}
						}
						return true;
					}
					return false;
				}
				return false;
			}
			else
			if(Util.parseKeyWord(subc, "scan", rest)){
				network.startDiscoveryProcess();
				
				System.out.println("\nLocal XBee: " + myDevice.getNodeID());
				
				System.out.println("\nScanning the network, please wait...");
				while (network.isDiscoveryRunning()) {
					sleep(100);
				}
				
				System.out.println("Devices found:");
				for (RemoteXBeeDevice remote : network.getDevices()) {
					System.out.println(" - " + remote.getNodeID());
					String vline="";
					vline="return xbee-device="+remote.getNodeID();
           		   controller.parseCommand("println "+vline);
           	       controller.parseCommand("sendSensorNetworkValue "+vline);
				}
                return true;
			}
			return false;
	    }
		return false;
	}
	
	public InterpreterInterface lookUp(String x) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public void putApplicationTable(String key, InterpreterInterface obj) {
		// TODO Auto-generated method stub
		
	}
	private static String[] parseData(String text) throws IndexOutOfBoundsException {
		String[] s = new String[2];
		s[0] = text.substring(0, text.indexOf(":"));
		s[1] = text.substring(text.indexOf(":") + 2);
		return s;
	}	
	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {}
	}	
	private static class DataReceiveListener implements IDataReceiveListener {
		public void dataReceived(XBeeMessage xbeeMessage) {
			String recvLine=new String(xbeeMessage.getData());
			System.out.println("------------------------------------------------------------------");
			System.out.println("> " + xbeeMessage.getDevice().getNodeID() +
					(xbeeMessage.isBroadcast() ? " (broadcast)" : "") +
					": " + recvLine);
			System.out.println("------------------------------------------------------------------");
			String vline="";
			vline="dev=xbee,from="+xbeeMessage.getDevice().getNodeID()+",return="+recvLine;
         	controller.parseCommand("println "+vline);
         	controller.parseCommand("sendSensorNetworkValue "+vline);
         	gui.addText(vline);
		}
	}
	

}
