package org.yamaLab.TwitterConnector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.yamaLab.pukiwikiCommunicator.Pi4j;
import org.yamaLab.pukiwikiCommunicator.FukuyamaWB4Pi.WikiBotV1Gui;

public class Twitter_GUI {
	
	JTextArea tweetTextArea ;
	TwitterController controller;
    private JPanel twitterPanel,twitterAuthPanel;	
	private Properties setting;
	private JCheckBox twitterCheckBox;
    

	Twitter_GUI(TwitterController p){
		controller=p;		
		initTwitterGui();
		twitterAuthSettingGui();
	}
	public JPanel getTwitterPanel(){
		return twitterPanel;
	}	
	public JPanel getTwitterAuthPanel(){
		return twitterAuthPanel;
	}	
	
	
	public void initTwitterGui(){
		try{
		twitterPanel= new JPanel();
		twitterPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		twitterPanel.setLayout(null);
		
		int h=5;
		twitterCheckBox = new JCheckBox();
		twitterPanel.add(twitterCheckBox);
		twitterCheckBox.setText("use");
		twitterCheckBox.setBounds(10, h, 80, 25);
		twitterCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
//				onlineCommandRefreshButtonActionPerformed(evt);
				controller.parseCommand("twitterCheckBox "+(twitterCheckBox.isSelected()));
			}
		});	
    			
		
	    twitterPanel.setLayout(null);
	    h=30;
		{
			JButton btnConnectTwitter = new JButton("Login Twitter");
			btnConnectTwitter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Login Twitter");
//					connectTwitter();
					controller.parseCommand("login", "f");
				}
			});
			btnConnectTwitter.setBounds(6, h, 165, 29);
			twitterPanel.add(btnConnectTwitter);
			
		}
		JScrollPane tweetScrollPane = new JScrollPane();
		tweetScrollPane.setBounds(180, h, 225, 42);
		twitterPanel.add(tweetScrollPane);
		
		JButton btnTweet = new JButton("Tweet");
		btnTweet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				System.out.println("Tweet");
				String x=tweetTextArea.getText();
//				int rtn=tweet(x);
//				if(rtn==1){
				controller.parseCommand("tweet", x);
					tweetTextArea.setText("");
//				}
			}
		});
		btnTweet.setBounds(432, h, 90, 42);
		twitterPanel.add(btnTweet);
		
		tweetTextArea = new JTextArea();
		tweetScrollPane.setViewportView(tweetTextArea);
		

		
		JButton savePropertiesButton = new JButton("SaveProperties");
		savePropertiesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Save Properties");
//				connectTwitter();
				reflectProperties();
				saveProperties();
			}
		});
		savePropertiesButton.setBounds(530, h, 165, 29);
		twitterPanel.add(savePropertiesButton);
		
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
//			this.setTitle("PukiwikiCommunicator");
		
	}	
	JLabel consumerKeyLabel;
	JLabel consumerSecretLabel;
	JLabel accessTokenLabel;
	JLabel accessTokenSecretLabel;
	JTextField consumerKeyTextField;
	JTextField consumerSecretTextField;
	JTextField accessTokenTextField;
	JTextField accessTokenSecretTextField;
	
	public void twitterAuthSettingGui(){
		try{
		twitterAuthPanel= new JPanel();
		twitterAuthPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		twitterAuthPanel.setLayout(null);
		
	    twitterAuthPanel.setLayout(null);
	    
		{
			consumerKeyLabel = new JLabel();
			twitterAuthPanel.add(consumerKeyLabel);
			consumerKeyLabel.setText("Comsumer Key:");
			consumerKeyLabel.setBounds(1, 30, 105, 24);
		}
		{
			consumerKeyTextField = new JTextField();
			twitterAuthPanel.add(consumerKeyTextField);
			consumerKeyTextField.setBounds(120, 30, 446, 30);
		}
		{
			consumerSecretLabel = new JLabel();
			twitterAuthPanel.add(consumerSecretLabel);
			consumerSecretLabel.setText("Comsumer Secret:");
			consumerSecretLabel.setBounds(1, 55, 120, 24);
		}
		{
			consumerSecretTextField = new JTextField();
			twitterAuthPanel.add(consumerSecretTextField);
			consumerSecretTextField.setBounds(120, 55, 446, 30);
		}
		{
			accessTokenLabel = new JLabel();
			twitterAuthPanel.add(accessTokenLabel);
			accessTokenLabel.setText("Access Token:");
			accessTokenLabel.setBounds(1, 80, 105, 24);
		}
		{
			accessTokenTextField = new JTextField();
			twitterAuthPanel.add(accessTokenTextField);
			accessTokenTextField.setBounds(120, 80, 446, 30);
		}
		{
			accessTokenSecretLabel = new JLabel();
			twitterAuthPanel.add(accessTokenSecretLabel);
			accessTokenSecretLabel.setText("AccessToken Secret:");
			accessTokenSecretLabel.setBounds(1, 105, 120, 24);
		}
		{
			accessTokenSecretTextField = new JTextField();
			twitterAuthPanel.add(accessTokenSecretTextField);
			accessTokenSecretTextField.setBounds(120, 105, 446, 30);
		}
		JButton savePropertiesButton = new JButton("SaveProperties");
		savePropertiesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Save Properties");
//				connectTwitter();
				reflectProperties();
				saveProperties();
			}
		});
		savePropertiesButton.setBounds(530, 0, 165, 29);
		twitterAuthPanel.add(savePropertiesButton);
		
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
//			this.setTitle("PukiwikiCommunicator");
		
	}
	public void setSetting(Properties s){
		this.setting=s;
		String w=null;
        w =this.setting.getProperty("oauth.consumerKey");
        if(w!=null){
        	this.consumerKeyTextField.setText(w);
        }
        w =this.setting.getProperty("oauth.consumerSecret");
        if(w!=null){
        	this.consumerSecretTextField.setText(w);
        }
        w =this.setting.getProperty("oauth.accessToken");
        if(w!=null){
        	this.accessTokenTextField.setText(w);
        }
        w =this.setting.getProperty("oauth.accessTokenSecret");
        if(w!=null){
        	this.accessTokenSecretTextField.setText(w);
        }
		w=this.setting.getProperty("twitterCheckBox");
		if(w!=null){
			if(w.equals("true")){
		       this.setTwitterUse(true);
			}
			else
			if(w.equals("false")){
				this.setTwitterUse(false);
			}
		}
		else{
	    	this.setTwitterUse(true);
	    	if(getTwitterUse()){
	    	   setting.put("twitterCheckBox", "true");
	    	}
	    	else{
	    		setting.put("twitterCheckBox", "false");
	    	}
		}        
	}
	public void reflectProperties(){
		if(this.setting==null)return;
		setting.put("oauth.consumerKey", this.consumerKeyTextField.getText());
		setting.put("oauth.consumerSecret", this.consumerSecretTextField.getText());
		setting.put("oauth.accessToken", this.accessTokenTextField.getText());
		setting.put("oauth.accessTokenSecret", this.accessTokenSecretTextField.getText());	
    	if(getTwitterUse()){
	    	   setting.put("twitterCheckBox", "true");
	    	}
	    	else{
	    		setting.put("twitterCheckBox", "false");
	    	}
		
	}	
	public void saveProperties(){
		controller.saveProperties();
	}
	public void setTwitterUse(boolean x){
		this.twitterCheckBox.setSelected(x);
	}
	public boolean getTwitterUse(){
		return this.twitterCheckBox.isSelected();
	}


}