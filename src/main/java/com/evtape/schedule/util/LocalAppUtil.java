package com.evtape.schedule.util;

import com.evtape.schedule.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class LocalAppUtil {

	private static Logger logger = LoggerFactory.getLogger(LocalAppUtil.class);

	public static List<String> getOutputFromProgram(String  cmd) throws Exception {
		final List<String> result=new ArrayList<>();
		String[] fullCommand=new String[]{"/bin/sh","-c",cmd};
		ProcessBuilder pb = new ProcessBuilder(fullCommand);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		Runnable getContent = () -> {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String content = null;
				while((content = br.readLine()) != null){
					logger.debug("line content: {}",content);
					result.add(content);
				}
			} catch (IOException e) {
				logger.error("cmd error: ",e);
			}
		};
		Thread t=new Thread(getContent);
		t.start();
		t.join();
		process.waitFor();
		process.destroy();
		return result;
	}

	public static boolean checkProcessLive(String uuid){
		if (uuid==null){
			logger.error("uuid is null");
			throw new BaseException(ErrorCode.PARAMETER_ERROR);
		}
		String cmd = "ps -fe | grep {0} | grep aac_adtstoasc | grep -v 'grep'";
		cmd = MessageFormat.format(cmd, uuid);
		try {
			logger.info("check job status: {}", cmd);
			List<String> times = getOutputFromProgram(cmd);
			if (times.size() > 0) {
				return true;
			}
		} catch (Exception e) {
			logger.error("error: ", e);
		}
		return false;
	}

	public static void finishProcess(String uuid){
		if (uuid==null){
			logger.info("uuid is null");
			throw new BaseException(ErrorCode.PARAMETER_ERROR);
		}
		String cmd = "ps -fe | grep "+uuid+" | grep aac_adtstoasc | grep -v grep | awk '{print $2}' | xargs kill ";
		try {
			logger.info("start killing job: {}", cmd);
			LocalAppUtil.getOutputFromProgram(cmd);
		} catch (Exception e) {
			logger.warn("killing thread error", e);
		}
	}

	public static void transform(String origin,String fileName){
		if (origin==null||fileName==null){
			logger.info("param is null");
			throw new BaseException(ErrorCode.PARAMETER_ERROR);
		}
		String cmd = "ffmpeg -i {0} -vcodec copy -acodec copy {1}";
		cmd = MessageFormat.format(cmd, origin, fileName);

		try {
			logger.info("start transforming job: {}", cmd);
			LocalAppUtil.getOutputFromProgram(cmd);
		} catch (Exception e) {
			logger.warn("视频转换异常", e);
		}
	}

}
