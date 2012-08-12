package jp.pso2.gui;

import java.io.File;

import jp.pso2.PSO2Patcher;
import jp.pso2.PatcherListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class PatcherUI implements PatcherListener {

	protected Shell shlPhantasyStarOnline;

	private int opsDone;
	private int numOps;
	private ProgressBar progressBar;
	private Label lblStatus;
	
	private PSO2Patcher patcher;
	
	private long diff;
	private long lastOp;
	private long[] samples = new long[64];
	private int used;
	private int index;
	private Button btnUpdate;
	private Button btnPlay;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PatcherUI window = new PatcherUI();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlPhantasyStarOnline.open();
		shlPhantasyStarOnline.layout();
		while (!shlPhantasyStarOnline.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlPhantasyStarOnline = new Shell(SWT.CLOSE | SWT.MIN | SWT.TITLE);
		shlPhantasyStarOnline.setText("Phantasy Star Online 2");
		shlPhantasyStarOnline.setSize(505, 476);
		shlPhantasyStarOnline.setLayout(new GridLayout(2, false));
		
		Composite compositeUI = new Composite(shlPhantasyStarOnline, SWT.NONE);
		compositeUI.setLayout(new GridLayout(1, false));
		compositeUI.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		Browser browser = new Browser(compositeUI, SWT.NONE);
		//browser.setText("<p><b>Phantasy Star Online 2 News</b></p>\r\n<p><i>August 11, 2012</i></p>\r\n<p>Yay, new custom launcher/patcher! The official one is terrible so here's one that doesn't make you want to gouge your eyes out whenever it fails to patch.</p>\r\n<p><i>August 10, 2012</i> - <a href=\"http://bumped.org/psublog/sakai-addresses-the-tos-change-and-future-updates/\">Sakai Addresses the TOS Change and Future Updates</a></p>\r\n<p>Sakai added a clause to the PSO2 terms of service which basically forbids other countries from playing it. There was a similar description from the Alpha and Beta test but it has now returned.</p>");
		browser.setUrl("http://fury.srb2.org/pso2patcher.html");
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		
		progressBar = new ProgressBar(compositeUI, SWT.NONE);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		
		lblStatus = new Label(shlPhantasyStarOnline, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblStatus.setText("Version " + LauncherProps.VERSION);
		
		Composite compositeButtons = new Composite(shlPhantasyStarOnline, SWT.NONE);
		compositeButtons.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		btnPlay = new Button(compositeButtons, SWT.NONE);
		btnPlay.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				error("Not Yet Implemented", false);
			}
		});
		btnPlay.setText("Play");
		
		btnUpdate = new Button(compositeButtons, SWT.NONE);
		btnUpdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				_patch();
			}
		});
		btnUpdate.setText("Update");
		
		Button btnOptions = new Button(compositeButtons, SWT.NONE);
		btnOptions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Options(shlPhantasyStarOnline, SWT.DIALOG_TRIM).open();
			}
		});
		btnOptions.setText("Options");
		
		Button btnQuit = new Button(compositeButtons, SWT.NONE);
		btnQuit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				_close();
			}
		});
		btnQuit.setText("Quit");

	}
	
	private void _close() {
		shlPhantasyStarOnline.close();
	}

	@Override
	public void setNumberOfOperations(final int ops) {
		Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				opsDone = 0;
				numOps = ops;
				progressBar.setMaximum(numOps);
				_setStatusLabel();
			}
		});
	}

	@Override
	public void fileDownloaded() {
		_updateTimeSampler(System.currentTimeMillis() - lastOp);
		Display display = Display.getDefault();
		opsDone++;
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				_setStatusLabel();
			}
		});
		lastOp = System.currentTimeMillis();
	}

	@Override
	public void filePatched() {
		_updateTimeSampler(System.currentTimeMillis() - lastOp);
		Display display = Display.getDefault();
		opsDone++;
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				_setStatusLabel();
			}
		});
		lastOp = System.currentTimeMillis();
	}
	
	@Override
	public void error(final String error, boolean fatal) {
		Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				new ErrorDialog(shlPhantasyStarOnline, SWT.DIALOG_TRIM, error).open();
			}
		});
	}
	
	private void _setStatusLabel() {
		progressBar.setSelection(opsDone);
		float progress = (float)opsDone / numOps;
		progress *= 100;
		lblStatus.setText("Progress: " + progress + "%, " + Math.round(_minutesRemaining()) + "m remaining");
	}
	
	private void _updateTimeSampler(long diff) {
		if (used < samples.length) {
			used++;
		}

		samples[index] = diff;
		
		index++;
		if (index >= samples.length) {
			index = 0;
		}
	}
	
	private double _msRemaining() {
		double avg = 0;
		for (int i = 0; i < used; i++) {
			avg += samples[i];
		}
		avg /= used;
		return avg * (numOps - opsDone);
	}
	
	private double _secondsRemaining() {
		return _msRemaining() / 1000;
	}
	
	private double _minutesRemaining() {
		return _secondsRemaining() / 60;
	}
	
	private void _patch() {
		String ddir = LauncherPreferences.getSingleton().get("downloaddir");
		String idir = LauncherPreferences.getSingleton().get("installdir");
		
		if (!(new File(idir).exists())) {
			error("Installation directory " + idir + " doesn't exist!", true);
		}
		
		patcher = new PSO2Patcher();
		patcher.setPatcherListener(this);
		patcher.setDownloadDir(ddir);
		patcher.setInstallPath(idir);
		patcher.setInstall(LauncherPreferences.getSingleton().getBoolean("install"));
		patcher.setForceUpdate(LauncherPreferences.getSingleton().getBoolean("forceupdate"));
		patcher.clearMirrors();
		
		String[] mirrors = LauncherPreferences.getSingleton().get("mirrors").split(";");
		for (String m : mirrors) {
			patcher.addMirror(m);
		}
		
		Thread p = new Thread(patcher, "Patcher Thread");
		p.setDaemon(true);
		lastOp = System.currentTimeMillis();
		p.start();
		
		btnUpdate.setEnabled(false);
		btnPlay.setEnabled(false);
	}

	@Override
	public void done() {
		Display display = Display.getDefault();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				progressBar.setSelection(0);
				lblStatus.setText("Done!");
				
				btnUpdate.setEnabled(true);
				btnPlay.setEnabled(true);
			}
		});
	}
}
