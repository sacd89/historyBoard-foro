package com.example.hector.history;

import java.util.Date;

public class Mensaje {

    public String usuario;
    public String date;
    public String msg;

    public Mensaje(String usuario, String date, String msg){
        this.usuario = usuario;
        this.date = date;
        this.msg = msg;
    }
}
