package jp.pso2.gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class Options extends Dialog {

	protected Object result;
	protected Shell shlOptions;
	private Text txtMirrorURL;
	private List mirrorList;
	private Button btnSinglethreaded;
	private Text txtInstallationDirectory;
	private Text txtDownloadDirectory;
	private Button chkbtnInstall;
	private Button btnForceUpdateOff;
	private Button btnForceUpdateOn;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public Options(Shell parent, int style) {
		super(parent, style);
		setText("Options");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlOptions.open();
		shlOptions.layout();
		Display display = getParent().getDisplay();
		while (!shlOptions.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlOptions = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shlOptions.setText("Options");
		shlOptions.setSize(455, 337);
		shlOptions.setLayout(new GridLayout(1, false));
		
		TabFolder tabFolder = new TabFolder(shlOptions, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.setBounds(0, 0, 122, 43);
		
		TabItem tbtmPatching = new TabItem(tabFolder, SWT.NONE);
		tbtmPatching.setText("Patching");
		
		Composite compositeLauncher = new Composite(tabFolder, SWT.NONE);
		tbtmPatching.setControl(compositeLauncher);
		compositeLauncher.setLayout(new GridLayout(2, false));
		
		Group grpPatchMirrors = new Group(compositeLauncher, SWT.NONE);
		grpPatchMirrors.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		grpPatchMirrors.setText("Patch Mirrors");
		grpPatchMirrors.setLayout(new GridLayout(1, false));
		
		mirrorList = new List(grpPatchMirrors, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		mirrorList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		mirrorList.setSize(224, 81);
		mirrorList.setItems(LauncherPreferences.getSingleton().get("mirrors").split(";"));
		
		Composite composite = new Composite(grpPatchMirrors, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite.setSize(107, 31);
		composite.setLayout(new GridLayout(3, false));
		
		txtMirrorURL = new Text(composite, SWT.BORDER);
		txtMirrorURL.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == 13) {
					if (_addMirror(txtMirrorURL.getText())) {
						txtMirrorURL.setText("");
					}
				}
			}
		});
		txtMirrorURL.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnAdd = new Button(composite, SWT.NONE);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (_addMirror(txtMirrorURL.getText())) {
					txtMirrorURL.setText("");
				}
			}
		});
		btnAdd.setText("Add");
		
		Button btnRemove = new Button(composite, SWT.NONE);
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (mirrorList.getSelectionIndex() != -1) {
					txtMirrorURL.setText(mirrorList.getSelection()[0]);
					mirrorList.remove(mirrorList.getSelectionIndex());
				}
			}
		});
		btnRemove.setText("Remove");
		
		Group grpPatchMode = new Group(compositeLauncher, SWT.NONE);
		grpPatchMode.setText("Patch Mode");
		grpPatchMode.setLayout(new GridLayout(1, false));
		
		Button btnMultithreaded = new Button(grpPatchMode, SWT.RADIO);
		btnMultithreaded.setToolTipText("Update on multiple threads, potentially decreasing update time.");
		btnMultithreaded.setSelection(!LauncherPreferences.getSingleton().getBoolean("singlethread"));
		btnMultithreaded.setText("Multi-threaded");
		
		btnSinglethreaded = new Button(grpPatchMode, SWT.RADIO);
		btnSinglethreaded.setToolTipText("Update on a single thread. For compatibility.");
		btnSinglethreaded.setSelection(LauncherPreferences.getSingleton().getBoolean("singlethread"));
		btnSinglethreaded.setText("Single-threaded");
		
		Group grpForceUpdate = new Group(compositeLauncher, SWT.NONE);
		grpForceUpdate.setText("Force File Check");
		grpForceUpdate.setLayout(new GridLayout(1, false));
		grpForceUpdate.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		
		btnForceUpdateOff = new Button(grpForceUpdate, SWT.RADIO);
		btnForceUpdateOff.setToolTipText("Only check files if version does not match.");
		btnForceUpdateOff.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnForceUpdateOff.setText("Off");
		btnForceUpdateOff.setSelection(!LauncherPreferences.getSingleton().getBoolean("forceupdate"));
		
		btnForceUpdateOn = new Button(grpForceUpdate, SWT.RADIO);
		btnForceUpdateOn.setToolTipText("Check all files for updating, regardless of version");
		btnForceUpdateOn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnForceUpdateOn.setText("On");
		btnForceUpdateOn.setSelection(LauncherPreferences.getSingleton().getBoolean("forceupdate"));
		
		TabItem tbtmLocations = new TabItem(tabFolder, SWT.NONE);
		tbtmLocations.setText("Locations");
		
		Composite compositeLocations = new Composite(tabFolder, SWT.NONE);
		tbtmLocations.setControl(compositeLocations);
		compositeLocations.setLayout(new GridLayout(2, false));
		
		Label lblInstallationDirectory = new Label(compositeLocations, SWT.NONE);
		lblInstallationDirectory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInstallationDirectory.setBounds(0, 0, 55, 15);
		lblInstallationDirectory.setText("Installation Directory");
		
		txtInstallationDirectory = new Text(compositeLocations, SWT.BORDER);
		txtInstallationDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtInstallationDirectory.setText(LauncherPreferences.getSingleton().get("installdir"));
		
		Label lblDownloadDirectory = new Label(compositeLocations, SWT.NONE);
		lblDownloadDirectory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDownloadDirectory.setBounds(0, 0, 55, 15);
		lblDownloadDirectory.setText("Download Directory");
		
		txtDownloadDirectory = new Text(compositeLocations, SWT.BORDER);
		txtDownloadDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDownloadDirectory.setText(LauncherPreferences.getSingleton().get("downloaddir"));
		
		Label lblInstall = new Label(compositeLocations, SWT.NONE);
		lblInstall.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInstall.setText("Install");
		
		chkbtnInstall = new Button(compositeLocations, SWT.CHECK);
		chkbtnInstall.setSelection(LauncherPreferences.getSingleton().getBoolean("install"));
		
		Composite compositeButtons = new Composite(shlOptions, SWT.NONE);
		compositeButtons.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
		compositeButtons.setBounds(0, 0, 64, 64);
		
		Button btnCancel = new Button(compositeButtons, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlOptions.close();
			}
		});
		btnCancel.setText("Cancel");
		
		Button btnApply = new Button(compositeButtons, SWT.NONE);
		btnApply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				_apply();
			}
		});
		btnApply.setText("Apply");
		
		Button btnOk = new Button(compositeButtons, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				_apply();
				shlOptions.close();
			}
		});
		btnOk.setText("OK");

	}
	
	private void _apply() {
		// save mirror list
		String[] ml = mirrorList.getItems();
		StringBuilder b = new StringBuilder();
		
		for (int i = 0; i < ml.length; i++) {
			String s = ml[i];
			b.append(s);
			if (i != ml.length-1) {
				b.append(";");
			}
		}
		
		LauncherPreferences.getSingleton().set("mirrors", b.toString());
		
		LauncherPreferences.getSingleton().set("singlethread", btnSinglethreaded.getSelection());
		
		LauncherPreferences.getSingleton().set("install", chkbtnInstall.getSelection());
		
		LauncherPreferences.getSingleton().set("installdir", txtInstallationDirectory.getText());
		
		LauncherPreferences.getSingleton().set("downloaddir", txtDownloadDirectory.getText());
		
		LauncherPreferences.getSingleton().set("forceupdate", btnForceUpdateOn.getSelection());
		
		LauncherPreferences.getSingleton().save();
	}
	
	private boolean _addMirror(String text) {
		if (txtMirrorURL.getText().matches("((?:http|https|ftp|file)(?::\\/{2}[\\w]+)(?:[\\/|\\.]?)(?:[^\\s\"]*))")) {
			mirrorList.add(text);
			return true;
		} else {
			return false;
		}
	}
}
