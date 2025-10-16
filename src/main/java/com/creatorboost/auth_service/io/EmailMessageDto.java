package com.creatorboost.auth_service.io;

public class EmailMessageDto {
    private String toEmail;
    private String name;
    private String otp;
    private String  emailType;
    private String timestamp;
    private String serviceFrom;

    // ✅ Add no-args constructor for Jackson
    public EmailMessageDto() {}

    public EmailMessageDto(String toEmail, String name,String otp, String emailType, String timestamp, String serviceFrom) {
        this.toEmail = toEmail;
        this.name = name;
        this.otp = otp;
        this.emailType = emailType;
        this.timestamp = timestamp;
        this.serviceFrom = serviceFrom;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmailType() {
        return emailType;
    }
    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getServiceFrom() {
        return serviceFrom;
    }
    public void setServiceFrom(String serviceFrom) {
        this.serviceFrom = serviceFrom;
    }

    @Override
    public String toString() {
        return "EmailMessageDto{" +
                "toEmail='" + toEmail + '\'' +
                ", name='" + name + '\'' +
                ", otp='" + otp + '\'' +
                ", emailType='" + emailType + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", serviceFrom='" + serviceFrom + '\'' +
                '}';
    }
}