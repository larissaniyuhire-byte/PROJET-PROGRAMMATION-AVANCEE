import java.io.*;
import java.util.*;

public class GestionEtudiants {

    // ============================================================
    // Classe interne représentant un étudiant
    // ============================================================
    static class Etudiant {
        String matricule;
        String nom;
        String prenom;
        double[] notes;
        double moyenne;
        int classement;
        String mention;

        public Etudiant(String matricule, String nom, String prenom, double[] notes) {
            this.matricule = matricule;
            this.nom       = nom;
            this.prenom    = prenom;
            this.notes     = notes;
            this.moyenne   = calculerMoyenne();
            this.mention   = attribuerMention();
        }

        // Calcul de la moyenne des notes
        private double calculerMoyenne() {
            double somme = 0;
            for (double note : notes) {
                somme += note;
            }
            return Math.round((somme / notes.length) * 100.0) / 100.0;
        }

        // Attribution de la mention selon la moyenne
        private String attribuerMention() {
            if (moyenne >= 16)       return "Très Bien";
            else if (moyenne >= 14)  return "Bien";
            else if (moyenne >= 12)  return "Assez Bien";
            else if (moyenne >= 10)  return "Passable";
            else                     return "Insuffisant";
        }
    }

    // ============================================================
    // Lecture du fichier CSV des étudiants
    // ============================================================
    static List<Etudiant> lireFichierCSV(String cheminFichier) throws IOException {
        List<Etudiant> etudiants = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(cheminFichier));
        String ligne;
        boolean premiereLigne = true;

        while ((ligne = reader.readLine()) != null) {
            // Ignorer la ligne d'en-tête
            if (premiereLigne) {
                premiereLigne = false;
                continue;
            }

            // Ignorer les lignes vides
            if (ligne.trim().isEmpty()) continue;

            String[] colonnes = ligne.split(",");

            String matricule = colonnes[0].trim();
            String nom       = colonnes[1].trim();
            String prenom    = colonnes[2].trim();

            // Lire toutes les notes (colonnes à partir de l'index 3)
            double[] notes = new double[colonnes.length - 3];
            for (int i = 3; i < colonnes.length; i++) {
                notes[i - 3] = Double.parseDouble(colonnes[i].trim());
            }

            etudiants.add(new Etudiant(matricule, nom, prenom, notes));
        }

        reader.close();
        return etudiants;
    }

    // ============================================================
    // Classement des étudiants par moyenne décroissante
    // ============================================================
    static void classerEtudiants(List<Etudiant> etudiants) {
        // Tri par moyenne décroissante
        etudiants.sort((e1, e2) -> Double.compare(e2.moyenne, e1.moyenne));

        // Attribution des rangs (ex-aequo reçoivent le même rang)
        int rang = 1;
        for (int i = 0; i < etudiants.size(); i++) {
            if (i > 0 && etudiants.get(i).moyenne == etudiants.get(i - 1).moyenne) {
                // Même rang pour les ex-aequo
                etudiants.get(i).classement = etudiants.get(i - 1).classement;
            } else {
                etudiants.get(i).classement = rang;
            }
            rang++;
        }
    }

    // ============================================================
    // Écriture des résultats dans un fichier CSV
    // ============================================================
    static void ecrireResultatsCSV(List<Etudiant> etudiants,
                                   String cheminFichier,
                                   String[] nomsMatières) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier));

        // Construction de l'en-tête dynamique
        StringBuilder entete = new StringBuilder();
        entete.append("Classement,Matricule,Nom,Prenom");
        for (String matiere : nomsMatières) {
            entete.append(",").append(matiere);
        }
        entete.append(",Moyenne,Mention");
        writer.write(entete.toString());
        writer.newLine();

        // Écriture des données de chaque étudiant
        for (Etudiant e : etudiants) {
            StringBuilder ligne = new StringBuilder();
            ligne.append(e.classement).append(",");
            ligne.append(e.matricule).append(",");
            ligne.append(e.nom).append(",");
            ligne.append(e.prenom);

            for (double note : e.notes) {
                ligne.append(",").append(note);
            }

            ligne.append(",").append(e.moyenne);
            ligne.append(",").append(e.mention);

            writer.write(ligne.toString());
            writer.newLine();
        }

        writer.close();
    }

    // ============================================================
    // Affichage du tableau récapitulatif dans la console
    // ============================================================
    static void afficherTableau(List<Etudiant> etudiants, String[] nomsMatières) {
        System.out.println("\n========== RÉSULTATS DES ÉTUDIANTS ==========");
        System.out.printf("%-5s %-10s %-20s %-15s", "Rang", "Matricule", "Nom Prénom", "Moyenne");
        for (String m : nomsMatières) {
            System.out.printf(" %-12s", m);
        }
        System.out.printf(" %-15s%n", "Mention");
        System.out.println("-".repeat(100));

        for (Etudiant e : etudiants) {
            System.out.printf("%-5d %-10s %-20s %-15.2f",
                e.classement,
                e.matricule,
                e.nom + " " + e.prenom,
                e.moyenne);
            for (double note : e.notes) {
                System.out.printf(" %-12.1f", note);
            }
            System.out.printf(" %-15s%n", e.mention);
        }

        System.out.println("-".repeat(100));

        // Statistiques générales
        double moyenneGenerale = etudiants.stream()
            .mapToDouble(e -> e.moyenne)
            .average()
            .orElse(0);

        long admis = etudiants.stream()
            .filter(e -> e.moyenne >= 10)
            .count();

        System.out.printf("%nMoyenne générale de la promotion : %.2f%n", moyenneGenerale);
        System.out.printf("Étudiants admis (moyenne ≥ 10)    : %d / %d%n", admis, etudiants.size());
        System.out.printf("Étudiants recalés                 : %d / %d%n",
            etudiants.size() - admis, etudiants.size());
        System.out.println("=============================================\n");
    }

    // ============================================================
    // Programme principal
    // ============================================================
    public static void main(String[] args) {

        String fichierEntree = "etudiants.csv";
        String fichierSortie = "resultats_classement.csv";

        try {
            // 1. Lecture du fichier d'entrée
            System.out.println("Lecture du fichier : " + fichierEntree);
            List<Etudiant> etudiants = lireFichierCSV(fichierEntree);
            System.out.println(etudiants.size() + " étudiant(s) chargé(s).\n");

            // 2. Récupération dynamique des noms de matières
            // (on relit la première ligne pour extraire les en-têtes)
            BufferedReader br = new BufferedReader(new FileReader(fichierEntree));
            String entete = br.readLine();
            br.close();
            String[] colonnes = entete.split(",");
            String[] nomsMatières = Arrays.copyOfRange(colonnes, 3, colonnes.length);

            // 3. Classement
            classerEtudiants(etudiants);

            // 4. Affichage console
            afficherTableau(etudiants, nomsMatières);

            // 5. Écriture du fichier de résultats
            ecrireResultatsCSV(etudiants, fichierSortie, nomsMatières);
            System.out.println("Résultats enregistrés dans : " + fichierSortie);

        } catch (FileNotFoundException e) {
            System.err.println("Erreur : fichier introuvable -> " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Erreur : note invalide dans le fichier CSV -> " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur de lecture/écriture -> " + e.getMessage());
        }
    }
}
