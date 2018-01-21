package poafs.file;

public class FileMeta {
	private String id;
	private String fileName;
	private int length;
	
	public FileMeta(String id, String fileName, int length) {
		super();
		this.id = id;
		this.fileName = fileName;
		this.length = length;
	}
	public String getId() {
		return id;
	}
	public String getFileName() {
		return fileName;
	}
	public int getLength() {
		return length;
	}
}
