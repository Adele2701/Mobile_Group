package com.edu.carpool;

public class driverModelClass {

    String IC, StudentID, carPlateNum, carModel, carColour;

    public driverModelClass(String IC, String StudentID, String carPlateNum, String carModel, String carColour) {

        this.IC = IC;
        this.StudentID = StudentID;
        this.carPlateNum = carPlateNum;
        this.carModel = carModel;
        this.carColour = carColour;
    }

    public String getIC() {
        return IC;
    }

    public void setIC(String IC) {
        this.IC = IC;
    }

    public String getStudentID() {
        return StudentID;
    }

    public void setStudentID(String studentID) {
        StudentID = studentID;
    }

    public String getCarPlateNum() {
        return carPlateNum;
    }

    public void setCarPlateNum(String carPlateNum) {
        this.carPlateNum = carPlateNum;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarColour() {
        return carColour;
    }

    public void setCarColour(String carColour) {
        this.carColour = carColour;
    }
}
