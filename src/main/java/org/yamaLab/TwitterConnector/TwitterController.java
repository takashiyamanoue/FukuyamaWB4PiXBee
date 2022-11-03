package org.yamaLab.TwitterConnector;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
//import org.yamalab.android.twitter2neomatrixex1.twitterconnector.Tweet;
import java.util.StringTokenizer;

import javax.swing.JTabbedPane;

import org.yamaLab.pukiwikiCommunicator.MainController;
import org.yamaLab.pukiwikiCommunicator.language.InterpreterInterface;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterController  implements InterpreterInterface
{
	static final String TAG = "TwitterController ";
		
	public String CONSUMER_KEY = ""; // dev.twitter.com ������
	public String CONSUMER_SECRET = ""; // dev.twitter.com ������
	public String CALLBACK_URL = "http://www.yama-lab.org" ; // "myapp://oauth";
	public String ACCESS_TOKEN="";
	public String ACCESS_TOKEN_SECRET="";
	private String nechatterStatus;
	private MainController main;
//	private Hashtable<String, String> context;
	private Properties setting;
	public RequestToken requestToken = null;
	public Twitter twitter = null;
	private Tweet mTweet = null;
	private TwitterLoginController mTwitterLoginController=null;
//	public OAuthAuthorization twitterOauth;
	private boolean accessingWeb=false;
    JTabbedPane tabbedPane;
    Twitter_GUI gui;
    /** Called when the activity is first created. */
    
    public TwitterController(MainController m) {
        main=m;
		System.out.println(TAG+"TwitterController");
//        mTwitterController=activity.mTwitterController;
        //������������������������������������������������������������
//	    SharedPreferences pref = context.getSharedPreferences("Twitter_setting", context.MODE_PRIVATE);
	    //���������������������K���v������������������������������������������������������
        mTweet = new Tweet(this);
		mTwitterLoginController = new TwitterLoginController(this);
		gui=new Twitter_GUI(this);
    }
	
    final private boolean isConnected(String nechatterStatus){
		if(nechatterStatus != null && nechatterStatus.equals("available")){
			return true;
		}else{
			return false;
		}
	}
    
    public void disconnectTwitter(){		
    	setting.put("status", null);
        
        //finish();
	}
	public boolean parseCommand(String x, String v){
		String subcmd=Util.skipSpace(x);
		String [] rest=new String[1];
		String [] match=new String[1];
	   	if(this.main==null) return false;
	   	if(this.mTweet==null) return false;
//		System.out.println(TAG+"parseCommand("+x+","+v+")");
	   	main.parseCommand("guiMessage",x+" "+v);
		int [] intv = new int[1];
		String tweetMessage="";
		if(v==null) return false;
		if(Util.parseKeyWord(subcmd,"tweet",rest)){
		   	String subsub=Util.skipSpace(rest[0]);
//		    SharedPreferences pref = context.getSharedPreferences("Twitter_setting", context.MODE_PRIVATE);
			Status status=null;
			Calendar calendar=Calendar.getInstance();
			int cy=calendar.get(Calendar.YEAR);
			int cmon=calendar.get(Calendar.MONTH)+1;
			int cday=calendar.get(Calendar.DATE);
			int ch=calendar.get(Calendar.HOUR_OF_DAY);
			int cmin=calendar.get(Calendar.MINUTE);
			int csec=calendar.get(Calendar.SECOND);
			int dp=v.indexOf("<day>");
			if(dp>0){
                v=v.replace("<day>", ""+cday);				
			}
			int dh=v.indexOf("<hour>");
			if(dh>0){
                v=v.replace("<hour>", ""+ch);				
			}
			int dm=v.indexOf("<min>");
			if(dm>0){
                v=v.replace("<min>", ""+cmin);				
			}
			
			if(twitter==null){
//				putMessage("error... twitter is not connected.");
				main.parseCommand("guiMessage", "twitter error... twitter is not connected.");
				return true;
			}
			try{
			   status=twitter.updateStatus(v);
			}
			catch(Exception e){
//				putMessage("error when tweet, "+e.getMessage());
				String wstatus="null";
				if(status!=null) wstatus=status.toString();
				main.parseCommand("guiMessage", "twitter error when tweet, "+e.getMessage()+" status="+wstatus);
				return true;
			}
		   	return true;
		}
		else
		if(Util.parseKeyWord(subcmd,"getNewTweet",rest)){
			nechatterStatus  = (String)setting.get("status");
			if(accessingWeb) return true;
			accessingWeb=true;
		   	if(isConnected(nechatterStatus)){
//		   		if(tweetMessage==null) return false;
		   	    mTweet.getHashTweet();
		   	}
		   	else{
		   		/* */
//					new connectTwitterTask().execute("");
		   		/* */
		   	}
		   	accessingWeb=false;
		   	return true;
		}
		else
		if(Util.parseKeyWord(subcmd,"set ",rest)){
		   	String subsub=Util.skipSpace(rest[0]);
		   	if(subsub.equals("uploadHashTag")){
		   		mTweet.setUploadHashTag(v);
		   		main.parseCommand("guiMessage", "twitter set uploadHashTag:"+ v);
		   		return true;
		   	}
		   	else
		   	if(subsub.equals("downloadHashTag")){
		   		main.parseCommand("guiMessage","twitter set downloadHashTag"+v);
		   		mTweet.setDownloadHashTag(v);
		   		return true;
		   	}
		   	else
		   	if(subsub.equals("OAuth")){
		   		StringTokenizer st=new StringTokenizer(v);
		   		String oAuthToken=st.nextToken();
		   		String oAuthVerifier=st.nextToken();
		   		mTwitterLoginController.startOAuthTask(oAuthToken,oAuthVerifier);

		   		return true;
		   	}
		   	else
		   	if(subsub.equals("accessingweb")){
		   		if(v.equals("true")){
		   			
		   		}
		   		else
		   		if(v.equals("false")){
		   			this.setAccessingWeb(false);		   			
		   		}

		   		return true;
		   	}
		   	
		   	return false;
		}
		else
		if(subcmd.equals("login")){
			if(v.equals("f")){
				loginTwitter();
			}
			else
			if(gui.getTwitterUse()){
			     loginTwitter();
			}
			this.mTweet.setTwitter(twitter);
		}
		return false;
	}
	private void loginTwitter(){
        try{
        // アクセストークンの設定
        AccessToken token = new AccessToken(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
         
        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        twitter.setOAuthAccessToken(token);
        User user = twitter.verifyCredentials();
        nechatterStatus="available";
        // 表示してみる。
        List<Status> list_status = twitter.getHomeTimeline();
        System.out.println("自分の名前：" + user.getScreenName());
        main.parseCommand("guiMessage","twitter login/自分の名前：" + user.getScreenName());
        System.out.println("概要　　　：" + user.getDescription());
        main.parseCommand("guiMessage","twitter login/概要　　　：" + user.getDescription());
//        for (Status status : list_status) {
//            System.out.println("ツイート：" + status.getText());
//        	service.parseCommand("guiMessage","ツイート：" + status.getText());        
//          }
        }
        catch(Exception e){
        	System.out.println("twitter error:"+e.toString());
            main.parseCommand("guiMessage","twitter error：" + e.toString());
        	nechatterStatus=null;
        }
        setting.setProperty("status", nechatterStatus);
		
	}
	public void setAccessingWeb(boolean x){
		accessingWeb=x;
	}
	public TwitterApplication getService(){
		return main;
	}
	public Properties getContext(){
		return setting;
	}

	public String getOutputText() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isTracing() {
		// TODO Auto-generated method stub
		return false;
	}

	public String parseCommand(String x) {
		// TODO Auto-generated method stub
		String subcmd=Util.skipSpace(x);
		String [] rest=new String[1];
		String [] match=new String[1];
		String[] param1=new String[1];
		String[] param2=new String[2];		
	   	if(this.main==null) return "ERROR";
	   	if(this.mTweet==null) return "ERROR";
//		System.out.println(TAG+"parseCommand("+x+","+v+")");
//	   	service.parseCommand("guiMessage",x);
		int [] intv = new int[1];
		String tweetMessage="";
		if(Util.parseKeyWord(subcmd,"tweet ",rest)){
			String l=subcmd.substring("tweet ".length());
			l=Util.skipSpace(l);
			if(!Util.parseStrConst(l, param1, rest)) return "ERROR";
			l=Util.skipSpace(rest[0]);
			if(Util.parseKeyWord(l, "when ", rest)) {
				l=Util.skipSpace(rest[0]);
				if(!Util.parseStrConst(l, param2, rest)) return "ERROR";
				tweetWhen(param1[0],param2[0]);
			}
			else{
				this.parseCommand("tweet", param1[0]);				
			}
			return "OK";

		}
		else
		if(Util.parseKeyWord(subcmd,"getNewTweet ",rest)){
			String htg=Util.skipSpace(rest[0]);
			htg=Util.deleteLastSpace(htg);
			mTweet.setDownloadHashTag(htg);
			nechatterStatus  = (String)setting.get("status");
			if(accessingWeb) return "ERROR";
			accessingWeb=true;
		   	if(isConnected(nechatterStatus)){
//		   		if(tweetMessage==null) return false;
		   	    mTweet.getHashTweet();
		   	}
		   	else{
		   		/* */
//					new connectTwitterTask().execute("");
		   		accessingWeb=false;
		   		return "ERROR";
		   		/* */
		   	}
		   	String rtn=null;
		   	for(int count=0;count<300; count++){
		   		rtn=this.getReturnTweet("getHashTweet");
		   		if(rtn!=null){
		   			accessingWeb=false;
		   			return rtn;
		   		}
		   		try{
     		   		Thread.sleep(100);
		   		}
		   		catch(InterruptedException e){
		   			
		   		}
		   	}
		   	accessingWeb=false;
		   	return "ERROR";
		}
		else
		if(Util.parseKeyWord(subcmd,"set ",rest)){
		   	String subsub=Util.skipSpace(rest[0]);
		   	if(Util.parseKeyWord(subsub,"uploadHashTag ",rest)){
		   		String l=Util.skipSpace(rest[0]);
				if(!Util.parseStrConst(l, param1, rest)) return "ERROR";
				String v=rest[0];
		   		mTweet.setUploadHashTag(v);
		   		main.parseCommand("guiMessage", "twitter set uploadHashTag:"+ v);
		   		return "OK";
		   	}
		   	else
		   	if(Util.parseKeyWord(subsub,"downloadHashTag ",rest)){
		   		String l=Util.skipSpace(rest[0]);
				if(!Util.parseStrConst(l, param1, rest)) return "ERROR";
				String v=rest[0];
		   		main.parseCommand("guiMessage","twitter set downloadHashTag"+v);
		   		mTweet.setDownloadHashTag(v);
		   		return "OK";
		   	}
		   	return "ERROR";
		}
		else
		if(subcmd.equals("login")){
			loginTwitter();
			return "OK";
		}
		return "ERROR";
		
	}
	public void tweetWhen(String tw, String t){
		Calendar calendar=Calendar.getInstance();
		int cy=calendar.get(Calendar.YEAR);
		int cmon=calendar.get(Calendar.MONTH)+1;
		int cday=calendar.get(Calendar.DATE);
		int ch=calendar.get(Calendar.HOUR_OF_DAY);
		int cmin=calendar.get(Calendar.MINUTE);
		int csec=calendar.get(Calendar.SECOND);
		StringTokenizer st=new StringTokenizer(t,",");
		String gy=st.nextToken();
		String gmon=st.nextToken();
		String gday=st.nextToken();
		String gh=st.nextToken();
		String gmin=st.nextToken();
		String gsec=st.nextToken();
		if(!gy.equals("*")){ /* same year ? */
			int igy=(new Integer(gy)).intValue();
			if(igy!=cy) return;
		}
		if(!gmon.equals("*")){ /* same month ? */
			int igmon=(new Integer(gmon)).intValue();
			if(igmon!=cmon) return;
		}
		if(!gday.equals("*")){ /* same day ?*/
			int igday=(new Integer(gday)).intValue();
			if(igday!=cday) return;
		}
		if(!gh.equals("*")){ /* same hour ? */
			int igh=(new Integer(gh)).intValue();
			if(igh!=ch) return;
		}
		if(!gmin.equals("*")){ /* same minutes ? */
			int igmin=(new Integer(gmin)).intValue();
			if(igmin!=cmin) return;
		}
		if(!gsec.equals("*")){ /* same seconds ? */
			int igsec=(new Integer(gsec)).intValue();
			if(igsec!=csec) return;
		}
		this.parseCommand("tweet", tw);
	}	

	public InterpreterInterface lookUp(String x) {
		// TODO Auto-generated method stub
		return null;
	}
	Hashtable<String,String> returnTable;
	public void returnTweet(String key, String val){
		if(returnTable==null){
			returnTable=new Hashtable();
		}
		returnTable.put(key, val);
	}
	public String getReturnTweet(String key){
		if(returnTable==null) return null;
		String x=returnTable.get(key);
		returnTable.remove(key);
		return x;
	}

	public void putApplicationTable(String key, InterpreterInterface obj) {
		// TODO Auto-generated method stub
		
	}
    public void setGui(JTabbedPane g){
    	this.tabbedPane=g;
    	if(g==null) return;
    	tabbedPane.add("twitter",gui.getTwitterPanel());
    	tabbedPane.add("twitter auth", gui.getTwitterAuthPanel());
    }
	public void setSetting(Properties s){
		setting=s;
		nechatterStatus  = (String)setting.get("status");		
        CONSUMER_KEY =setting.getProperty("oauth.consumerKey");
        CONSUMER_SECRET =setting.getProperty("oauth.consumerSecret");
        ACCESS_TOKEN =setting.getProperty("oauth.accessToken");
        ACCESS_TOKEN_SECRET =setting.getProperty("oauth.accessTokenSecret");
        CALLBACK_URL =setting.getProperty("oauth.callbackUrl");
        if(CALLBACK_URL==null){
        	CALLBACK_URL="http://www.yama-lab.org/";
        	s.put("oauth.callbackUrl", CALLBACK_URL);	
        }
		gui.setSetting(s);
	}
	public void saveProperties(){
		main.parseCommand("saveProperties");
	}
	

}