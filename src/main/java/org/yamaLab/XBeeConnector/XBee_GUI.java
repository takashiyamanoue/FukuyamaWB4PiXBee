package org.yamaLab.XBeeConnector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.yamaLab.pukiwikiCommunicator.Pi4j;
import org.yamaLab.pukiwikiCommunicator.FukuyamaWB4Pi.WikiBotV1Gui;

public class XBee_GUI {
	
	JTextArea xbeeTextArea ;
	private Properties setting;
	private XBee controller;
	
    private JPanel mainPanel;
	private JScrollPane xbeeAreaPane;
	private JLabel xbeeLabel;
	private JTextArea xbeeOutputArea;
	private JTextField xbeeCommandField;
    
	XBee_GUI(XBee p){
		initXBeeGui();		
		controller=p;		
	}
	public JPanel getPanel(){
		return mainPanel;
	}
	
	public void initXBeeGui(){
		mainPanel= new JPanel();
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPanel.setLayout(null);
		
		int x1=1;
		int x2=95;
		/* */
		{
			int h=5;
			xbeeCommandField = new JTextField();
			mainPanel.add(xbeeCommandField);
			xbeeCommandField.setBounds(x2, h, 450, 33);	
			JLabel inLabel = new JLabel();
			mainPanel.add(inLabel);
			inLabel.setText("input:");
			inLabel.setBounds(x1, h, 100, 33);
			{
				JButton sendButton = new JButton("parse");
				sendButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.out.println("XBee parse");
						String sending=xbeeCommandField.getText();
						//					saveProperties();
						//					controller.send(sending);
						if(controller.parseXBee(sending)){
							
						}
						xbeeCommandField.setText("");
					}
				});
				mainPanel.add(sendButton);
				sendButton.setBounds(x2+452,h, 100, 33);	
				JButton clearButton = new JButton("clear");
				clearButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						System.out.println("xbee clear");
						xbeeOutputArea.setText("");
					}
				});
				mainPanel.add(clearButton);
				clearButton.setBounds(x2+555,h,100,33);
			}
		}
/*	*/
    
		{
			int h=50;
			/* */
			xbeeAreaPane = new JScrollPane();
			mainPanel.add(xbeeAreaPane);
			xbeeAreaPane.setBounds(x2, h, 550, 200);
			{
				xbeeOutputArea = new JTextArea();
				xbeeAreaPane.setViewportView(xbeeOutputArea);
			}
			/* */
		}		
		{
			int h=300;
			/* */
			JLabel exampleLabel1=new JLabel("ex: xbee send \"get a0.\" to <xbee-id>");
			mainPanel.add(exampleLabel1);
			exampleLabel1.setBounds(x2,h,500,20);

			/* */
		}		


		/* */		
		mainPanel.setVisible(false);
//		this.setSize(804, 700);		
	}

	private Vector<String> putResultQueue=new Vector();	
	
	public void addText(String x){
		putResultQueue.add(x);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String x=putResultQueue.remove(0);
				String w=xbeeOutputArea.getText();
				if(w.length()>10000)
					w=w.substring(5000);
				w=w+"\n"+x;
				xbeeOutputArea.setText(w);
				JScrollBar sb=xbeeAreaPane.getVerticalScrollBar();
				sb.setValue(sb.getMaximum());           	
			}
		});		
	}
	public void setCommand(String x){
		this.xbeeCommandField.setText(x);
	}


}
