package com.gpstracker.data_clases;

/**
 * Created by Lenovo on 04.10.2016.
 */

public class FirebaseUser {

    //variables

    private int id;         //id of the one run
    private String userName;
    private String email;
    private String password;

    //constructors

    public FirebaseUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public FirebaseUser(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    public FirebaseUser(int id, String userName, String email, String password) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    //getters setters


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
