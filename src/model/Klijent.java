package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Klijent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String ime;
    private List<Rezervacija> rezervacije;

    public Klijent(String id, String ime) {
        this.id = id;
        this.ime = ime;
        this.rezervacije = new CopyOnWriteArrayList<>();
    }

    // Metoda za dodavanje rezervacije
    public void dodajRezervaciju(Rezervacija rezervacija) {
        rezervacije.add(rezervacija);
    }

    // Metoda za dobijanje aktivnih rezervacija
    public List<Rezervacija> getAktivneRezervacije() {
        List<Rezervacija> aktivne = new ArrayList<>();

        for (Rezervacija rezervacija : rezervacije) {
            if (rezervacija.getStatus() == Rezervacija.Status.AKTIVNA && !rezervacija.jeIstekla()) {
                aktivne.add(rezervacija);
            }
        }

        return aktivne;
    }

    // Metoda za dobijanje istorije rezervacija
    public List<Rezervacija> getIstorijaRezervacija() {
        List<Rezervacija> istorija = new ArrayList<>();

        for (Rezervacija rezervacija : rezervacije) {
            if (rezervacija.getStatus() == Rezervacija.Status.PLACENA ||
                    rezervacija.getStatus() == Rezervacija.Status.ISTEKLA ||
                    rezervacija.jeIstekla()) {
                istorija.add(rezervacija);
            }
        }

        return istorija;
    }

    // Getteri i setteri
    public String getId() {
        return id;
    }

    public String getIme() {
        return ime;
    }

    public List<Rezervacija> getRezervacije() {
        return new ArrayList<>(rezervacije);
    }

    @Override
    public String toString() {
        return ime + " (ID: " + id + ")";
    }
}