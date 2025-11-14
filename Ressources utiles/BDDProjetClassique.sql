DROP DATABASE IF EXISTS GestionRH;
CREATE DATABASE GestionRH;
USE GestionRH;

CREATE TABLE Role ( 
	id_role INT AUTO_INCREMENT PRIMARY KEY,
    nom_role VARCHAR(50) NOT NULL UNIQUE );

INSERT INTO Role (nom_role) VALUES (
	'HEADDEPARTEMENT'),
    ('PROJECTMANAGER'),
    ('ADMINISTRATOR');

CREATE TABLE Departement (
	id_departement INT AUTO_INCREMENT PRIMARY KEY,
    nom_departement VARCHAR(100) NOT NULL,
    id_chef_departement INT NULL );

CREATE TABLE Employee (
matricule INT AUTO_INCREMENT PRIMARY KEY,
prenom VARCHAR(100) NOT NULL,
nom VARCHAR(100) NOT NULL,
sexe VARCHAR(10),
email VARCHAR(100) NOT NULL UNIQUE,
mdp VARCHAR(255) NOT NULL,
poste VARCHAR(100),
grade VARCHAR(50),
id_departement INT,
FOREIGN KEY (id_departement)
	REFERENCES Departement(id_departement)
	ON DELETE SET NULL ON UPDATE CASCADE );

ALTER TABLE Departement
ADD CONSTRAINT fk_chef_departement
FOREIGN KEY (id_chef_departement)
REFERENCES Employee(matricule) ON DELETE SET NULL;

CREATE TABLE Employee_Role (
	matricule INT,
    id_role INT,
    PRIMARY KEY (matricule, id_role),
    FOREIGN KEY (matricule)
		REFERENCES Employee(matricule)
        ON DELETE CASCADE,
    FOREIGN KEY (id_role)
		REFERENCES Role(id_role)
        ON DELETE CASCADE );

CREATE TABLE Projet (
	id_projet INT AUTO_INCREMENT PRIMARY KEY,
    nom_projet VARCHAR(150) NOT NULL,
    date_debut DATE,
    date_fin DATE,
    id_chef_projet INT,
    FOREIGN KEY (id_chef_projet)
		REFERENCES Employee(matricule),
		etat ENUM(
			'En cours',
            'Terminé',
            'Annulé')
        DEFAULT 'En cours' );

CREATE TABLE Employe_Projet (
	matricule INT,
	id_projet INT,
	role_dans_projet VARCHAR(100),
	PRIMARY KEY (matricule, id_projet),
	FOREIGN KEY (matricule)
		REFERENCES Employee(matricule)
		ON DELETE CASCADE,
	FOREIGN KEY (id_projet)
		REFERENCES Projet(id_projet)
		ON DELETE CASCADE );

CREATE TABLE Payroll (
	id_payroll INT AUTO_INCREMENT PRIMARY KEY,
    matricule INT NOT NULL,
    date DATE NOT NULL,
    salary INT NOT NULL,
    netPay DOUBLE NOT NULL,
    FOREIGN KEY (matricule)
		REFERENCES Employee(matricule)
        ON DELETE CASCADE
        ON UPDATE CASCADE );

CREATE TABLE IntStringPayroll (
	id_line INT AUTO_INCREMENT PRIMARY KEY,
    id_payroll INT NOT NULL,
    amount INT NOT NULL,
    label VARCHAR(255),
    type_list VARCHAR(20) NOT NULL,
    FOREIGN KEY (id_payroll)
		REFERENCES Payroll(id_payroll)
		ON DELETE CASCADE );
        

