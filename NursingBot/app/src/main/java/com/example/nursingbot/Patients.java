package com.example.nursingbot;

public class Patients
{
    String Name,Mrcode,Med1,Med2;

    public Patients(String name, String mrcode,String med1,String med2) {
        Name = name;
        Mrcode = mrcode;
        Med1 = med1;
        Med2 = med2;
    }

    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
    }
    public String getMrcode() {
        return Mrcode;
    }
    public void setMrcode(String mrcode) {
        Mrcode = mrcode;
    }
    public String getMed1() {
        return Med1;
    }
    public void setMed1(String med1) {
        Med1 = med1;
    }
    public String getMed2() {
        return Med2;
    }
    public void setMed2(String med2) {
        Med2 = med2;
    }

    public Patients(){

    }
}

