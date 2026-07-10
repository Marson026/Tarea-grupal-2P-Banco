package hn.uth.atm;

import java.io.Serializable;

public class Favorito implements Serializable {
    private String cuentaPropietario;
    private String cuentaFavorita;
    private String nombreFavorito;

    public Favorito() {
    }

    public Favorito(String cuentaPropietario, String cuentaFavorita, String nombreFavorito) {
        this.cuentaPropietario = cuentaPropietario;
        this.cuentaFavorita = cuentaFavorita;
        this.nombreFavorito = nombreFavorito;
    }

    public String getCuentaPropietario() {
        return cuentaPropietario;
    }

    public void setCuentaPropietario(String cuentaPropietario) {
        this.cuentaPropietario = cuentaPropietario;
    }

    public String getCuentaFavorita() {
        return cuentaFavorita;
    }

    public void setCuentaFavorita(String cuentaFavorita) {
        this.cuentaFavorita = cuentaFavorita;
    }

    public String getNombreFavorito() {
        return nombreFavorito;
    }

    public void setNombreFavorito(String nombreFavorito) {
        this.nombreFavorito = nombreFavorito;
    }

    public String getCuentaEnmascarada() {
        if (cuentaFavorita == null || cuentaFavorita.length() < 4) {
            return "****";
        }
        return "******" + cuentaFavorita.substring(cuentaFavorita.length() - 4);
    }
}
