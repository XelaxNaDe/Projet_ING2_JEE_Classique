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
        
USE GestionRH;

INSERT INTO Departement (nom_departement, id_chef_departement) VALUES 
('Informatique', NULL),
('Ressources Humaines', NULL),
('Marketing', NULL);

INSERT INTO Employee (fname, sname, gender, email, password, position, id_departement) VALUES 
('Alice', 'Dupont', 'F', 'admin@cytech.fr', '$2a$10$VpjQJ4QSnMqpuexpB3x2U.ZshFq0jZTjHjsxz/tLUgrArfeZ2acwK', 'Directrice Technique', 1),
('Bob', 'Martin', 'M', 'bob.martin@cytech.fr', '$2a$10$VpjQJ4QSnMqpuexpB3x2U.ZshFq0jZTjHjsxz/tLUgrArfeZ2acwK', 'Développeur Fullstack', 1),('Charlie', 'Durand', 'M', 'charlie.durand@cytech.fr', '$2a$10$Ew.1/x9.1/x9.1/x9.1/x9.1/x9.1/x9.1/x9.1/x9.1/x9.1/x9', 'DRH', 2),
('David', 'Lefebvre', 'M', 'david.lefebvre@cytech.fr', '$2a$10$VpjQJ4QSnMqpuexpB3x2U.ZshFq0jZTjHjsxz/tLUgrArfeZ2acwK', 'Chargé de Recrutement', 2),
('Eve', 'Moreau', 'F', 'eve.moreau@cytech.fr', '$2a$10$VpjQJ4QSnMqpuexpB3x2U.ZshFq0jZTjHjsxz/tLUgrArfeZ2acwK', 'Responsable Marketing', 3);

UPDATE Departement SET id_chef_departement = 1 WHERE id_departement = 1;
UPDATE Departement SET id_chef_departement = 3 WHERE id_departement = 2;
UPDATE Departement SET id_chef_departement = 5 WHERE id_departement = 3;

INSERT INTO Employee_Role (id, id_role) VALUES 
(1, 3),
(1, 1),
(2, 2),
(3, 1),
(3, 2),
(5, 1);

INSERT INTO Projet (nom_projet, date_debut, date_fin, id_chef_projet, etat) VALUES 
('Refonte Site Web', '2023-09-01', '2024-03-30', 2, 'En cours'), -- Chef: Bob
('Campagne Recrutement 2024', '2023-11-01', '2023-12-31', 3, 'En cours'); -- Chef: Charlie

INSERT INTO Employe_Projet (id, id_projet, role_dans_projet) VALUES 
(1, 1, 'Expert Backend'),
(2, 1, 'Lead Dev'),
(3, 2, 'Superviseur'),
(4, 2, 'Sourcing Candidats'),
(5, 1, 'Consultante UX/UI'); 

INSERT INTO Payroll (id, date, salary, netPay) VALUES 
(1, '2023-09-30', 4000, 4200.00),
(1, '2023-10-31', 4000, 3950.00),
(1, '2023-11-30', 4200, 4200.00),
(2, '2023-10-31', 3000, 3100.00),
(2, '2023-11-30', 3000, 2900.00),
(3, '2023-11-30', 3500, 3500.00),
(4, '2023-10-31', 2500, 2500.00),
(4, '2023-11-30', 2500, 2600.00),
(5, '2023-11-30', 3800, 3800.00);


INSERT INTO IntStringPayroll (id_payroll, amount, label, type_list) VALUES 
(1, 300, 'Prime Performance', 'Prime'),
(1, 100, 'Ticket Restaurant', 'Déduction'),

(2, 50, 'Retard', 'Déduction'),

(4, 200, 'Prime Projet', 'Prime'),
(4, 100, 'Mutuelle', 'Déduction'),

(5, 100, 'Absence', 'Déduction'),

(8, 100, 'Prime Cooptation', 'Prime');