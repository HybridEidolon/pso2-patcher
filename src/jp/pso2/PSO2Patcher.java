package jp.pso2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class PSO2Patcher implements Runnable {
	
	private String location = "http://patch01.pso2gs.net/patch_prod/patches/";
	private String downloadDir = "C:/Users/" + System.getProperty("user.name") + "/Documents/SEGA/PHANTASYSTARONLINE2/download/";
	private String installPath = "C:/Program Files/SEGA/PHANTASYSTARONLINE2/pso2_bin/";
	
	private List<PatchFile> launcherFiles;
	private List<PatchFile> patchFiles;
	
	private List<PatchFile> updatedFiles = new LinkedList<PatchFile>();
	
	@Override
	public void run() {
		System.out.println("PSO2 Patcher");
		System.out.println("Downloading from " + location);
		System.out.println("Saving to " + downloadDir);
		System.out.println("Installing to " + installPath);
		
		parseLauncherList();
		parsePatchList();
		
		System.out.println("Checking launcher files");
		boolean res;
		res = downloadPatches(launcherFiles);
		if (res) {
			System.out.println("Installing updated launcher files");
			
			installFiles(updatedFiles);
			updatedFiles.clear();
		}
		
		System.out.println("Checking patch files");
		res = downloadPatches(patchFiles);
		if (res) {
			System.out.println("Installing updated patch files");
			
			installFiles(updatedFiles);
			updatedFiles.clear();
		}
		
		// download version.ver
		try {
			System.out.println("Downloading version.ver");
			ReadableByteChannel rbc = Channels.newChannel(new URL(location + "version.ver").openStream());
			File fff = new File(new File(downloadDir).getParent() + "version.ver");
			FileOutputStream fos = new FileOutputStream(fff);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Patching complete!");
	}
	
	private boolean downloadPatches(List<PatchFile> patches) {
		boolean changed = false;
		for (PatchFile f : patches) {
			File lf = new File(installPath + f.localPathNoPat);
			try {
				// check md5sum
				String md5 = MD5Sum.getMD5Checksum(lf.getAbsolutePath()).toUpperCase();
				if (md5.equals(f.md5sum)) {
					System.out.println("File " + f.localPathNoPat + " is up to date.");
					continue;
				}
			} catch (Exception e) {
				// file doesn't exist at destination
			}
			
			lf = new File(downloadDir + f.localPath);
			try {
				// check md5sum
				String md5 = MD5Sum.getMD5Checksum(lf.getAbsolutePath()).toUpperCase();
				if (md5.equals(f.md5sum)) {
					System.out.println("Using existing download to patch " + f.localPathNoPat);
					updatedFiles.add(f);
					changed = true;
					continue;
				}
			} catch (Exception e) {
				// file doesn't exist in predownloads
			}
			
			// download the patch because the files are different
			try {
				changed = true;
				System.out.println("Downloading " + ((double)f.fileSize/1048576) + " MiB of data for " + f.localPath + "...");
				ReadableByteChannel rbc = Channels.newChannel(f.url.openStream());
				File fff = new File(downloadDir + f.localPath);
				fff.getParentFile().mkdirs();
				FileOutputStream fos = new FileOutputStream(fff);
				fos.getChannel().transferFrom(rbc, 0, f.fileSize);
				fos.close();
				rbc.close();
				updatedFiles.add(f);
			} catch (Exception e) {
				System.out.println("Error downloading file " + f.localPath + ": " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
		return changed;
	}
	
	private void parseLauncherList() {
		System.out.println("Downloading launcherlist");
		launcherFiles = new LinkedList<PatchFile>();
		
		try {
			URL url = new URL(location + "launcherlist.txt");
			Scanner s = new Scanner(url.openStream());
			
			while (s.hasNextLine()) {
				PatchFile p = new PatchFile(s.nextLine(), location);
				launcherFiles.add(p);
			}
		} catch (IOException e) {
			System.out.println("Failed to download and parse patchlist");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void parsePatchList() {
		System.out.println("Downloading patchlist");
		patchFiles = new LinkedList<PatchFile>();
		
		try {
			URL url = new URL(location + "patchlist.txt");
			Scanner s = new Scanner(url.openStream());
			
			while (s.hasNextLine()) {
				PatchFile p = new PatchFile(s.nextLine(), location);
				patchFiles.add(p);
			}
			
		} catch (IOException e) {
			System.out.println("Failed to download and parse patchlist");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void installFiles(List<PatchFile> files) {
		for (PatchFile f : files) {
			FileChannel source = null;
			FileChannel dest = null;
			try {
				System.out.println("Installing " + f.localPathNoPat);
				source = new FileInputStream(new File(downloadDir + f.localPath)).getChannel();
				dest = new FileOutputStream(new File(installPath + f.localPathNoPat)).getChannel();
				dest.transferFrom(source, 0, source.size());
				source.close();
				dest.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		new PSO2Patcher().run();
	}
}
