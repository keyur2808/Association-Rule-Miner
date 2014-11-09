import java.util.List;

public class Conditions {
	// Template 1 rules
	private boolean isTemplate1;
	private String part;
	private List<String> items;
	private List<String> notItems;
	private List<String> someItems;
	private int sizeOfSomeItems;
	// Template 2 rules
	private int size;

	public boolean isTemplate1() {
		return isTemplate1;
	}

	public void setTemplate1(boolean isTemplate1) {
		this.isTemplate1 = isTemplate1;
	}

	public List<String> getItems() {
		return items;
	}

	public void setItems(List<String> items) {
		this.items = items;
	}

	public List<String> getNotItems() {
		return notItems;
	}

	public void setNotItems(List<String> notItems) {
		this.notItems = notItems;
	}

	public List<String> getSomeItems() {
		return someItems;
	}

	public void setSomeItems(List<String> someItems) {
		this.someItems = someItems;
	}

	public int getSizeOfSomeItems() {
		return sizeOfSomeItems;
	}

	public void setSizeOfSomeItems(int sizeOfSomeItems) {
		this.sizeOfSomeItems = sizeOfSomeItems;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getPart() {
		return part;
	}

	public void setPart(String part) {
		this.part = part;
	}
}
