package jp.pso2;

public interface PatcherListener {
	public void setNumberOfOperations(int ops);
	public void fileDownloaded();
	public void filePatched();
	public void error(String error, boolean fatal);
	public void done();
}
