package com.example.nursingbot;

public class Medicines
{
    String Name,Quantity;

    public Medicines(String name, String quantity) {
        Name = name;
        Quantity = quantity;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public Medicines(){

    }
}

