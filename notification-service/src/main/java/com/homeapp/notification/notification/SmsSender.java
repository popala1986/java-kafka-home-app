package com.homeapp.notification.notification;

// "Coś, co umie wysłać SMS-a". Dziś jest tylko wersja testowa, a w Kroku 9c dojdzie SMSAPI.pl.
// Reszta kodu zna tylko ten interfejs, więc podmiana niczego innego nie zmieni.
public interface SmsSender {

    void send(String phoneNumber, String text);
}
