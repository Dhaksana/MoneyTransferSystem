package com.bd.model;


import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table (name="account-details")
public class Account {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	int id;
	@Column(name="holderName", nullable=false)
	String holderName;
	@Column(name="balance", nullable=false)
	double balance;
	@Column(name="status", nullable=false)
	String status;
	@Column(name="version", nullable=false)
	String version;
	@Column (name="lastUpdated", nullable=false)
	ZonedDateTime lastUpdated;
	
	public Account(int id, String holderName, double balance, String status, String version,
			ZonedDateTime lastUpdated) {
		super();
		this.id = id;
		this.holderName = holderName;
		this.balance = balance;
		this.status = status;
		this.version = version;
		this.lastUpdated = lastUpdated;
	}
	
	public Account() {
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHolderName() {
		return holderName;
	}

	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public ZonedDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public void debit(double amount) {
		if (this.balance< amount) {
			System.out.println("Not possible");
		}
		else {
			this.balance = this.balance - amount;
			System.out.println("Success!!");
		}
		
	}
	public void credit() {
	}
	
	public boolean isActive()  {
		if (this.status == "ACTIVE") return true;
		return false;
	}
	
		
		
}
