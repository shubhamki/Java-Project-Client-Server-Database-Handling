package finalproject.entities;

/**
 * This is the person Object class implementation.
 * This class is required to transfer object from client to server.
 * 
 * @author Shubham Ingale
 *
 */

public class Person implements java.io.Serializable {
	
	private String FirstName;
	private String LastName;
	private String Age;
	private String City;
	private String ID;
	private String Sent;
	private static final long serialVersionUID = 4190276780070819093L;

	public Person(String FName, String LName, String Age, String City, String Sent, String ID)
	{
		this.FirstName = FName;
		this.LastName = LName;
		this.Age = Age;
		this.City = City;
		this.ID = ID;
		this.Sent = Sent;
	}
	
	public String getLastName() {
		return LastName;
	}

	public String getFirstName() {
		return FirstName;
	}

	public String getAge() {
		return Age;
	}

	@Override
	public String toString() {
		return "Person [FirstName=" + FirstName + ", LastName=" + LastName + ", Age=" + Age + ", City=" + City + ", ID="
				+ ID + ", Sent=" + Sent + "]";
	}

	public String getCity() {
		return City;
	}

	public String getID() {
		return ID;
	}

	/**
	 * @return the sent
	 */
	public String getSent() {
		return Sent;
	}

	// this is a person object that you will construct with data from the DB
	// table. The "sent" column is unnecessary. It's just a person with
	// a first name, last name, age, city, and ID.
}
