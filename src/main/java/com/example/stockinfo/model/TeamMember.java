package com.example.stockinfo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamMember {
    private String name;
    private String designation;
    private String role;
    private String bio;
    
    // Detailed fields
    private String email;
    private String linkedIn;
    private String detailedBio;

    public TeamMember() {}

    public TeamMember(String name, String designation, String role, String bio) {
        this.name = name;
        this.designation = designation;
        this.role = role;
        this.bio = bio;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLinkedIn() { return linkedIn; }
    public void setLinkedIn(String linkedIn) { this.linkedIn = linkedIn; }

    public String getDetailedBio() { return detailedBio; }
    public void setDetailedBio(String detailedBio) { this.detailedBio = detailedBio; }
}
