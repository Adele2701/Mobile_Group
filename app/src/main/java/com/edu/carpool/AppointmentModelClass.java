package com.edu.carpool;

public class AppointmentModelClass {

    String from_address, to_address, driver_name, driver_id, date_time, custom_request, user_id, status;



    public AppointmentModelClass(String from_address, String to_address, String driver_name, String driver_id, String date_time, String custom_request, String user_id, String status) {
        this.from_address = from_address;
        this.to_address = to_address;
        this.driver_name = driver_name;
        this.driver_id = driver_id;
        this.date_time = date_time;
        this.custom_request = custom_request;
        this.user_id = user_id;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFrom_address() {
        return from_address;
    }

    public void setFrom_address(String from_address) {
        this.from_address = from_address;
    }

    public String getTo_address() {
        return to_address;
    }

    public void setTo_address(String to_address) {
        this.to_address = to_address;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getCustom_request() {
        return custom_request;
    }

    public void setCustom_request(String custom_request) {
        this.custom_request = custom_request;
    }
}
