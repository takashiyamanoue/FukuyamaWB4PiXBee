package org.yamaLab.pukiwikiCommunicator;

import org.yamaLab.pukiwikiCommunicator.language.InterpreterInterface;
import org.yamaLab.pukiwikiCommunicator.language.Util;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.io.serial.*;

import com.pi4j.platform.Platform;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.platform.PlatformManager;
import com.pi4j.util.Console;


//import com.pi4j.jni.I2C;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.JTabbedPane;


public class Pi4j implements InterpreterInterface, Runnable{
    Thread me;
    int counter;
    int frequency;
    int pie;
    GpioController gpio;
    I2CDevice i2c_device;
    int i2c_fd;
    I2CBus i2c;
    boolean debug=false;  /* */
    Serial serial=null;
    InterpreterInterface controller;
    String readingLine="";
    Pi4J_GUI gui;
    JTabbedPane tabbedPane;

  

   public Pi4j(InterpreterInterface ctlr){
		controller=ctlr;
		GpioPinDigitalInput myButton=null;
	    gpio=null;
		if(!debug){
		  try{
		      gpio = GpioFactory.getInstance();
		  }
		  catch(Exception e){
			System.out.println("Pi4j... Could not Gpio Instance.");
			gpio=null;
			return;
		  }

          // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
          if(gpio!=null){
		     myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);
          }

        // set shutdown state for this input pin
          if(myButton!=null)
             myButton.setShutdownOptions(true);
          // create and register gpio pin listener         
          myButton.addListener(new GpioPinListenerDigital() {
            //@Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
//                if(pie == 0){
                	if(event.getState().isHigh()){
//                		counter++;
                		pie = 1;
                	}else{
                		pie = 0;
                	}
//                }
            }

          });

		}
        pie = 0;
        
 //       start();     // comment out 20191002
        System.out.println(" ... complete the GPIO #02 circuit and see the listener feedback here in the console.");
        this.gui=new Pi4J_GUI(this);
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
    	tabbedPane.add("Pi4j",gui.getPanel());
    }
  private boolean parse1Param(String x, int[] v, String[] rest){
	    String param2=Util.skipSpace(x);
	    int vx=0;
	    String[] hex=new String[1];
	    int[] va=new int[1];
	// getting slave reg
	    if(Util.parseHex(param2, hex, rest)){
		     vx=Util.sh2i(hex[0]);
	    }
	    else
	    if(Util.parseInt(param2, va, rest)){
	       vx=v[0];
	    }
	    else{
		     return false;
	    }
	    v[0]=vx;
	   return true;
 }

	public String parseCommand(String x) {
		// TODO 自動生成されたメソッド・スタブ
		String[] rest=new String[1];
		if(Util.parseKeyWord(x,"get-a-0",rest)){
			               return ""+frequency;
		}
		else
		if(Util.parseKeyWord(x,"i2c ",rest)){ 
			 String subc=rest[0];
			 if(Util.parseKeyWord(subc, "use ", rest)){ //i2c use 0 or i2c use 1
				  String param=rest[0];
				  int[] v=new int[1];
				  if(Util.parseInt(param, v, rest)){
					   int bus=v[0];
					   if(bus==0){
						    try{
							     if(gpio!=null) {
//						       i2c_fd=I2C.i2cOpen("/dev/i2c-0");
								      i2c=I2CFactory.getInstance(I2CBus.BUS_0);
							        return "OK";
							     }
						    }
						    catch(Exception e){
							     System.out.println("i2c-0-open error:"+e);
						    }
					   }
					   else
					   if(bus==1){
						    try{
							     if(gpio!=null){
//							   i2c_fd=I2C.i2cOpen("/dev/i2c-1");
								      i2c=I2CFactory.getInstance(I2CBus.BUS_1);
							        return "OK";
							     }
						    }
						    catch(Exception e){
							     System.out.println("i2c-1-open error:"+e);
						    }
					   }
				  } // if(Util.parseInt(param, v, rest))
			} // if(Util.parseKeyWord(subc, "use ", rest))
			else
			if(Util.parseKeyWord(subc, "close", rest)){ // i2c close.
				if(gpio!=null)
//    				I2C.i2cClose(i2c_fd);
					try{
					  i2c.close();
					}
				  catch(Exception e){
					     System.out.println("i2c.close error..e="+e);
				  }
			}
			else
			if(Util.parseKeyWord(subc, "read-A-2 ", rest)){ // i2c read1 addr,reg
				String param=rest[0]; // target i2c slave device address
				int[] v=new int[1];
				String[] hex=new String[1];
				
				int addr;				
				if(!parse1Param(param,v,rest)) return "ERROR";
				addr=v[0];

				if(gpio!=null){
//				return ""+I2C.i2cReadByte(i2c_fd, addr,reg);				
					I2CDevice device=null;
					try{
						 device = i2c.getDevice(addr);						
					}
					catch(Exception e){
						   System.out.println("Pi4J i2c.getDevice("+addr+") error e="+e);
						   return "ERROR";
					}
					byte[] vs=new byte[3];
					try{
					   int rx=device.read(vs,0,2);
					   if( rx<1 ) return "-1";				   
						 int rtn= (vs[0]<<8)|(0xff & vs[1]);
						 return ""+rtn;
					    
					}
					catch(Exception e){
						   System.out.println("Pi4J i2c.read-A-2("+addr+") error e="+e);
						   //return "ERROR";
						     return "-1";
					}
					
				}
				else
					return "0";
			}			 
			else
			if(Util.parseKeyWord(subc, "read1 ", rest)){ // i2c read1 addr,reg
				String param=rest[0]; // target i2c slave device address
				int[] v=new int[1];
				String[] hex=new String[1];
				int addr;
				
				if(!parse1Param(param,v,rest)) return "ERROR";
				addr=v[0];

				String param2=rest[0];
				param2=Util.skipSpace(param2);
				
				if(!Util.parseKeyWord(param2,",",rest))	return "ERROR";

				param2=Util.skipSpace(rest[0]);
				
				int reg=0;
				if(!parse1Param(param2,v,rest)) return "ERROR";
				reg=v[0];

				if(gpio!=null){
//				return ""+I2C.i2cReadByte(i2c_fd, addr,reg);				
					I2CDevice device=null;
					try{
						 device = i2c.getDevice(addr);						
					}
					catch(Exception e){
						   System.out.println("Pi4J i2c.getDevice("+addr+") error e="+e);
						   return "ERROR";
					}
					try{
					    return ""+device.read(reg);
					}
					catch(Exception e){
						   System.out.println("Pi4J i2c.read("+reg+") error e="+e);
						   //return "ERROR";
						     return "-1";
					}
					
				}
				else
					return "0";
			}
			else
			if(Util.parseKeyWord(subc, "read2 ", rest)){ // i2c read2 addr,reg
				String param=rest[0]; // target i2c slave device address
				int[] v=new int[1];
				String[] hex=new String[1];
				
				param=Util.skipSpace(param);
				
				int addr=0;
				// getting slave addr
				if(!parse1Param(param,v,rest)) return "ERROR";
				addr=v[0];

				String param2=rest[0];
				param2=Util.skipSpace(param2);
				
				if(!Util.parseKeyWord(param2,",",rest)){
					return "ERROR";
				}

				param2=Util.skipSpace(rest[0]);
				if(!parse1Param(param2,v,rest)) return "ERROR";
				int reg=v[0];

				byte[] vs=new byte[2];
				if(gpio!=null){
					try{
					  I2CDevice device = i2c.getDevice(addr);
					  int rtn0=device.read(reg,vs,0,2);					
//				   int rtn0=I2C.i2cReadBytes(i2c_fd, addr,reg,2,0,vs);				
				   if(rtn0<1 ) return "-1";
					}
					catch(Exception e){
						 System.out.println("Pi4j i2c.read2 error e="+e);
						  return "-1";
					}
		//		  int rtn=(0xff & vs[0])<<8 |(0xff & vs[1]);
					int rtn= (vs[0]<<8)|(0xff & vs[1]);
				  return ""+rtn;
				}
				else return "0";
			}
			else
			if(Util.parseKeyWord(subc, "write-b ", rest)){ // i2c write-b slvAddr,v
				String param=rest[0]; // target i2c slave device address
				int[] v=new int[1];				
				String[] hex=new String[1];
				
				param=Util.skipSpace(param);
				
				int addr=0;
				// getting slave addr
      if(!parse1Param(param,v,rest)) return "ERROR";
      addr=v[0];

				String param2=rest[0];
				param2=Util.skipSpace(param2);
				
				if(!Util.parseKeyWord(param2,",",rest)) return "ERROR";

				param2=Util.skipSpace(rest[0]);
			  if(!parse1Param(param2,v,rest)) return "ERROR";
			 byte b=(byte)((v[0]) & 0xff);
				
      if(gpio!=null){
    	     try{
					    I2CDevice device = i2c.getDevice(addr);
					    device.write(b);	
    	     }
    	     catch(Exception e){
    	    	                System.out.println("Pi4j i2c.write1 error e="+e);
    	    	                return "ERROR";
    	     }
				    //I2C.i2cWriteByte(i2c_fd, addr, reg, (byte)vx);
      }
      return "OK";
			}			
			else
			if(Util.parseKeyWord(subc, "write1 ", rest)){ // i2c write1 slvAddr,reg,v
				String param=rest[0]; // target i2c slave device address
				int[] v=new int[1];				
				String[] hex=new String[1];
				
				param=Util.skipSpace(param);
				
				int addr=0;
				// getting slave addr
				if(!parse1Param(param, v, rest)) return "ERROR";
      addr=v[0];

				String param2=rest[0];
				param2=Util.skipSpace(param2);
				
				if(!Util.parseKeyWord(param2,",",rest)) return "ERROR";

				param2=Util.skipSpace(rest[0]);
				if(!parse1Param(param2,v,rest)) return "ERROR";
				// getting slave reg
				int reg=v[0];

				String param3=rest[0];
				param3=Util.skipSpace(param3);
				
				if(!Util.parseKeyWord(param3,",",rest)){
					return "ERROR";
				}

				param3=Util.skipSpace(rest[0]);
				if(!parse1Param(param3,v,rest)) return "ERROR";
				// getting slave reg
				int vx=v[0];
				
      if(gpio!=null){
    	     try{
					    I2CDevice device = i2c.getDevice(addr);
					    device.write(reg,(byte)vx);	
    	     }
    	     catch(Exception e){
    	    	                System.out.println("Pi4j i2c.write1 error e="+e);
    	    	                return "ERROR";
    	     }
				    //I2C.i2cWriteByte(i2c_fd, addr, reg, (byte)vx);
      }
      return "OK";
			}
			else
			if(Util.parseKeyWord(subc, "write2 ", rest)){ // i2c write2 slvAddr,v (v:2byte)
				String param=rest[0]; // target i2c slave device address
				int[] v=new int[1];
				String[] hex=new String[1];
				
				param=Util.skipSpace(param);
				
				int addr=0;
				// getting slave addr
				if(!parse1Param(param, v, rest)) return "ERROR";
      addr=v[0];

				String param2=rest[0];
				param2=Util.skipSpace(param2);
				
				if(!Util.parseKeyWord(param2,",",rest)){
					return "ERROR";
				}

				// getting slave reg
				param2=Util.skipSpace(rest[0]);
				if(!parse1Param(param2,v,rest)) return "ERROR";
				// getting slave reg
				int reg=v[0];

				String param3=rest[0];
				param3=Util.skipSpace(param3);
				
				if(!Util.parseKeyWord(param3,",",rest)){
					return "ERROR";
				}

				param3=Util.skipSpace(rest[0]);
				if(!parse1Param(param3,v,rest)) return "ERROR";
				// getting slave reg
				int vx=v[0];
												
				byte[] vs=new byte[2];
				vs[0]=(byte)(0xff & (vx>>8)) ;
				vs[1]=(byte)(0xff & vx);
				if(gpio!=null){
					try{
					  I2CDevice device = i2c.getDevice(addr);
					  device.write(reg,vs,0,2);					              	                              
					}
					catch(Exception e){
						 System.out.println("Pi4j i2c.write2 error e="+e);
						 return "ERROR";
					}
				//I2C.i2cWriteBytes(i2c_fd, addr, reg, 2, 0,vs);
				}
				return "OK";
			}
			else
			if(Util.parseKeyWord(subc, "write3 ", rest)){ // i2c writeN slvAddr,v1,v2,v3
				String param=rest[0]; // target i2c slave device address
				int[] v=new int[1];
				String[] hex=new String[1];
				
				param=Util.skipSpace(param);
				
				int addr=0;
				// getting slave addr
				if(!parse1Param(param, v, rest)) return "ERROR";
      addr=v[0];

				String param2=rest[0];
				param2=Util.skipSpace(param2);
				
				if(!Util.parseKeyWord(param2,",",rest)){
					return "ERROR";
				}

				// getting slave reg
				param2=Util.skipSpace(rest[0]);
				if(!parse1Param(param2,v,rest)) return "ERROR";
				// getting slave reg
				int reg=v[0];

				String param3=rest[0];
				param3=Util.skipSpace(param3);
				
				if(!Util.parseKeyWord(param3,",",rest)){
					return "ERROR";
				}

				param3=Util.skipSpace(rest[0]);
				if(!parse1Param(param3,v,rest)) return "ERROR";
				// getting slave reg
				int vx1=v[0];
				
				String param4=rest[0];
				param4=Util.skipSpace(param4);
				
				if(!Util.parseKeyWord(param4,",",rest)){
					return "ERROR";
				}

				param4=Util.skipSpace(rest[0]);
				if(!parse1Param(param4,v,rest)) return "ERROR";
				// getting slave reg
				int vx2=v[0];				
				
				String param5=rest[0];
				param5=Util.skipSpace(param5);
				
				if(!Util.parseKeyWord(param5,",",rest)){
					return "ERROR";
				}

				param5=Util.skipSpace(rest[0]);
				if(!parse1Param(param5,v,rest)) return "ERROR";
				// getting slave reg
				int vx3=v[0];				
				
				byte[] vs=new byte[3];
				vs[0]=(byte)(0xff & vx1);
				vs[1]=(byte)(0xff & vx2);
				vs[2]=(byte)(0xff & vx3);				
				if(gpio!=null){
					   try{
					  I2CDevice device = i2c.getDevice(addr);
					  device.write(reg,vs,0,3); 
					   }
					   catch(Exception e){
						   System.out.println("Pij4 i2c write3  error:"+e);
						       return "ERROR";
					   }
				   // I2C.i2cWriteBytesDirect(i2c_fd, addr, 3, 0,vs);
				}
				return "OK";
			}
			else
			if(Util.parseKeyWord(subc, "write4 ", rest)){ // i2c writeN slvAddr,v1,v2
				                                          // v1,v2...2byte, 2byte
				String param=rest[0]; // target i2c slave device address
				int[] v=new int[1];
				String[] hex=new String[1];
				
				param=Util.skipSpace(param);
				
				int addr=0;
				// getting slave addr
				if(!parse1Param(param, v, rest)) return "ERROR";
      addr=v[0];

				String param2=rest[0];
				param2=Util.skipSpace(param2);
				
				if(!Util.parseKeyWord(param2,",",rest)){
					return "ERROR";
				}

				// getting slave reg
				param2=Util.skipSpace(rest[0]);
				if(!parse1Param(param2,v,rest)) return "ERROR";
				// getting slave reg
				int reg=v[0];

				String param3=rest[0];
				param3=Util.skipSpace(param3);
				
				if(!Util.parseKeyWord(param3,",",rest)){
					return "ERROR";
				}

				param3=Util.skipSpace(rest[0]);
				if(!parse1Param(param3,v,rest)) return "ERROR";
				// getting slave reg
				int vx1=v[0];
				
				String param4=rest[0];
				param4=Util.skipSpace(param4);
				
				if(!Util.parseKeyWord(param4,",",rest)){
					return "ERROR";
				}

				param4=Util.skipSpace(rest[0]);
				if(!parse1Param(param4,v,rest)) return "ERROR";
				// getting slave reg
				int vx2=v[0];
								
				byte[] vs=new byte[4];
                vs[1]=(byte)(0xff & ((0xff00 & vx1)>>8));
				vs[0]=(byte)(0xff & vx1);
				vs[3]=(byte)(0xff & ((0xff00 & vx2)>>8));
				vs[2]=(byte)(0xff & vx2);
      if(gpio!=null){
//				I2C.i2cWriteBytes(i2c_fd, addr, reg, 4, 0,vs);
    	  try{
		     I2CDevice device = i2c.getDevice(addr);
		     device.write(reg,vs,0,4); 
        return "OK";
    	  }
    	  catch(Exception e){
    		        System.out.println("Pi4j i2c write4 error:"+e);
    		        return "ERROR";
    	  }
      }
			}		
		} // i2c
		else
			if(parseSerial(x)) return "OK";

		return "error";
	}
	private boolean parseSerial(String x){
		String[] rest=new String[1];
		String[] command=new String[1];
		if(Util.parseKeyWord(x,"serial ",rest)){ 
			String subc=Util.skipSpace(rest[0]);
			if(Util.parseKeyWord(subc, "send ", rest)){ //i2c use 0 or i2c use 1
				subc=Util.skipSpace(rest[0]);
				if(Util.parseStrConst(subc, command, rest)){
					try{
					    serial.writeln(command[0]);
					}
					catch(IOException e){
						controller.parseCommand("putSendBuffer error:"+e.toString());
						e.printStackTrace();
					}
					return true;
				}
			}
			
	}

		return false;
	}
	

	public InterpreterInterface lookUp(String x) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public void start(){
		if(gpio==null) return;
		if(me == null){
			me = new Thread(this,"Pi4j");
			me.start();
		}
	}

	public void stop(){
		me = null;
	}

	public void run() {
		if(gpio==null) return;
		// TODO 自動生成されたメソッド・スタブ
		 System.out.println("start pi4j thread");
		 long lastTime=System.currentTimeMillis();
		 counter=0;
		while(me != null){
			long timeNow=System.currentTimeMillis();
			if(timeNow>=lastTime+10000){
			  frequency = counter;
			  System.out.println("frequency="+frequency);
			  counter = 0;
			    lastTime=timeNow;
			}
			else{
				if(pie==1){
					counter++;
				}
			}
			try{
			Thread.sleep(100);
			}
			catch(InterruptedException e){}
		}
	}
	public void putApplicationTable(String key, InterpreterInterface obj) {
		// TODO Auto-generated method stub
		
	}

}
