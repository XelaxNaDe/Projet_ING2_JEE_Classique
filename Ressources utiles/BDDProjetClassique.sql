CREATE DATABASE GestionRH;
USE GestionRH;

CREATE TABLE Role (
    id_role INT AUTO_INCREMENT PRIMARY KEY,
    nom_role VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    id_role INT,
    FOREIGN KEY (id_role) REFERENCES Role(id_role)
);


CREATE TABLE Departement (
    id_departement INT AUTO_INCREMENT PRIMARY KEY,
    nom_departement VARCHAR(100) NOT NULL,
    id_chef_departement INT NULL
);


CREATE TABLE Employe (
    id_employe INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    poste VARCHAR(100),
    grade VARCHAR(50),
    salaire_base DECIMAL(10,2),
    date_embauche DATE,
    id_departement INT,
    id_utilisateur INT,
    FOREIGN KEY (id_departement) REFERENCES Departement(id_departement)
        ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id_utilisateur)
        ON DELETE SET NULL ON UPDATE CASCADE
);

ALTER TABLE Departement
ADD CONSTRAINT fk_chef_departement
FOREIGN KEY (id_chef_departement) REFERENCES Employe(id_employe)
ON DELETE SET NULL;

CREATE TABLE Projet (
    id_projet INT AUTO_INCREMENT PRIMARY KEY,
    nom_projet VARCHAR(150) NOT NULL,
    date_debut DATE,
    date_fin DATE,
    id_chef_projet INT,
    FOREIGN KEY (id_chef_projet) REFERENCES Employe(id_employe),
    etat ENUM('En cours', 'Terminé', 'Annulé') DEFAULT 'En cours'
);


CREATE TABLE Employe_Projet (
    id_employe INT,
    id_projet INT,
    role_dans_projet VARCHAR(100),
    PRIMARY KEY (id_employe, id_projet),
    FOREIGN KEY (id_employe) REFERENCES Employe(id_employe)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_projet) REFERENCES Projet(id_projet)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE FicheDePaie (
    id_fiche INT AUTO_INCREMENT PRIMARY KEY,
    id_employe INT NOT NULL,
    mois VARCHAR(20) NOT NULL,
    annee INT NOT NULL,
    salaire_base DECIMAL(10,2),
    primes DECIMAL(10,2) DEFAULT 0,
    deductions DECIMAL(10,2) DEFAULT 0,
    net_a_payer DECIMAL(10,2),
    date_generation DATE,
    FOREIGN KEY (id_employe) REFERENCES Employe(id_employe)
        ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO Role (nom_role) VALUES
('Administrateur'),
('ChefDepartement'),
('ChefProjet'),
('Employe');

