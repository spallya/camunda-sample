package com.vider.quantum.engine.entities.vider;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    private SubCategory subCategory;

    private String authorizedPerson;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String mobileNumber;
    private String alternateMobileNumber;
    private String city;
    private String state;
    private String pincode;
    @Lob
    private String notes;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private String slug;
    private String clientId;
    private String panNumber;
    private String firstName;
    private String lastName;
    private String image;
    private String localDirectoryPath;
    private LocalDateTime inactiveAt;
    private Integer organizationId;
    private Integer createdById;
    private Integer clientManagerId;
    private String gstNumber;
    private String tradeName;
    private LocalDate dob;
    private String designation;
    @Column(nullable = false)
    private boolean gstVerified;
    private String legalName;
    private String placeOfSupply;
    private String constitutionOfBusiness;
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    @Column(nullable = false)
    private boolean panVerified;
    private String fullName;
    private String buildingName;
    private String street;
    private Integer userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false)
    private boolean clientPortalAccess;
    private boolean issameaddress;
    @Column(length = 5000)
    private String address;
    private String middleName;
    private Integer clientImageId;
    private String tanNumber;
    @Enumerated(EnumType.STRING)
    private RegistrationType registrationType;
    private String gstRegistrationDate;
    private String clientNumber;
    private String countryCode;

    public enum Category {
        INDIVIDUAL, HUF, PARTNERSHIP_FIRM, LLP, COMPANY, TRUST, SOCIETY, AOP, BOI, CORPORATIONS, GOVERNMENT, ARTIFICIAL_JUDICIAL_PERSON, LOCAL_AUTHORITY
    }

    public enum SubCategory {
        INDIAN, FOREIGN, PRIVATE, PUBLIC, GOVERNMENT, OPC, SEC_8, PUBLIC_TRUST, PRIVATE_DISCRETIONARY_TRUST, SOCIETY, COOPERATIVE_SOCIETY, STATE, CENTRAL
    }

    public enum Status {
        ACTIVE, INACTIVE, DELETED
    }

    public enum RegistrationType {
        REGULAR_TAXPAYER, TAX_COLLECTOR, TAX_DEDUCTOR
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
