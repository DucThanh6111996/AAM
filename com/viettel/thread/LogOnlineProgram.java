package com.viettel.thread;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.viettel.util.SSHUserInfo;

public class LogOnlineProgram extends Thread implements Serializable {

	private static final long serialVersionUID = 1L;
	private String host;
	private int port;
	private String username;
	private String password;
	private String prompt = "$";
	private Session session;
	private Channel channel;
	private StringBuilder log = new StringBuilder();
	private static Logger logger = Logger.getLogger(LogOnlineProgram.class);
	private String ctrlC = "\u0003";
	private StringBuffer connectErrorLog = new StringBuffer();
	// private OutputStream out = null;
	private BufferedInputStream is;
	private PrintStream shellStream = null;
	private Date startDate;
	private boolean stop;
	private boolean running;
	private Object key = new Object();
	private List<String> dataRow =new ArrayList<>();

	/*
	 * private OutputStream out ; private PrintStream shellStream=null;
	 */
	

	private List<String> subLogToList(String log) {
		List<String> list = new ArrayList<>();
		try {
			if ("".equals(log.trim()))
				return new ArrayList<>();
			log = log.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
			list = Arrays.asList(log.split("\n"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return list;
	}
	

	private String joinList(List<String> arr) {
		StringBuilder data = new StringBuilder();
		try {
			if(arr==null || arr.isEmpty()) return "";
			
			for(String line :arr ){
				data.append(line+"\r\n");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return data.toString();
	}
	
	public LogOnlineProgram(){
		
	}
	
	public LogOnlineProgram(String host, int port, String usename, String password, String promt) {
		
		  this.host = host; 
		  this.port = port;
		  this.username = usename;
		  this.password = password;
		  this.prompt = promt;
		 
	/*	this.host = "10.60.5.133";
		this.port = 22;
		this.username = "ptpm_checklist";
		this.password = "P9^5c4sc*76s";
		this.prompt = "$";*/
	}

	public boolean connect() {

		this.session = null;
		this.channel = null;
		// this.out = null;
		this.shellStream = null;

		this.connectErrorLog.delete(0, this.connectErrorLog.length());
		try {
			JSch shell = new JSch();
			// get a new session
			session = shell.getSession(username, this.host, this.port);
			// set user password and connect to a channel
			session.setUserInfo(new SSHUserInfo(password, true));
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "privatekey,keyboard-interactive,password");
			session.setConfig(config);
			session.setTimeout(10 * 1000);
		} catch (Exception e) {
			logger.info(e, e);
			this.connectErrorLog.append("\r\n" + ExceptionUtils.getStackTrace(e));
		}

		try {

			this.session.connect();
			this.channel = session.openChannel("shell");
			// this.out = new ByteArrayOutputStream();
			// this.channel.setOutputStream(this.out);
			this.shellStream = new PrintStream(this.channel.getOutputStream());

			((ChannelShell) channel).setPtyType("dumb");
			((ChannelShell) channel).setPtySize(4096, 24, 640, 480);
			this.channel.connect();
			this.shellStream = new PrintStream(this.channel.getOutputStream());

			this.is = new BufferedInputStream(this.channel.getInputStream());
			logger.info("Connected Server: " + host);
			this.startDate = new Date();
			this.stop = false;
            this.dataRow = new ArrayList<>();
			this.start();
			return true;
		} catch (Exception e) {
			logger.info(e, e);
			this.connectErrorLog.append("\r\n" + ExceptionUtils.getStackTrace(e));
		}

		return false;
	}

	public boolean sendBreakCommand() {
		try {
			if (!this.isLive())
				return false;
			shellStream.print(this.ctrlC + "\r");
			shellStream.flush();
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	public boolean sendCommand(String command) {
		try {
			if (!this.isLive())
				return false;
			shellStream.print(command + "\r");
			shellStream.flush();	
			this.dataRow.clear();
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	public void disconnect() {

		try {
			// if (this.out != null)
			// this.out.close();
			
			if (this.is != null)
				 this.is.close();
			if (this.shellStream != null)
				this.shellStream.close();
			if (this.channel != null)
				channel.disconnect();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		try {
			if (this.session != null)
				session.disconnect();
			logger.info("Disconected Server" + host);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			this.stop = true;
			try {
				Thread.sleep(2000L);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public String getDataReceive() {
		synchronized (this.key) {			
			if( ! this.isAlive() ) return this.log.toString();	
			if(this.dataRow==null) this.dataRow =new ArrayList<>();
			String allData =  this.joinList(this.dataRow)+this.log.toString();
			this.dataRow.clear();
			this.dataRow.addAll(this.subLogToList(allData));
			if(this.dataRow.size() > 1000){
				this.dataRow = this.dataRow.subList(this.dataRow.size()-1000, this.dataRow.size());
			}
			this.log.delete(0, this.log.length());		
			return this.joinList(dataRow);
		}
	}

	public ChannelSftp getSftpChanel() throws JSchException {
		ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
		sftp.connect();
		return sftp;
	}

	public boolean isLive() {
		try {
			if (session == null)
				return false;
			return session.isConnected();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	public String getConnectErrorLog() {
		return this.connectErrorLog == null ? "" : this.connectErrorLog.toString();
	}

	public Date getStartDate() {
		return startDate;
	}

	private void receive() throws IOException {
		synchronized (this.key) {
			int count = this.is.available();
			byte buf[] = new byte[count];
			count = this.is.read(buf);
			if (count < 0){
				this.logger.info("Session is closed");
				this.disconnect();
				return;	
			}
			this.log.append(new String(buf));
		}
	}

	@Override
	public void run() {
		this.startDate = new Date();
		this.running = true;
		System.err.println("Start Log online");
		try {
			while (!this.stop) {
				
				if((new Date()).getTime() - (this.startDate).getTime() > 30*60*1000 ){
					this.running = false;
					this.disconnect();
					this.log.delete(0, this.log.length());
					this.log.append("Connection time out");
					return;
				}
				this.receive();
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			this.running = false;
			System.err.println("Finish Log online");
		}
	}

	public static void main(String[] args) {
		try {

			/*String txt = "0123456789";
			StringBuilder  builder =new StringBuilder();
			builder.append(txt);
			builder.delete(0, builder.length());
			
			System.err.println("builder : "+builder);*/
			
			List<String> arr= new ArrayList<>();
			arr.add("a");
			arr.add("b");
			arr.add("c");
			arr.add("d");
			
			arr = arr.subList(arr.size()-4, arr.size());
			
			System.err.println(arr);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
