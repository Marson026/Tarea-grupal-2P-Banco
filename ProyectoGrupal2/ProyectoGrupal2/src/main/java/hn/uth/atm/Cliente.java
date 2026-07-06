package hn.uth.atm;

import java.io.Serializable;
import java.math.BigDecimal;

public class Cliente implements Serializable {
    private String numeroCuenta;
    private BigDecimal saldo;
    private String pin;

    public Cliente() {
    }

    public Cliente(String numeroCuenta, BigDecimal saldo, String pin) {
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
        this.pin = pin;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
