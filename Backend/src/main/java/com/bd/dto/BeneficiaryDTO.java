package com.bd.dto;

import com.bd.model.Beneficiary;

import jakarta.validation.constraints.NotBlank;

public class BeneficiaryDTO {
    private Long id;
    private String ownerAccountId;
    @NotBlank private String beneficiaryName;
    @NotBlank private String beneficiaryAccountNumber;
    private String bankName;
    private String ifsc;
    private String nickname;
    private boolean favorite;

    public static BeneficiaryDTO from(Beneficiary beneficiary) {
        BeneficiaryDTO dto = new BeneficiaryDTO();
        dto.id = beneficiary.getId();
        dto.ownerAccountId = beneficiary.getOwnerAccount().getId();
        dto.beneficiaryName = beneficiary.getBeneficiaryName();
        dto.beneficiaryAccountNumber = beneficiary.getBeneficiaryAccountNumber();
        dto.bankName = beneficiary.getBankName();
        dto.ifsc = beneficiary.getIfsc();
        dto.nickname = beneficiary.getNickname();
        dto.favorite = beneficiary.isFavorite();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOwnerAccountId() { return ownerAccountId; }
    public void setOwnerAccountId(String ownerAccountId) { this.ownerAccountId = ownerAccountId; }
    public String getBeneficiaryName() { return beneficiaryName; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }
    public String getBeneficiaryAccountNumber() { return beneficiaryAccountNumber; }
    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) { this.beneficiaryAccountNumber = beneficiaryAccountNumber; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getIfsc() { return ifsc; }
    public void setIfsc(String ifsc) { this.ifsc = ifsc; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}
