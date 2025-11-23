DROP DATABASE IF EXISTS GestionRH;
CREATE DATABASE GestionRH;
USE GestionRH;

CREATE TABLE Role ( 
    id_role INT AUTO_INCREMENT PRIMARY KEY,
    nom_role VARCHAR(50) NOT NULL UNIQUE 
);

INSERT INTO Role (nom_role) VALUES 
('HEADDEPARTEMENT'),
('PROJECTMANAGER'),
('ADMINISTRATOR');

CREATE TABLE Departement (
    id_departement INT AUTO_INCREMENT PRIMARY KEY,
    nom_departement VARCHAR(100) NOT NULL,
    id_chef_departement INT NULL 
);

CREATE TABLE Employee (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fname VARCHAR(100) NOT NULL,
    sname VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    position VARCHAR(100),
    grade VARCHAR(50),
    id_departement INT,
    FOREIGN KEY (id_departement)
        REFERENCES Departement(id_departement)
        ON DELETE SET NULL ON UPDATE CASCADE 
);

ALTER TABLE Departement
ADD CONSTRAINT fk_chef_departement
FOREIGN KEY (id_chef_departement)
REFERENCES Employee(id) ON DELETE SET NULL;

CREATE TABLE Employee_Role (
    id INT,
    id_role INT,
    PRIMARY KEY (id, id_role),
    FOREIGN KEY (id)
        REFERENCES Employee(id)
        ON DELETE CASCADE,
    FOREIGN KEY (id_role)
        REFERENCES Role(id_role)
        ON DELETE CASCADE 
);

CREATE TABLE Projet (
    id_projet INT AUTO_INCREMENT PRIMARY KEY,
    nom_projet VARCHAR(150) NOT NULL,
    date_debut DATE,
    date_fin DATE,
    id_chef_projet INT,
    FOREIGN KEY (id_chef_projet)
        REFERENCES Employee(id),
    etat ENUM(
            'En cours',
            'Terminé',
            'Annulé')
        DEFAULT 'En cours' 
);

CREATE TABLE Employe_Projet (
    id INT,
    id_projet INT,
    role_dans_projet VARCHAR(100),
    PRIMARY KEY (id, id_projet),
    FOREIGN KEY (id)
        REFERENCES Employee(id)
        ON DELETE CASCADE,
    FOREIGN KEY (id_projet)
        REFERENCES Projet(id_projet)
        ON DELETE CASCADE 
);

CREATE TABLE Payroll (
    id_payroll INT AUTO_INCREMENT PRIMARY KEY,
    id INT NOT NULL,
    date DATE NOT NULL,
    salary INT NOT NULL,
    netPay DOUBLE NOT NULL,
    FOREIGN KEY (id)
        REFERENCES Employee(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE 
);

CREATE TABLE IntStringPayroll (
    id_line INT AUTO_INCREMENT PRIMARY KEY,
    id_payroll INT NOT NULL,
    amount INT NOT NULL,
    label VARCHAR(255),
    type_list ENUM(
            'Prime',
            'Déduction')
        DEFAULT 'Prime',
    FOREIGN KEY (id_payroll)
        REFERENCES Payroll(id_payroll)
        ON DELETE CASCADE 
);
        
INSERT INTO Departement (nom_departement) VALUES 
('IT & Développement'), 
('Ressources Humaines');

INSERT INTO Employee (fname, sname, gender, email, password, position, grade, id_departement)
VALUES ('Jean', 'Admin', 'M', 'admin@cytech.fr', 'admin123', 'Directeur Technique', 'A1', 1);

INSERT INTO Employee (fname, sname, gender, email, password, position, grade, id_departement)
VALUES ('Sarah', 'Connor', 'F', 'sarah@cytech.fr', 'sarah123', 'Lead Developer', 'B1', 1);

INSERT INTO Employee (fname, sname, gender, email, password, position, grade, id_departement)
VALUES ('Mike', 'Ross', 'M', 'mike@cytech.fr', 'mike123', 'Project Lead', 'B2', 1);

INSERT INTO Employee (fname, sname, gender, email, password, position, grade, id_departement)
VALUES ('Pierre', 'Lambda', 'M', 'pierre@cytech.fr', 'pierre123', 'Développeur Junior', 'C1', 1);

INSERT INTO Employee_Role (id, id_role)
SELECT e.id, r.id_role 
FROM Employee e, Role r 
WHERE e.email = 'admin@cytech.fr' AND r.nom_role = 'ADMINISTRATOR';

INSERT INTO Employee_Role (id, id_role)
SELECT e.id, r.id_role 
FROM Employee e, Role r 
WHERE e.email = 'sarah@cytech.fr' AND r.nom_role = 'HEADDEPARTEMENT';

INSERT INTO Employee_Role (id, id_role)
SELECT e.id, r.id_role 
FROM Employee e, Role r 
WHERE e.email = 'mike@cytech.fr' AND r.nom_role = 'PROJECTMANAGER';

UPDATE Departement 
SET id_chef_departement = (SELECT id FROM Employee WHERE email = 'sarah@cytech.fr')
WHERE nom_departement = 'IT & Développement';

INSERT INTO Projet (nom_projet, date_debut, date_fin, id_chef_projet, etat) VALUES
('Refonte Intranet', '2024-01-01', '2024-06-30', (SELECT id FROM Employee WHERE email = 'mike@cytech.fr'), 'En cours'),
('Migration Cloud', '2024-03-15', '2024-09-15', (SELECT id FROM Employee WHERE email = 'sarah@cytech.fr'), 'En cours');

SET @id_pierre = (SELECT id FROM Employee WHERE email = 'pierre@cytech.fr');

INSERT INTO Payroll (id, date, salary, netPay) 
VALUES (@id_pierre, '2024-01-31', 2000, 2150); 

INSERT INTO Payroll (id, date, salary, netPay) 
VALUES (@id_pierre, '2024-02-29', 2000, 1950);

SET @id_payroll_jan = (SELECT id_payroll FROM Payroll WHERE id = @id_pierre AND date = '2024-01-31');
SET @id_payroll_feb = (SELECT id_payroll FROM Payroll WHERE id = @id_pierre AND date = '2024-02-29');

INSERT INTO IntStringPayroll (id_payroll, amount, label, type_list) VALUES
(@id_payroll_jan, 200, 'Prime Performance', 'PRIME'),
(@id_payroll_jan, 50, 'Tickets Restaurant', 'DEDUCTION');

INSERT INTO IntStringPayroll (id_payroll, amount, label, type_list) VALUES
(@id_payroll_feb, 50, 'Retard injustifié', 'DEDUCTION');

SELECT * FROM Employee;
