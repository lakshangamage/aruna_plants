package com.intelligentz.arunaplants.model;

/**
 * Created by Lakshan on 2017-05-21.
 */

public class Customer {
    private String officer_id;
    private String officer_name;
    private String nic;
    private String name;
    private String address;
    private String birthday;
    private String mobile;

    public Customer() {
    }

    public Customer(String officer_id, String officer_name, String nic, String name, String address, String birthday, String mobile) {
        this.officer_id = officer_id;
        this.officer_name = officer_name;
        this.nic = nic;
        this.name = name;
        this.address = address;
        this.birthday = birthday;
        this.mobile = mobile;
    }

    public String getOfficer_id() {
        return officer_id;
    }

    public void setOfficer_id(String officer_id) {
        this.officer_id = officer_id;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOfficer_name() {
        return officer_name;
    }

    public void setOfficer_name(String officer_name) {
        this.officer_name = officer_name;
    }
}
