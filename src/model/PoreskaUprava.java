package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PoreskaUprava implements Serializable {
    private static final long serialVersionUID = 1L;

    // Mapa za čuvanje prijavljenih prihoda po kompaniji i datumu
    // Ključ je naziv kompanije (avio kompanija ili agent), a vrednost je mapa datuma i prihoda
    private Map<String, Map<LocalDate, Double>> prijavljeniPrihodi;

    public PoreskaUprava() {
        this.prijavljeniPrihodi = new ConcurrentHashMap<>();
    }

    // Metoda za prijavljivanje dnevnog prihoda
    public synchronized boolean prijaviDnevniPrihod(String nazivKompanije, LocalDate datum, double prihod) {
        if (!prijavljeniPrihodi.containsKey(nazivKompanije)) {
            prijavljeniPrihodi.put(nazivKompanije, new ConcurrentHashMap<>());
        }

        Map<LocalDate, Double> prihodiKompanije = prijavljeniPrihodi.get(nazivKompanije);
        prihodiKompanije.put(datum, prihod);

        System.out.println("Poreska uprava - Prijavljen prihod: " + nazivKompanije +
                " za datum " + datum + ": " + prihod);

        return true;
    }

    // Metoda za dobijanje prijavljenih prihoda kompanije za određeni datum
    public double getPrijavljeniPrihod(String nazivKompanije, LocalDate datum) {
        if (!prijavljeniPrihodi.containsKey(nazivKompanije)) {
            return 0;
        }

        Map<LocalDate, Double> prihodiKompanije = prijavljeniPrihodi.get(nazivKompanije);
        return prihodiKompanije.getOrDefault(datum, 0.0);
    }

    // Metoda za dobijanje ukupnih prijavljenih prihoda za određeni datum
    public double getUkupniPrijavljeniPrihod(LocalDate datum) {
        double ukupno = 0;

        for (Map<LocalDate, Double> prihodiKompanije : prijavljeniPrihodi.values()) {
            ukupno += prihodiKompanije.getOrDefault(datum, 0.0);
        }

        return ukupno;
    }

    // Metoda za dobijanje svih prijavljenih prihoda
    public Map<String, Map<LocalDate, Double>> getSviPrijavljeniPrihodi() {
        // Vrati kopiju mape
        Map<String, Map<LocalDate, Double>> kopija = new HashMap<>();

        for (Map.Entry<String, Map<LocalDate, Double>> entry : prijavljeniPrihodi.entrySet()) {
            kopija.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }

        return kopija;
    }
}