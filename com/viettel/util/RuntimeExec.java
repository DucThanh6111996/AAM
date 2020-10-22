package com.viettel.util;

import com.viettel.bean.LogObjectInfo;
import com.viettel.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author quanns2
 */
public class RuntimeExec {
	private static Logger logger = LogManager.getLogger(RuntimeExec.class);

	//20181005_tudn_start ghi log real time
//	public StreamWrapper getStreamWrapper(InputStream is, OutputStream input, String type, String file, String pwd, StringBuilder log) {
//		return new StreamWrapper(is, input, type, file, pwd, log);
//	}
	public StreamWrapper getStreamWrapper(InputStream is, OutputStream input, String type, String file, String pwd, StringBuilder log, StringBuilder logCommand, File fileLog) {
		return new StreamWrapper(is, input, type, file, pwd, log, logCommand, fileLog);
	}
	//20181005_tudn_end ghi log real time

	private Integer round = 1;

	private class StreamWrapper extends Thread {
		InputStream is = null;
		OutputStream input;
		String type = null;
		String message = null;
		String pwd = null;
		String file = null;
		StringBuilder log = null;
		//20181005_tudn_start ghi log real time
		File fileLog = null;
		StringBuilder logCommand = null;
		//20181005_tudn_end ghi log real time

		public String getMessage() {
			return message;
		}

		//20181005_tudn_start ghi log real time
//		StreamWrapper(InputStream is, OutputStream input, String type, String file, String pwd, StringBuilder log) {
		StreamWrapper(InputStream is, OutputStream input, String type, String file, String pwd, StringBuilder log, StringBuilder logCommand, File fileLog) {
		//20181005_tudn_start ghi log real time
			this.is = is;
			this.type = type;
			this.input = input;
			this.pwd = pwd;
			this.file = file;
			this.log = log;
			//20181005_tudn_start ghi log real time
			this.fileLog = fileLog;
			this.logCommand = logCommand;
			//20181005_tudn_end ghi log real time
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
					/*20181121_hoangnd_log command_start*/
					logCommand.append(c);
					/*20181121_hoangnd_log command_end*/

					if (c == '\n') {
						buffer.append(line + "\n");// .append("\n");
						line = "";
					}
					
					if (line.endsWith("Enter password: ") || line.endsWith("Password? (**********?) ")) {
						input.write(pwd.getBytes());
						input.flush();
						try {
							logger.info("Sleep...." + 3000*round);
							Thread.sleep(3000 * round);
						} catch (InterruptedException e) {
							logger.error(e.getMessage(), e);
						}catch (Exception ex){
							logger.error(ex.getMessage(), ex);
						}
					}
				}
				
				message = buffer.toString();
			} catch (IOException ioe) {
				logger.error(ioe.getMessage(), ioe);
			}
			catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			finally {
				try {
					if ("OUTPUT".equals(type) && input != null)
						input.close();

				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}

				//20181005_tudn_start ghi log real time
				if(message.length()>0) {
					try {
						Boolean fileLength = false;
						BufferedReader br = new BufferedReader(new FileReader(fileLog.getPath()));
						Date currentTime1 = new Date();
						while (true) {
							String line = br.readLine();
							if (line != null) {
								log.append(line + "\n");
								/*20181121_hoangnd_log command_start*/
								logCommand.append(line + "\n");
								/*20181121_hoangnd_log command_end*/
								fileLength = true;
								if (line.contains("quit"))
									break;
							} else {
								Thread.sleep(100);
								if (!fileLength && ((new Date().getTime() - currentTime1.getTime()) > 60 * 1000)) //Neu load file log 60 giay ma ko co du lieu
									break;
							}
						}
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					} catch (FileNotFoundException e) {
						logger.error(e.getMessage(), e);
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
				//20181005_tudn_end ghi log real time
			}
		}
	}

	// this is where the action is
	//20181005_tudn_start ghi log real time
//	public String executeCommand(String command, String file, String pwd, StringBuilder log, Integer round) {
	public String executeCommand(String command, String file, String pwd, StringBuilder log, StringBuilder logCommand, Integer round, File fileOutputLog) {
	//20181005_tudn_end ghi log real time
		this.round = round;
		Runtime rt = Runtime.getRuntime();
//		RuntimeExec rte = new RuntimeExec();
		StreamWrapper error, output;
		OutputStream input = null;
		String result = "";
//		String[] executeCmd = new String[]{"/bin/sh", "-c", "mysql -h10.55.54.34 -P3306 -u" + "root" + " -p"+ " < /tmp/execute8492601736370107482.sql > /tmp/execute2699199612987527641.log" };
//		String[] executeCmd = new String[]{"mysql", "-h10.55.54.34", "-P3306",  "-uroot", " -p", " < /tmp/execute8492601736370107482.sql > /tmp/execute2699199612987527641.log" };
//		String[] executeCmd = new String[]{"/bin/sh", "-c", command };
		try {
			logger.info(command);
			Date currentTime = new Date();
			Process proc = rt.exec(command);
/*			Process proc = null;
			ProcessBuilder p = new ProcessBuilder();
			p.command(commands);
			proc = p.start();*/
			Thread.sleep(10000 * round);
			input = proc.getOutputStream();
			/*20181121_hoangnd_log command_start*/
			error = getStreamWrapper(proc.getErrorStream(), input, "ERROR", file, pwd, log, logCommand, fileOutputLog);
			output = getStreamWrapper(proc.getInputStream(), input, "OUTPUT", file, pwd, log, logCommand, fileOutputLog);
			/*20181121_hoangnd_log command_end*/

			int exitVal = 0;
//			Thread.sleep(10000);
			error.start();
			output.start();

//			Thread.sleep(10000);
			error.join(3000);
			output.join(3000);
//			Thread.sleep(10000);

//			proc.waitFor(100, TimeUnit.MILLISECONDS);  // let the process run for 5 seconds
//			proc.destroy();                     // tell the process to stop
//			proc.waitFor(200, TimeUnit.MILLISECONDS); // give it a chance to stop
//			proc.destroyForcibly();
			exitVal = proc.waitFor();


			if(StringUtils.isNotEmpty(error.message)) {
				result += error.message;
			}
			
			if(StringUtils.isNotEmpty(output.message)) {
				result += output.message;
			}
			
//			System.out.println("Output: " + output.message + "\nError: " + error.message);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			logger.error(command + "\t" + e.getMessage());
			result += command + "\t" + e.getMessage();
		} catch (InterruptedException e) {
        	/*20181016_hoangnd_timeout_start*/
//			logger.error(e.getMessage(), e);
			logger.info("interrupted!");
			result += command + "\t" + "interrupted";
        	/*20181016_hoangnd_timeout_end*/
		}
		catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		finally {
//			error, output;
			if (input != null)
				try {
					input.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
		}
		
		return result;
	}
}