package com.rktuhinbd.smartmessmanager.Model;

public class Members {

    private String memberId, name, phone, email, homeAddress, nationalId, profilePhotoUrl, occupation, organisation;

    public Members(String memberId, String name, String phone, String email, String homeAddress, String nationalId, String occupation, String organisation, String profilePhotoUrl) {
        this.memberId = memberId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.homeAddress = homeAddress;
        this.nationalId = nationalId;
        this.occupation = occupation;
        this.organisation = organisation;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getOrganisation() {
        return organisation;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }
}