-- ============================================================
-- 1. CREATION DES DEPARTEMENTS (Sans chef pour l'instant)
-- ============================================================
INSERT INTO Departement (nom_departement) VALUES 
('IT & Développement'), 
('Ressources Humaines');

-- ============================================================
-- 2. CREATION DES EMPLOYES
-- ============================================================

-- 1. L'Administrateur
INSERT INTO Employee (prenom, nom, sexe, email, mdp, poste, grade, id_departement)
VALUES ('Jean', 'Admin', 'M', 'admin@cytech.fr', 'admin123', 'Directeur Technique', 'A1', 1);

-- 2. Le Chef de Département (HEADDEPARTEMENT)
INSERT INTO Employee (prenom, nom, sexe, email, mdp, poste, grade, id_departement)
VALUES ('Sarah', 'Connor', 'F', 'sarah@cytech.fr', 'sarah123', 'Lead Developer', 'B1', 1);

-- 3. Le Chef de Projet (PROJECTMANAGER)
INSERT INTO Employee (prenom, nom, sexe, email, mdp, poste, grade, id_departement)
VALUES ('Mike', 'Ross', 'M', 'mike@cytech.fr', 'mike123', 'Project Lead', 'B2', 1);

-- 4. L'Employé Lambda (Pas de rôle spécifique assigné, sert pour les payrolls)
INSERT INTO Employee (prenom, nom, sexe, email, mdp, poste, grade, id_departement)
VALUES ('Pierre', 'Lambda', 'M', 'pierre@cytech.fr', 'pierre123', 'Développeur Junior', 'C1', 1);

-- ============================================================
-- 3. ASSIGNATION DES ROLES (Table Employee_Role)
-- ============================================================

-- Assignation ADMIN
INSERT INTO Employee_Role (matricule, id_role)
SELECT e.matricule, r.id_role 
FROM Employee e, Role r 
WHERE e.email = 'admin@cytech.fr' AND r.nom_role = 'ADMINISTRATOR';

-- Assignation HEADDEPARTEMENT
INSERT INTO Employee_Role (matricule, id_role)
SELECT e.matricule, r.id_role 
FROM Employee e, Role r 
WHERE e.email = 'sarah@cytech.fr' AND r.nom_role = 'HEADDEPARTEMENT';

-- Assignation PROJECTMANAGER
INSERT INTO Employee_Role (matricule, id_role)
SELECT e.matricule, r.id_role 
FROM Employee e, Role r 
WHERE e.email = 'mike@cytech.fr' AND r.nom_role = 'PROJECTMANAGER';

-- NOTE : Pas d'insertion pour 'Pierre Lambda', il n'a donc aucun rôle (null/empty set).

-- ============================================================
-- 4. MISE A JOUR DU CHEF DE DEPARTEMENT
-- ============================================================
UPDATE Departement 
SET id_chef_departement = (SELECT matricule FROM Employee WHERE email = 'sarah@cytech.fr')
WHERE nom_departement = 'IT & Développement';

-- ============================================================
-- 5. CREATION DES PROJETS
-- ============================================================
INSERT INTO Projet (nom_projet, date_debut, date_fin, id_chef_projet, etat) VALUES
('Refonte Intranet', '2024-01-01', '2024-06-30', (SELECT matricule FROM Employee WHERE email = 'mike@cytech.fr'), 'En cours'),
('Migration Cloud', '2024-03-15', '2024-09-15', (SELECT matricule FROM Employee WHERE email = 'sarah@cytech.fr'), 'En cours');

-- ============================================================
-- 6. FICHES DE PAIE (PAYROLL) - Pour Pierre Lambda uniquement
-- ============================================================

-- Variables pour récupérer l'ID de Pierre
SET @id_pierre = (SELECT matricule FROM Employee WHERE email = 'pierre@cytech.fr');

-- Fiche de Janvier (Salaire + Prime)
INSERT INTO Payroll (matricule, date, salary, netPay) 
VALUES (@id_pierre, '2024-01-31', 2000, 2150); -- 2000 + 200 (Prime) - 50 (Ticket Resto)

-- Fiche de Février (Salaire standard - Retard)
INSERT INTO Payroll (matricule, date, salary, netPay) 
VALUES (@id_pierre, '2024-02-29', 2000, 1950); -- 2000 - 50 (Retard)

-- ============================================================
-- 7. LIGNES DE PAIE (IntStringPayroll)
-- ============================================================

-- Récupération des IDs des fiches qu'on vient de créer
SET @id_payroll_jan = (SELECT id_payroll FROM Payroll WHERE matricule = @id_pierre AND date = '2024-01-31');
SET @id_payroll_feb = (SELECT id_payroll FROM Payroll WHERE matricule = @id_pierre AND date = '2024-02-29');

-- Détails Janvier
INSERT INTO IntStringPayroll (id_payroll, amount, label, type_list) VALUES
(@id_payroll_jan, 200, 'Prime Performance', 'BONUS'),
(@id_payroll_jan, 50, 'Tickets Restaurant', 'DEDUCTION');

-- Détails Février
INSERT INTO IntStringPayroll (id_payroll, amount, label, type_list) VALUES
(@id_payroll_feb, 50, 'Retard injustifié', 'DEDUCTION');