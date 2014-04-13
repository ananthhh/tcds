package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.ebean.Model;

@Entity
@Table(name = "user_details")
public class User extends Model{
	

	private static final long serialVersionUID = 1L;
	
	@Id
	private Long id;
	
	@Column(nullable=false, unique=true)
	String email;
	String name;
	String firstName;
	String lastName;
	
	@Column(nullable = false)
	String accessToken;
	
	@ManyToOne
	List<Envelope> activities;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public List<Envelope> getActivities() {
		return activities;
	}
	public void setActivities(List<Envelope> activities) {
		this.activities = activities;
	}
	
	

}
