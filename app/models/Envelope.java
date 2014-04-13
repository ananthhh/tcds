package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import play.db.ebean.Model;

/**
 * @author Ananth
 *
 * Main entity which holds everything together.
 * Whole application is basically about creating envelope and managing them.
 * So everything linked to it.
 * It is used to create JSON while creating draft
 * 
 */
@Entity
public class Envelope extends Model {

	private static final long serialVersionUID = 1L;
	public enum Status {
		CREATED,
		SENT,
		SIGNED
	}
	
	@Id
	private Long id;
	
	@Column(nullable=false, unique=true)
	private String envelopeId;
	
	@Column(nullable = false)
	private String emailSubject;
	
	@OneToMany(cascade=CascadeType.ALL)  
	private List<Recipient> recipientList = new ArrayList<Recipient>();
	
	@Column(nullable = false)
	private Date dateSent;
	
	@Column(nullable = false)
	private Date dateLastUpdated;
	
    @ManyToOne
    @JoinColumn(name="user_id")
	@Column(nullable = false)
    private User user;
    
    @OneToMany(cascade=CascadeType.ALL)  
    private List<Document> documents = new ArrayList<Document>();
    
    @Enumerated(value=EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.CREATED;
    
    @Transient
    private Recipients recipients = new Recipients();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEnvelopeId() {
		return envelopeId;
	}

	public void setEnvelopeId(String envelopeId) {
		this.envelopeId = envelopeId;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public List<Recipient> getRecipientList() {
		return recipientList;
	}

	public void setRecipientList(List<Recipient> recipientList) {
		this.recipientList = recipientList;
	}

	public Date getDateSent() {
		return dateSent;
	}

	public void setDateSent(Date dateSent) {
		this.dateSent = dateSent;
	}

	public Date getDateLastUpdated() {
		return dateLastUpdated;
	}

	public void setDateLastUpdated(Date dateLastUpdated) {
		this.dateLastUpdated = dateLastUpdated;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Recipients getRecipients() {
		return recipients;
	}

	public void setRecipients(Recipients recipients) {
		this.recipients = recipients;
	}


}
