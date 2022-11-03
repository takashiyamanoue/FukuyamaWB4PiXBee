package org.yamaLab.pukiwikiCommunicator;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class Pi4J_GUI 
{
	
    private JPanel mainPanel;
	private JScrollPane pi4jAreaPane;
	private JLabel pConLabel;
	private JTextArea pi4jOutputArea;
	private JTextField pi4jCommandField;
	private Pi4j controller;
	private JCheckBox pythonCheckBox;
	
	Pi4J_GUI(Pi4j p){
		initPi4J_GUI();
		controller=p;
	}
	public JPanel getPanel(){
		return mainPanel;
	}
	
	public void initPi4J_GUI(){
		mainPanel= new JPanel();
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPanel.setLayout(null);
		
        int x1=1;
        int x2=95;
		/* */
		{
			int h=5;
			pi4jCommandField = new JTextField();
		    mainPanel.add(pi4jCommandField);
		    pi4jCommandField.setBounds(x2, h, 450, 33);	
		    JLabel inLabel = new JLabel();
			mainPanel.add(inLabel);
			inLabel.setText("input:");
			inLabel.setBounds(x1, h, 100, 33);
			{
				JButton sendButton = new JButton("parse");
				sendButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.out.println("Pi4j parse");
						String sending=pi4jCommandField.getText();
//						saveProperties();
//						controller.send(sending);
						controller.parseCommand(sending);
						pi4jCommandField.setText("");
					}
				});
				mainPanel.add(sendButton);
				sendButton.setBounds(x2+452,h, 100, 33);	
				JButton clearButton = new JButton("clear");
				clearButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						System.out.println("Pi4j clear");
						pi4jOutputArea.setText("");
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
			pi4jAreaPane = new JScrollPane();
			mainPanel.add(pi4jAreaPane);
			pi4jAreaPane.setBounds(x2, h, 550, 200);
			{
				pi4jOutputArea = new JTextArea();
				pi4jAreaPane.setViewportView(pi4jOutputArea);
			}
			/* */
		}		
		{
			int h=300;
			/* */
			JLabel exampleLabel1=new JLabel("ex: serial send \"get * dev temp.\"");
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
        		String w=pi4jOutputArea.getText();
        		if(w.length()>10000)
        			w=w.substring(5000);
        		w=w+"\n"+x;
        		pi4jOutputArea.setText(w);
        		JScrollBar sb=pi4jAreaPane.getVerticalScrollBar();
        		sb.setValue(sb.getMaximum());           	
            }
       });		
	}
	public void setCommand(String x){
		this.pi4jCommandField.setText(x);
	}


}