package models;

import java.util.ArrayList;
import java.util.List;

/*
 * Wrapper to enable different roles of recipients
 * It is used while JSON parsing
 */

public class Recipients {
	
	private List<Recipient> signers = new ArrayList<Recipient>();
	private List<Recipient> editors = new ArrayList<Recipient>();
	
	public List<Recipient> getSigners() {
		return signers;
	}
	public void setSigners(List<Recipient> signers) {
		this.signers = signers;
	}
	public List<Recipient> getEditors() {
		return editors;
	}
	public void setEditors(List<Recipient> editors) {
		this.editors = editors;
	}
	
	

}
