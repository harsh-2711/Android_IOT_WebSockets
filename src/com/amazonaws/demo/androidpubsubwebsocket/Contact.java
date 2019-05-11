package com.amazonaws.demo.androidpubsubwebsocket;

public class Contact {

    String Telephone;
    String name;

    public Contact() {
//        required constructor
    }

    /*get the telephone number*/
    public String getTelephone() {
        return Telephone;
    }

    /*ste the telephone number*/
    public void setTelephone(String telephone) {
        Telephone = telephone;
    }

    /*get the name*/
    public String getName() {
        return name;
    }

    /*set the name*/
    public void setName(String name) {
        this.name = name;
    }
}
