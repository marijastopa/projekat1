package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Let implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sifra;
    private Aerodrom polazniAerodrom;
    private Aerodrom dolazniAerodrom;
    private LocalDateTime vremePolaska;
    private String avioKompanija;
    private int ukupanBrojMesta;
    private AtomicInteger preostaliBrojMesta;
    private double pocetnaCena;
    private double trenutnaCena;
    private double maksimalnaCena;
    private int mestaPoCenovnomPragu; // Na koliko mesta se povećava cena
    private double povecanjeCene; // Koliko se povećava cena po pragu

    // Lock za sinhronizovano čitanje/pisanje cene i preostalih mesta
    private transient ReadWriteLock lock = new ReentrantReadWriteLock();

    public Let(String sifra, Aerodrom polazniAerodrom, Aerodrom dolazniAerodrom,
               LocalDateTime vremePolaska, String avioKompanija, int ukupanBrojMesta,
               double pocetnaCena, double maksimalnaCena, int mestaPoCenovnomPragu,
               double povecanjeCene) {
        this.sifra = sifra;
        this.polazniAerodrom = polazniAerodrom;
        this.dolazniAerodrom = dolazniAerodrom;
        this.vremePolaska = vremePolaska;
        this.avioKompanija = avioKompanija;
        this.ukupanBrojMesta = ukupanBrojMesta;
        this.preostaliBrojMesta = new AtomicInteger(ukupanBrojMesta);
        this.pocetnaCena = pocetnaCena;
        this.trenutnaCena = pocetnaCena;
        this.maksimalnaCena = maksimalnaCena;
        this.mestaPoCenovnomPragu = mestaPoCenovnomPragu;
        this.povecanjeCene = povecanjeCene;
    }

    // Metoda za rezervaciju mesta
    public boolean rezervisiMesta(int brojMesta) {
        lock.writeLock().lock();
        try {
            if (preostaliBrojMesta.get() >= brojMesta) {
                // Izračunaj novi broj mesta i ažuriraj cenu
                int noviBrojMesta = preostaliBrojMesta.addAndGet(-brojMesta);
                azurirajCenu(noviBrojMesta);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Metoda za otkazivanje rezervacije
    public void otkaziRezervaciju(int brojMesta) {
        lock.writeLock().lock();
        try {
            int noviBrojMesta = preostaliBrojMesta.addAndGet(brojMesta);
            // Ne smemo preći ukupan broj mesta
            if (noviBrojMesta > ukupanBrojMesta) {
                preostaliBrojMesta.set(ukupanBrojMesta);
                noviBrojMesta = ukupanBrojMesta;
            }
            azurirajCenu(noviBrojMesta);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Metoda za ažuriranje cene na osnovu broja preostalih mesta
    private void azurirajCenu(int preostaliBrojMesta) {
        // Izračunaj koliko je pragova prošlo
        int zauzetaMesta = ukupanBrojMesta - preostaliBrojMesta;
        int brojPragova = zauzetaMesta / mestaPoCenovnomPragu;

        // Izračunaj novu cenu
        double novaCena = pocetnaCena + (brojPragova * povecanjeCene);

        // Ograniči na maksimalnu cenu
        if (novaCena > maksimalnaCena) {
            novaCena = maksimalnaCena;
        }

        trenutnaCena = novaCena;
    }

    // Getteri i setteri
    public String getSifra() {
        return sifra;
    }

    public Aerodrom getPolazniAerodrom() {
        return polazniAerodrom;
    }

    public Aerodrom getDolazniAerodrom() {
        return dolazniAerodrom;
    }

    public LocalDateTime getVremePolaska() {
        return vremePolaska;
    }

    public String getAvioKompanija() {
        return avioKompanija;
    }

    public int getUkupanBrojMesta() {
        return ukupanBrojMesta;
    }

    public int getPreostaliBrojMesta() {
        lock.readLock().lock();
        try {
            return preostaliBrojMesta.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getTrenutnaCena() {
        lock.readLock().lock();
        try {
            return trenutnaCena;
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getPocetnaCena() {
        return pocetnaCena;
    }

    public double getMaksimalnaCena() {
        return maksimalnaCena;
    }

    @Override
    public String toString() {
        return sifra + ": " + polazniAerodrom.getSifra() + " -> " +
                dolazniAerodrom.getSifra() + ", " + avioKompanija + ", " +
                vremePolaska + ", Cena: " + trenutnaCena +
                " (" + preostaliBrojMesta.get() + "/" + ukupanBrojMesta + " mesta)";
    }

    // Metoda za serijalizaciju
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Reinicijalizacija transient polja
        lock = new ReentrantReadWriteLock();
    }
}