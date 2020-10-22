package com.viettel.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * @author quanns2
 */
public class RuntimeMysqlExec {
	private static Logger logger = LogManager.getLogger(RuntimeMysqlExec.class);
	public StreamWrapper getStreamWrapper(InputStream is, OutputStream input, String type, String file, String pwd, StringBuilder log) {
		return new StreamWrapper(is, input, type, file, pwd, log);
	}

	private Integer round = 1;

	private class StreamWrapper extends Thread {
		InputStream is = null;
		OutputStream input;
		String type = null;
		String message = null;
		String pwd = null;
		String file = null;
		StringBuilder log = null;

		public String getMessage() {
			return message;
		}

		StreamWrapper(InputStream is, OutputStream input, String type, String file, String pwd, StringBuilder log) {
			this.is = is;
			this.type = type;
			this.input = input;
			this.pwd = pwd;
			this.file = file;
			this.log = log;
		}

		public void run() {
			try {
				
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				StringBuffer buffer = new StringBuffer();
				String line = "";
				char c;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}

//				int counter = 0;
				while (br.ready()) {
					c = (char) br.read();
					System.out.print(c);
//					logger.info(c);
					line += c;
					log.append(c);
					
					if (c == '\n') {
						buffer.append(line + "\n");// .append("\n");
						line = "";
					}
					
					if (line.endsWith("Enter password: ") || line.endsWith("Password? (**********?) ")) {
						input.write(pwd.getBytes());
						input.flush();
						try {
							Thread.sleep(3000 * round);
						} catch (InterruptedException e) {
							logger.error(e.getMessage(), e);
						}
					}
				}
				
				message = buffer.toString();
			} catch (IOException ioe) {
				logger.error(ioe.getMessage(), ioe);
			} finally {
				try {
					if ("OUTPUT".equals(type) && input != null)
						input.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	// this is where the action is
	public String executeCommand(String command, String file, String pwd, StringBuilder log, Integer round) {
		this.round = round;
		Runtime rt = Runtime.getRuntime();
//		RuntimeExec rte = new RuntimeExec();
		StreamWrapper error, output;
		OutputStream input = null;
		String result = "";
//		String[] executeCmd = new String[]{"/bin/sh", "-c", "mysql -h10.55.54.34 -P3306 -u" + "root" + " -p"+ " < /tmp/execute8492601736370107482.sql > /tmp/execute2699199612987527641.log" };
//		String[] executeCmd = new String[]{"mysql", "-h10.55.54.34", "-P3306",  "-uroot", " -p", " < /tmp/execute8492601736370107482.sql > /tmp/execute2699199612987527641.log" };
		String[] executeCmd = new String[]{"/bin/sh", "-c", command };
		try {
			logger.info(command);
			Process proc = rt.exec(executeCmd);
/*			Process proc = null;
			ProcessBuilder p = new ProcessBuilder();
			p.command(commands);
			proc = p.start();*/
			Thread.sleep(10000 * round);
			input = proc.getOutputStream();
			error = getStreamWrapper(proc.getErrorStream(), input, "ERROR", file, pwd, log);
			output = getStreamWrapper(proc.getInputStream(), input, "OUTPUT", file, pwd, log);

			int exitVal = 0;
//			Thread.sleep(10000);
			error.start();
			output.start();

//			Thread.sleep(10000);
			error.join(3000);
			output.join(3000);
//			Thread.sleep(10000);
			exitVal = proc.waitFor();
			

			if(StringUtils.isNotEmpty(error.message)) {
				result += error.message;
			}
			
			if(StringUtils.isNotEmpty(output.message)) {
				result += output.message;
			}
			
//			System.out.println("Output: " + output.message + "\nError: " + error.message);
		} catch (IOException e) {
			logger.debug(e.getMessage(), e);
			logger.error(command + "\t" + e.getMessage());
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);

		} finally {
//			error, output;
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
		}
		
		return result;
	}
}