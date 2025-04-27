package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AvioKompanija implements Serializable {
    private static final long serialVersionUID = 1L;

    private String naziv;
    private Map<String, Let> letovi; // Ključ je šifra leta
    private Map<String, Rezervacija> rezervacije; // Ključ je ID rezervacije
    private Map<LocalDate, Double> dnevniPrihodi;
    private double procenatPoputaZaAgente; // Popust koji daju agentima (npr. 0.05 za 5%)

    public AvioKompanija(String naziv, double procenatPoputaZaAgente) {
        this.naziv = naziv;
        this.letovi = new ConcurrentHashMap<>();
        this.rezervacije = new ConcurrentHashMap<>();
        this.dnevniPrihodi = new ConcurrentHashMap<>();
        this.procenatPoputaZaAgente = procenatPoputaZaAgente;
    }

    // Metoda za dodavanje leta
    public void dodajLet(Let let) {
        letovi.put(let.getSifra(), let);
    }

    // Metoda za pronalaženje letova po kriterijumima
    public List<Let> nadjiLetove(Aerodrom polazniAerodrom, Aerodrom dolazniAerodrom,
                                 LocalDate datum) {
        List<Let> rezultat = new ArrayList<>();

        for (Let let : letovi.values()) {
            if ((let.getPolazniAerodrom().equals(polazniAerodrom) ||
                    let.getPolazniAerodrom().getGrad().equalsIgnoreCase(polazniAerodrom.getGrad())) &&
                    (let.getDolazniAerodrom().equals(dolazniAerodrom) ||
                            let.getDolazniAerodrom().getGrad().equalsIgnoreCase(dolazniAerodrom.getGrad())) &&
                    let.getVremePolaska().toLocalDate().equals(datum) &&
                    let.getPreostaliBrojMesta() > 0) {

                rezultat.add(let);
            }
        }

        return rezultat;
    }

    // Metoda za rezervaciju leta
    public Rezervacija rezervisiLet(String sifraLeta, String sifraPovratnogLeta,
                                    int brojOsoba, boolean zaAgenta) {
        Let odlazniLet = letovi.get(sifraLeta);
        if (odlazniLet == null || !odlazniLet.rezervisiMesta(brojOsoba)) {
            return null; // Nema leta ili nema dovoljno mesta
        }

        Let povratniLet = null;
        if (sifraPovratnogLeta != null) {
            povratniLet = letovi.get(sifraPovratnogLeta);
            if (povratniLet == null || !povratniLet.rezervisiMesta(brojOsoba)) {
                // Ako ne možemo rezervisati povratni let, otkazujemo i odlazni
                odlazniLet.otkaziRezervaciju(brojOsoba);
                return null;
            }
        }

        Rezervacija rezervacija = new Rezervacija(odlazniLet, povratniLet, brojOsoba);
        rezervacije.put(rezervacija.getId(), rezervacija);

        return rezervacija;
    }

    // Metoda za plaćanje rezervacije
    public double platiRezervaciju(String idRezervacije, boolean zaAgenta) {
        Rezervacija rezervacija = rezervacije.get(idRezervacije);
        if (rezervacija == null) {
            return -1; // model.Rezervacija ne postoji
        }

        if (rezervacija.getStatus() == Rezervacija.Status.PLACENA) {
            return -2; // Već plaćena
        }

        if (rezervacija.jeIstekla()) {
            // Oslobodi mesta ako je istekla
            otkaziRezervaciju(idRezervacije);
            return -3; // Istekla
        }

        // Izračunaj trenutnu cenu
        double trenutnaCena = rezervacija.izracunajTrenutnuCenu();

        // Primeni popust za agente ako je potrebno
        if (zaAgenta) {
            trenutnaCena = trenutnaCena * (1 - procenatPoputaZaAgente);
        }

        // Ažuriraj status rezervacije
        rezervacija.setStatus(Rezervacija.Status.PLACENA);

        // Ažuriraj dnevne prihode
        LocalDate danas = LocalDate.now();
        dnevniPrihodi.put(danas, dnevniPrihodi.getOrDefault(danas, 0.0) + trenutnaCena);

        return trenutnaCena;
    }

    // Metoda za otkazivanje rezervacije
    public boolean otkaziRezervaciju(String idRezervacije) {
        Rezervacija rezervacija = rezervacije.get(idRezervacije);
        if (rezervacija == null || rezervacija.getStatus() == Rezervacija.Status.PLACENA) {
            return false;
        }

        // Oslobodi mesta
        rezervacija.getOdlazniLet().otkaziRezervaciju(rezervacija.getBrojOsoba());
        if (rezervacija.getPovratniLet() != null) {
            rezervacija.getPovratniLet().otkaziRezervaciju(rezervacija.getBrojOsoba());
        }

        // Ažuriraj status rezervacije
        rezervacija.setStatus(Rezervacija.Status.ISTEKLA);

        return true;
    }

    // Metoda za dobijanje trenutne cene za rezervaciju
    public double getTrenutnaCenaRezervacije(String idRezervacije, boolean zaAgenta) {
        Rezervacija rezervacija = rezervacije.get(idRezervacije);
        if (rezervacija == null) {
            return -1;
        }

        double trenutnaCena = rezervacija.izracunajTrenutnuCenu();

        // Primeni popust za agente ako je potrebno
        if (zaAgenta) {
            trenutnaCena = trenutnaCena * (1 - procenatPoputaZaAgente);
        }

        return trenutnaCena;
    }

    // Metoda za dobijanje dnevnog prihoda
    public double getDnevniPrihod(LocalDate datum) {
        return dnevniPrihodi.getOrDefault(datum, 0.0);
    }

    // Getteri i setteri
    public String getNaziv() {
        return naziv;
    }

    public Map<String, Let> getLetovi() {
        return new HashMap<>(letovi);
    }

    public double getProcenatPoputaZaAgente() {
        return procenatPoputaZaAgente;
    }

    @Override
    public String toString() {
        return naziv;
    }
}