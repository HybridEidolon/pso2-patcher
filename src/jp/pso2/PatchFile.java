package jp.pso2;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class PatchFile {
	public URL url;
	public String localPath;
	public String localPathNoPat;
	public long fileSize;
	public String md5sum;
	
	public PatchFile(String line, String location) {
		Scanner s = new Scanner(line);
		localPath = s.next();
		localPathNoPat = localPath.substring(0, localPath.indexOf(".pat"));
		fileSize = s.nextLong();
		md5sum = s.next();
		
		try {
			url = new URL(location + localPath);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
