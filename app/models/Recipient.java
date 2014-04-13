package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import play.db.ebean.Model;

@Entity
public class Recipient extends Model{
	
	public enum Status {
		NOT_RECIEVED,
		RECIEVED,
		SIGNED,
		DECLINED
	}
	
	@Column(nullable = false)
	private String email;
	
	@Column(nullable = false)
	private String name;
	
    @Enumerated(value=EnumType.STRING)
    @Column(nullable = false)
    private Status recipientStatus = Status.NOT_RECIEVED;
    
    @ManyToOne
    @JoinColumn(name="envelope_id")
	@Column(nullable = false)
    private Envelope envelope;
    
    @Transient
    private int recipientId;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	public Status getRecipientStatus() {
		return recipientStatus;
	}

	public void setRecipientStatus(Status recipientStatus) {
		this.recipientStatus = recipientStatus;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

	public void setEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}

	public int getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(int recipientId) {
		this.recipientId = recipientId;
	}


   
}
