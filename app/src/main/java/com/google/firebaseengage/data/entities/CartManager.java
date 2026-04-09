package com.google.firebaseengage.data.entities;

public class CartManager {
    private static Cart instance;

    public static synchronized Cart getInstance() {
        if (instance == null) {
            instance = new Cart();
        }
        return instance;
    }
}
