package model;

import java.io.Serializable;

public class Aerodrom implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sifra;
    private String naziv;
    private String grad;

    public Aerodrom(String sifra, String naziv, String grad) {
        this.sifra = sifra;
        this.naziv = naziv;
        this.grad = grad;
    }

    // Getteri i setteri
    public String getSifra() {
        return sifra;
    }

    public void setSifra(String sifra) {
        this.sifra = sifra;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getGrad() {
        return grad;
    }

    public void setGrad(String grad) {
        this.grad = grad;
    }

    @Override
    public String toString() {
        return sifra + " - " + naziv + " (" + grad + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Aerodrom other = (Aerodrom) obj;
        return sifra.equals(other.sifra);
    }

    @Override
    public int hashCode() {
        return sifra.hashCode();
    }
}