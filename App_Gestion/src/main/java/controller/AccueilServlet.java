package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;

import java.io.IOException;

@WebServlet(name = "AccueilServlet", urlPatterns = "/accueil")
public class AccueilServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        // 1. Vérifier si l'utilisateur est connecté
        if (user == null) {
            // Si non, rediriger vers la page de connexion
            resp.sendRedirect(req.getContextPath() + "connexion.jsp");
        } else {
            // 2. Si oui, afficher la page d'accueil
            // On transfère la requête à la page JSP pour qu'elle s'affiche
            req.getRequestDispatcher("accueil.jsp").forward(req, resp);
        }
    }
}