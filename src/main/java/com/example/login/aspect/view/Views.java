package com.example.login.aspect.view;

/**
 * Classe que define diferentes níveis de visualização para serialização JSON.
 * Utilizada para controlar quais campos devem ser incluídos na resposta, dependendo do nível especificado.
 */
public class Views {

    public static class Basic {}

    public static class Regular extends Basic {}

    public static class Details extends Regular {}

    public static class Complete extends Details {}
}
