package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Rezervacija implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        AKTIVNA,
        ISTEKLA,
        PLACENA
    }

    private String id;
    private Let odlazniLet;
    private Let povratniLet; // Može biti null ako nema povratnog leta
    private int brojOsoba;
    private LocalDateTime datumRezervacije;
    private LocalDateTime rokPlacanja;
    private Status status;
    private double cenaOdlaznogLeta;
    private double cenaPovratnogLeta; // 0 ako nema povratnog leta

    public Rezervacija(Let odlazniLet, Let povratniLet, int brojOsoba) {
        this.id = UUID.randomUUID().toString();
        this.odlazniLet = odlazniLet;
        this.povratniLet = povratniLet;
        this.brojOsoba = brojOsoba;
        this.datumRezervacije = LocalDateTime.now();
        // Rok plaćanja je 24 sata od rezervacije
        this.rokPlacanja = datumRezervacije.plusHours(24);
        this.status = Status.AKTIVNA;
        // Čuvamo trenutne cene pri rezervaciji
        this.cenaOdlaznogLeta = odlazniLet.getTrenutnaCena();
        this.cenaPovratnogLeta = (povratniLet != null) ? povratniLet.getTrenutnaCena() : 0;
    }

    // Metoda za proveru da li je rezervacija istekla
    public boolean jeIstekla() {
        return LocalDateTime.now().isAfter(rokPlacanja);
    }

    // Metoda za izračunavanje trenutne ukupne cene
    public double izracunajTrenutnuCenu() {
        double trenutnaOdlaznaCena = odlazniLet.getTrenutnaCena() * brojOsoba;
        double trenutnaPovratnaCena = (povratniLet != null) ? povratniLet.getTrenutnaCena() * brojOsoba : 0;
        return trenutnaOdlaznaCena + trenutnaPovratnaCena;
    }

    // Metoda za izračunavanje cene pri rezervaciji
    public double izracunajRezervacionuCenu() {
        double rezervacionaOdlaznaCena = cenaOdlaznogLeta * brojOsoba;
        double rezervacionaPovratnaCena = cenaPovratnogLeta * brojOsoba;
        return rezervacionaOdlaznaCena + rezervacionaPovratnaCena;
    }

    // Getteri i setteri
    public String getId() {
        return id;
    }

    public Let getOdlazniLet() {
        return odlazniLet;
    }

    public Let getPovratniLet() {
        return povratniLet;
    }

    public int getBrojOsoba() {
        return brojOsoba;
    }

    public LocalDateTime getDatumRezervacije() {
        return datumRezervacije;
    }

    public LocalDateTime getRokPlacanja() {
        return rokPlacanja;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public double getCenaOdlaznogLeta() {
        return cenaOdlaznogLeta;
    }

    public double getCenaPovratnogLeta() {
        return cenaPovratnogLeta;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rezervacija ID: ").append(id).append("\n");
        sb.append("Odlazni let: ").append(odlazniLet).append("\n");
        if (povratniLet != null) {
            sb.append("Povratni let: ").append(povratniLet).append("\n");
        }
        sb.append("Broj osoba: ").append(brojOsoba).append("\n");
        sb.append("Datum rezervacije: ").append(datumRezervacije).append("\n");
        sb.append("Rok plaćanja: ").append(rokPlacanja).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Cena pri rezervaciji: ").append(izracunajRezervacionuCenu()).append("\n");
        sb.append("Trenutna cena: ").append(izracunajTrenutnuCenu());
        return sb.toString();
    }
}