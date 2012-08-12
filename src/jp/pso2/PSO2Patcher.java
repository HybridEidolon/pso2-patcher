package jp.pso2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class PSO2Patcher implements Runnable {
	
	private List<String> mirrors = new ArrayList<String>();
	private String downloadDir = System.getProperty("user.home") + "/My Documents/SEGA/PHANTASYSTARONLINE2/download/";
	private String installPath = "C:/Program Files (x86)/SEGA/PHANTASYSTARONLINE2/pso2_bin/";
	private boolean install;
	
	private boolean errorFlag;
	private boolean multithreaded = true;
	private boolean forceUpdate = false;
	
	private ScheduledThreadPoolExecutor exec;
	private ScheduledThreadPoolExecutor downloadExec;
	private boolean changed;
	
	private List<PatchFile> launcherFiles;
	private List<PatchFile> patchFiles;
	
	private List<PatchFile> updatedFiles = new LinkedList<PatchFile>();
	
	private PatcherListener listener;
	
	private Runnable _downr;
	
	@Override
	public void run() {
		int numOps = 0;
		
		if (multithreaded) {
			final Random rand = new Random();
			exec = new ScheduledThreadPoolExecutor(32);
			exec.setMaximumPoolSize(32);
			ThreadFactory tf = new ThreadFactory() {

				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r, "STPE Thread " + rand.nextInt(1000));
					t.setDaemon(true);
					return t;
				}
				
			};
			exec.setThreadFactory(tf);
			
			downloadExec = new ScheduledThreadPoolExecutor(4);
			downloadExec.setMaximumPoolSize(4);
			downloadExec.setThreadFactory(tf);
		}
		
		// check all mirror versions
		String v = null;
		for (String m : mirrors) {
			try {
				String vv = null;
				Scanner scanner = new Scanner(new URL(m + "/version.ver").openStream());
				vv = scanner.nextLine();
				scanner.close();
				if (v != null && !vv.equals(v)) {
					throw new Exception("Version mismatch in mirror list at mirror " + m);
				}
				v = vv;
			} catch (Exception e) {
				listener.error(e.toString(), true);
				return;
			}
		}
		
		if (!forceUpdate) {
			String lv = null;
			try {
				File f = new File(new File(downloadDir).getParent() + "/version.ver");
				Scanner s = new Scanner(new FileInputStream(f));
				lv = s.nextLine();
				s.close();
			} catch (IOException e) {
				listener.error(e.toString(), true);
				return;
			}
			
			if (lv.equals(v)) {
				listener.done();
				return;
			}
		}
		
		parseLauncherList();
		if (errorFlag) {
			return;
		}
		parsePatchList();
		if (errorFlag) {
			return;
		}
		
		numOps = launcherFiles.size() * 2;
		numOps += patchFiles.size() * 2;
		
		listener.setNumberOfOperations(numOps);
		
		downloadPatches(launcherFiles);
		downloadPatches(patchFiles);
		if (errorFlag) {
			return;
		}
		
		if (multithreaded) {

			exec.shutdown();
			boolean r = false;
			try {
				System.out.println("waiting for termination");
				r = exec.awaitTermination(5, TimeUnit.HOURS);
				System.out.println("terminated");
				downloadExec.shutdown();
				downloadExec.awaitTermination(5, TimeUnit.HOURS);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			if (r) {
				System.out.println("terminated successfully");
			}
		}
		
		
		if (install) {
			System.out.println("installing");
			installFiles(updatedFiles);
			updatedFiles.clear();
		}
		
		if (errorFlag) {
			return;
		}
		
		// save version.ver
		try {
			File fff = new File(new File(downloadDir).getParent() + "/version.ver");
			PrintWriter writer = new PrintWriter(new FileOutputStream(fff));
			writer.write(v);
			writer.flush();
			writer.close();
			
			fff = new File(downloadDir + "/version.ver");
			writer = new PrintWriter(new FileOutputStream(fff));
			writer.write(v);
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			listener.error(e.toString(), true);
		}
		
		listener.done();
	}
	
	private boolean downloadPatches(List<PatchFile> patches) {
		changed = false;
		for (final PatchFile f : patches) {
			if (errorFlag) {
				break;
			}
			Runnable r = new Runnable() {
				public void run() {
					File lf;
					if (install) {
						lf = new File(installPath + "\\" + f.localPathNoPat);
						try {
							// check md5sum
							String md5 = MD5Sum.getMD5Checksum(lf.getAbsolutePath()).toUpperCase();
							if (md5.equals(f.md5sum)) {
								//System.out.println("File " + f.localPathNoPat + " is up to date.");
								listener.fileDownloaded();
								listener.filePatched();
								return;
							}
						} catch (Exception e) {
							// file doesn't exist at destination
						}
					}
					
					
					lf = new File(downloadDir + "\\" + f.localPath);
					try {
						// check md5sum
						String md5 = MD5Sum.getMD5Checksum(lf.getAbsolutePath()).toUpperCase();
						if (md5.equals(f.md5sum)) {
							//System.out.println("Using existing download to patch " + f.localPathNoPat);
							updatedFiles.add(f);
							listener.fileDownloaded();
							changed = true;
							return;
						}
					} catch (Exception e) {
						// file doesn't exist in predownloads
					}
					
					// download the patch because the files are different
					_downr = new Runnable() {
						public void run() {
							try {
								changed = true;
								System.out.println("Downloading " + (float)((double)f.fileSize/1048576) + " MiB of data for " + f.localPath + "...");
								ReadableByteChannel rbc = Channels.newChannel(f.url.openStream());
								File fff = new File(downloadDir + "\\" + f.localPath);
								fff.getParentFile().mkdirs();
								FileOutputStream fos = new FileOutputStream(fff);
								fos.getChannel().transferFrom(rbc, 0, f.fileSize);
								fos.close();
								rbc.close();
								listener.fileDownloaded();
								updatedFiles.add(f);
							} catch (Exception e) {
								listener.error(e.toString(), true);
								errorFlag = true;
							}
						};
					};
					if (multithreaded) {
						downloadExec.execute(_downr);
					} else {
						_downr.run();
					}
				};
			};
			
			if (multithreaded) {
				exec.execute(r);
			} else {
				r.run();
			}
			
		}
		return changed;
	}
	
	private void parseLauncherList() {
		System.out.println("Downloading launcherlist");
		launcherFiles = new LinkedList<PatchFile>();
		
		try {
			URL url = new URL(mirrors.get(_randomMirror()) + "/launcherlist.txt");
			Scanner s = new Scanner(url.openStream());
			File fff = new File(downloadDir + "/launcherlist.txt");
			PrintWriter pw = new PrintWriter(new FileOutputStream(fff));
			
			while (s.hasNextLine()) {
				String ss = s.nextLine();
				PatchFile p = new PatchFile(ss, mirrors.get(_randomMirror()));
				launcherFiles.add(p);
				pw.write(ss + "\n");
				pw.flush();
			}
			s.close();
			pw.close();
		} catch (IOException e) {
			listener.error("Failed to download and parse launcherlist: " + e.toString(), true);
			errorFlag = true;
			return;
		}
	}
	
	private void parsePatchList() {
		System.out.println("Downloading patchlist");
		patchFiles = new LinkedList<PatchFile>();
		
		try {
			URL url = new URL(mirrors.get(_randomMirror()) + "/patchlist.txt");
			Scanner s = new Scanner(url.openStream());
			File fff = new File(downloadDir + "/patchlist.txt");
			PrintWriter pw = new PrintWriter(new FileOutputStream(fff));
			
			while (s.hasNextLine()) {
				String ss = s.nextLine();
				PatchFile p = new PatchFile(ss, mirrors.get(_randomMirror()));
				patchFiles.add(p);
				pw.write(ss + "\n");
				pw.flush();
			}
			s.close();
			pw.close();
		} catch (IOException e) {
			listener.error("Failed to download and parse patchlist: " + e.toString(), true);
			errorFlag = true;
			return;
		}
	}
	
	private void installFiles(List<PatchFile> files) {
		for (PatchFile f : files) {
			FileChannel source = null;
			FileChannel dest = null;
			try {
				System.out.println("Installing " + f.localPathNoPat);
				source = new FileInputStream(new File(downloadDir + "\\" + f.localPath)).getChannel();
				dest = new FileOutputStream(new File(installPath + "\\" + f.localPathNoPat)).getChannel();
				dest.transferFrom(source, 0, source.size());
				source.close();
				dest.close();
				listener.filePatched();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int _randomMirror() {
		double rand = Math.random();
		rand *= mirrors.size();
		long m = Math.round(rand);
		if (m > mirrors.size() - 1) {
			m = 0;
		}
		return (int) m;
	}
	
	public void setPatcherListener(PatcherListener l) {
		listener = l;
	}
	
	public void setInstallPath(String path) {
		installPath = path;
	}
	
	public void setDownloadDir(String path) {
		downloadDir = path;
	}
	
	public void clearMirrors() {
		mirrors.clear();
	}
	
	public void addMirror(String mirror) {
		mirrors.add(mirror);
	}
	
	public void setInstall(boolean install) {
		this.install = install;
	}
	
	public void setMultithreaded(boolean multith) {
		multithreaded = multith;
	}
	
	public void setForceUpdate(boolean b) {
		forceUpdate = b;
	}
	
	public static void main(String[] args) {
		PSO2Patcher p = new PSO2Patcher();
		p.clearMirrors();
		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			if (i == 0) {
				p.setDownloadDir(s);
				continue;
			}
			p.addMirror(s);
		}
		p.setInstall(false);
		p.setPatcherListener(new PatcherListener() {
			@Override
			public void error(String error, boolean fatal) {
				System.out.println(error);
			}
			
			@Override
			public void fileDownloaded() {
				System.out.print(".");
			}
			
			@Override
			public void filePatched() {
				System.out.print("!");
			}
			
			@Override
			public void setNumberOfOperations(int ops) {
				System.out.println("numfiles: " + (ops/2));
			}
			
			@Override
			public void done() {
				System.out.println("Done!");
			}
		});
		
		p.run();
	}
}
