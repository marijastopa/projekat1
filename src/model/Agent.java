package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Agent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String naziv;
    private Map<String, AvioKompanija> avioKompanije; // Ključ je naziv avio kompanije
    private Map<String, Rezervacija> rezervacije; // Ključ je ID rezervacije
    private Map<LocalDate, Double> dnevniPrihodi;
    private double procenatProvizije; // Procenat koji agent uzima (npr. 0.1 za 10%)

    // Executor service za konkurentne RPC zahteve (ograničeno na 3)
    private transient ExecutorService executorService;

    public Agent(String naziv, double procenatProvizije) {
        this.naziv = naziv;
        this.avioKompanije = new ConcurrentHashMap<>();
        this.rezervacije = new ConcurrentHashMap<>();
        this.dnevniPrihodi = new ConcurrentHashMap<>();
        this.procenatProvizije = procenatProvizije;
        this.executorService = Executors.newFixedThreadPool(3);
    }

    // Metoda za dodavanje avio kompanije
    public void dodajAvioKompaniju(AvioKompanija avioKompanija) {
        avioKompanije.put(avioKompanija.getNaziv(), avioKompanija);
    }

    // Metoda za pronalaženje letova po kriterijumima od svih avio kompanija
    public List<Let> nadjiLetove(Aerodrom polazniAerodrom, Aerodrom dolazniAerodrom,
                                 LocalDate datum) {
        List<Let> rezultat = new ArrayList<>();

        for (AvioKompanija avioKompanija : avioKompanije.values()) {
            rezultat.addAll(avioKompanija.nadjiLetove(polazniAerodrom, dolazniAerodrom, datum));
        }

        // Sortiraj po trenutnoj ceni (najjeftiniji prvo)
        rezultat.sort((let1, let2) -> Double.compare(let1.getTrenutnaCena(), let2.getTrenutnaCena()));

        return rezultat;
    }

    // Metoda za rezervaciju leta
    public Rezervacija rezervisiLet(String sifraLeta, String sifraPovratnogLeta, int brojOsoba) {
        // Pronađi kom avio kompaniji pripada let
        Let odlazniLet = null;
        AvioKompanija odlaznaKompanija = null;

        for (AvioKompanija avioKompanija : avioKompanije.values()) {
            for (Let let : avioKompanija.getLetovi().values()) {
                if (let.getSifra().equals(sifraLeta)) {
                    odlazniLet = let;
                    odlaznaKompanija = avioKompanija;
                    break;
                }
            }
            if (odlazniLet != null) break;
        }

        if (odlazniLet == null || odlaznaKompanija == null) {
            return null;
        }

        Let povratniLet = null;
        AvioKompanija povratnaKompanija = null;

        if (sifraPovratnogLeta != null) {
            for (AvioKompanija avioKompanija : avioKompanije.values()) {
                for (Let let : avioKompanija.getLetovi().values()) {
                    if (let.getSifra().equals(sifraPovratnogLeta)) {
                        povratniLet = let;
                        povratnaKompanija = avioKompanija;
                        break;
                    }
                }
                if (povratniLet != null) break;
            }

            if (povratniLet == null && sifraPovratnogLeta != null) {
                return null;
            }
        }

        // Rezerviši let preko avio kompanije
        Rezervacija rezervacija = odlaznaKompanija.rezervisiLet(sifraLeta,
                povratniLet != null ? sifraPovratnogLeta : null,
                brojOsoba, true);

        if (rezervacija != null) {
            rezervacije.put(rezervacija.getId(), rezervacija);
        }

        return rezervacija;
    }

    // Metoda za konkurentnu rezervaciju leta
    public Future<Rezervacija> rezervisiLetAsinhronno(final String sifraLeta,
                                                      final String sifraPovratnogLeta,
                                                      final int brojOsoba) {
        return executorService.submit(() -> rezervisiLet(sifraLeta, sifraPovratnogLeta, brojOsoba));
    }

    // Metoda za plaćanje rezervacije
    public double platiRezervaciju(String idRezervacije) {
        Rezervacija rezervacija = rezervacije.get(idRezervacije);
        if (rezervacija == null) {
            return -1; // model.Rezervacija ne postoji
        }

        // Pronađi avio kompaniju za odlazni let
        AvioKompanija avioKompanija = null;
        for (AvioKompanija ak : avioKompanije.values()) {
            if (ak.getNaziv().equals(rezervacija.getOdlazniLet().getAvioKompanija())) {
                avioKompanija = ak;
                break;
            }
        }

        if (avioKompanija == null) {
            return -2; // Avio kompanija nije pronađena
        }

        // Plati rezervaciju kod avio kompanije
        double cenaBezProvizije = avioKompanija.platiRezervaciju(idRezervacije, true);

        if (cenaBezProvizije < 0) {
            return cenaBezProvizije; // Greška pri plaćanju
        }

        // Dodaj proviziju agenta
        double cena = cenaBezProvizije * (1 + procenatProvizije);

        // Ažuriraj dnevne prihode (samo provizija)
        LocalDate danas = LocalDate.now();
        double provizija = cenaBezProvizije * procenatProvizije;
        dnevniPrihodi.put(danas, dnevniPrihodi.getOrDefault(danas, 0.0) + provizija);

        return cena;
    }

    // Metoda za dobijanje dnevnog prihoda
    public double getDnevniPrihod(LocalDate datum) {
        return dnevniPrihodi.getOrDefault(datum, 0.0);
    }

    // Metoda za konkurentno plaćanje rezervacije
    public Future<Double> platiRezervacijuAsinhronno(final String idRezervacije) {
        return executorService.submit(() -> platiRezervaciju(idRezervacije));
    }

    // Getteri i setteri
    public String getNaziv() {
        return naziv;
    }

    public Map<String, AvioKompanija> getAvioKompanije() {
        return new HashMap<>(avioKompanije);
    }

    public double getProcenatProvizije() {
        return procenatProvizije;
    }

    // Metoda za serijalizaciju
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Reinicijalizacija transient polja
        this.executorService = Executors.newFixedThreadPool(3);
    }

    // Metoda za gašenje executora
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Override
    public String toString() {
        return naziv + " (Provizija: " + (procenatProvizije * 100) + "%)";
    }
}