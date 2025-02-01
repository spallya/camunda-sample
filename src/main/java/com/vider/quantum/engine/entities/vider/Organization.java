package com.vider.quantum.engine.entities.vider;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "organization")
@Data
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isActive;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)")
    private LocalDateTime updatedAt;

    @Column(name = "city", length = 255)
    private String city;

    @Column(name = "state", length = 255)
    private String state;

    @Column(name = "logo", length = 255)
    private String logo;

    @Column(name = "gst_number", length = 255)
    private String gstNumber;

    @Column(name = "mobile_number", length = 255)
    private String mobileNumber;

    @Column(name = "pincode", length = 255)
    private String pincode;

    @Column(name = "alternate_mobile_number", length = 255)
    private String alternateMobileNumber;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "primary_contact_full_name", length = 255)
    private String primaryContactFullName;

    @Column(name = "primary_contact_email", length = 255)
    private String primaryContactEmail;

    @Column(name = "primary_contact_mobile_number", length = 255)
    private String primaryContactMobileNumber;

    @Column(name = "gst_attachment", length = 255)
    private String gstAttachment;

    @Column(name = "pan_number", length = 255)
    private String panNumber;

    @Column(name = "pan_attachment", length = 255)
    private String panAttachment;

    @Column(name = "registration_number", length = 255)
    private String registrationNumber;

    @Column(name = "building_name", length = 255)
    private String buildingName;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "legal_name", length = 255)
    private String legalName;

    @Column(name = "trade_name", length = 255)
    private String tradeName;

    @Column(name = "place_of_supply", length = 255)
    private String placeOfSupply;

    @Column(name = "constitution_of_business", length = 255)
    private String constitutionOfBusiness;

    @Column(name = "registration_date", length = 255)
    private String registrationDate;

    @Column(name = "gst_status", length = 255)
    private String gstStatus;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "middle_name", length = 255)
    private String middleName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private Category category;

    @Column(name = "gst_verified", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean gstVerified;

    @Column(name = "pan_verified", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean panVerified;

    @Column(name = "date_of_formation", length = 255)
    private String dateOfFormation;

    @Column(name = "building_no", length = 255)
    private String buildingNo;

    @Column(name = "district", length = 255)
    private String district;

    @Column(name = "config", columnDefinition = "json")
    private String config;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "floor_number", length = 255)
    private String floorNumber;

    @Column(name = "logged_in", columnDefinition = "json")
    private String loggedIn;

    @Column(name = "smtp", columnDefinition = "json")
    private String smtp;

    @Column(name = "org_gst_storage_id")
    private Integer orgGstStorageId;

    @Column(name = "org_pan_storage_id")
    private Integer orgPanStorageId;

    @Column(name = "primary_contact_designation", length = 45)
    private String primaryContactDesignation;

    @Column(name = "aws_bucket", length = 255)
    private String awsBucket;

    @Column(name = "quantum_expiry", columnDefinition = "json")
    private String quantumExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_system", nullable = false, length = 50, columnDefinition = "VARCHAR(255) DEFAULT 'AMAZON'")
    private StorageSystem storageSystem;

    @Column(name = "domain_url", length = 255)
    private String domainUrl;

    @Column(name = "header", length = 255)
    private String header;

    @Column(name = "footer", length = 255)
    private String footer;

    public enum Category {
        INDIVIDUAL, HINDU_UNDIVIDED_FAMILY, PARTNERSHIP_FIRM, LLP, COMPANY, ASSOCIATION_OF_PERSONS, BODY_OF_INDIVIDUALS, TRUST, GOVERNMENT, LOCAL_AUTHORITY, ARTIFICIAL_JURIDICAL_PERSON
    }

    public enum StorageSystem {
        AMAZON, MICROSOFT, GOOGLE
    }
}
