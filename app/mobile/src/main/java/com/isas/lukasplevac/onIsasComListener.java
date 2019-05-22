package com.isas.lukasplevac;

public interface onIsasComListener<T> {
    public void onSuccess(T object);
    public void onFailure(String message);
}
