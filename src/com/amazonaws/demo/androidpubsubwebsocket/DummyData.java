package com.amazonaws.demo.androidpubsubwebsocket;

import java.util.ArrayList;
import java.util.List;

public class DummyData {

    /*sample data*/
    public String[] telephoneNumbers = {"1234", "5678", "9111", "0987", "2908", "3876", "5210", "9217", "5104", "9211"};
    private String[] names = {"Jack", "James", "John", "Peter", "Pat", "Willy", "Sarah", "Solly", "Cathy", "Charles"};

    public DummyData() {
//        required constructor
    }

    /*get all the telephone numbers*/
    public String[] getTelephoneNumbers() {
        return telephoneNumbers;
    }

    /*get all the contacts*/
    public List<Contact> getContacts() {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < telephoneNumbers.length; i++) {
            Contact contact = new Contact();
            contact.setTelephone(telephoneNumbers[i]);
            contact.setName(names[i]);
            contacts.add(contact);
        }
//        return loadContacts(telephoneNumbers, names);
        return contacts;

    }

    /*create list of sample contacts*/
//    private List<Contact> loadContacts(String[] telephoneNumbers, String[] names) {
//        List<Contact> contacts = new ArrayList<>();
//        for (int i = 0; i < telephoneNumbers.length; i++) {
//            Contact contact = new Contact();
//            contact.setTelephone(telephoneNumbers[i]);
//            contact.setName(names[i]);
//            contacts.add(contact);
//        }
//        return contacts;
//    }
}
