package jp.pso2.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class LauncherPreferences {
	private static LauncherPreferences singleton;
	
	public static LauncherPreferences getSingleton() {
		if (singleton == null) {
			singleton = new LauncherPreferences();
		}
		return singleton;
	}
	
	public LauncherPreferences() {
		load();
	}
	
	private Properties properties;
	
	public String get(String name) {
		return properties.getProperty(name);
	}
	
	public int getInt(String name) {
		return Integer.parseInt(properties.getProperty(name));
	}
	
	public float getFloat(String name) {
		return Float.parseFloat(properties.getProperty(name));
	}
	
	public boolean getBoolean(String name) {
		return Boolean.parseBoolean(properties.getProperty(name));
	}
	
	public void set(String name, String value) {
		properties.setProperty(name, value);
	}
	
	private void _setIfNoValue(String name, String value) {
		String v = properties.getProperty(name);
		if (v == null) {
			set(name, value);
		}
	}
	
	public void set(String name, int value) {
		properties.setProperty(name, Integer.toString(value));
	}
	
	public void set(String name, float value) {
		properties.setProperty(name, Float.toString(value));
	}
	
	public void set(String name, boolean value) {
		properties.setProperty(name, Boolean.toString(value));
	}
	
	public void load() {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(new File("launcher.properties")));
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		_setDefaults();
	}
	
	private void _setDefaults() {
		_setIfNoValue("mirrors", "http://patch01.pso2gs.net/patch_prod/patches");
		_setIfNoValue("singlethread", Boolean.toString(false));
		_setIfNoValue("downloaddir", System.getProperty("user.home") + "\\My Documents\\SEGA\\PHANTASYSTARONLINE2\\download\\");
		_setIfNoValue("installdir", System.getenv("ProgramFiles") + "\\SEGA\\PHANTASYSTARONLINE2\\pso2_bin");
		_setIfNoValue("install", Boolean.toString(true));
		_setIfNoValue("forceupdate", Boolean.toString(false));
	}
	
	public void save() {
		try {
			properties.store(new FileOutputStream(new File("launcher.properties")), "PSO2 Launcher Settings");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
