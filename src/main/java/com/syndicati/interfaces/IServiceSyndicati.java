package com.syndicati.interfaces;

import java.sql.SQLDataException;
import java.util.List;

public interface IServiceSyndicati <T>{

    void Ajouter(T t) throws SQLDataException;
    void Modifier(T t) throws SQLDataException;
    void Supprimer (T t) throws SQLDataException;
    List<T> Recuperer() throws SQLDataException;
}
