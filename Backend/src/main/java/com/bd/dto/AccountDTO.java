package com.bd.dto;

import java.time.ZonedDateTime;

import com.bd.model.Account;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class AccountDTO {


    private int id;
    @NotBlank(message = "Account name must not be blank") //Rejects null + empty + spaces
    @Pattern(
            regexp = "^[A-Za-z ]{3,30}$",
            message = "Account name must be 3–30 characters and contain only letters"
        )
    
    private String holderName;
	double balance;
	String status;
	String version;
	ZonedDateTime lastUpdated;

    
    

    
    public AccountDTO() {}

    public AccountDTO(int id, String holderName, double balance, String status, String version,
			ZonedDateTime lastUpdated) {
    	this.id = id;
		this.holderName = holderName;
		this.balance = balance;
		this.status = status;
		this.version = version;
		this.lastUpdated = lastUpdated;
    }
    public AccountDTO(String holderName, double balance, String status, String version,
			ZonedDateTime lastUpdated) {
    	this.holderName = holderName;
		this.balance = balance;
		this.status = status;
		this.version = version;
		this.lastUpdated = lastUpdated;
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

	/* ---------- Entity → DTO ---------- */
    public static AccountDTO toDTO(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountDTO(
                account.getId(),
                account.getHolderName(),
                account.getBalance(),
                account.getStatus(),
                account.getVersion(),
                account.getLastUpdated()
                
        );
    }

    /* ---------- DTO → Entity ---------- */
    public static Account fromDTO(AccountDTO dto) {
        if (dto == null) {
            return null;
        }
        Account account = new Account();
//        account.setId(dto.getId());   // useful for update
        account.setHolderName(dto.getHolderName());
        account.setBalance(dto.getBalance());
        account.setStatus(dto.getStatus());
        account.setVersion(dto.getVersion());
        account.setLastUpdated(dto.getLastUpdated());
        return account;
    }
}
