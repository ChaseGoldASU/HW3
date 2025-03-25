package application;

import javafx.beans.property.SimpleStringProperty;

//Class for the rows displayed for the username, roles, and one-time-passwords
public class Rows {
	final SimpleStringProperty rowNumber;
	final SimpleStringProperty userName;
	final SimpleStringProperty role;
	final SimpleStringProperty otp;

	public Rows(String rowNumber, String userName, String role, String otp) {
		this.rowNumber = new SimpleStringProperty(rowNumber);
		this.userName = new SimpleStringProperty(userName);
		this.role = new SimpleStringProperty(role);
		this.otp = new SimpleStringProperty(otp == null ? "" : otp);
	}

	public void setRowNumber(String rowNumber) {
		this.rowNumber.set(rowNumber);
	}

	public void setUserName(String userName) {
		this.userName.set(userName);
	}

	public void setRole(String role) {
		this.role.set(role);
	}

	public void setOTP(String otp) {
		this.otp.set(otp);
	}

	public String getRowNumber() {
		return rowNumber.get();
	}

	public String getUserName() {
		return userName.get();
	}

	public String getRole() {
		return role.get();
	}

	public String getOTP() {
		return otp.get();
	}
}