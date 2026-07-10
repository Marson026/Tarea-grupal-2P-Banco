package hn.uth.atm;

import java.io.Serializable;
import java.math.BigDecimal;

public class Cliente implements Serializable {
    private String numeroCuenta;
    private String nombreCliente;
    private BigDecimal saldo;
    private String pin;

    public Cliente() {
    }

    public Cliente(String numeroCuenta, BigDecimal saldo, String pin) {
        this(numeroCuenta, "Cliente sin nombre", saldo, pin);
    }

    public Cliente(String numeroCuenta, String nombreCliente, BigDecimal saldo, String pin) {
        this.numeroCuenta = numeroCuenta;
        this.nombreCliente = nombreCliente;
        this.saldo = saldo;
        this.pin = pin;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
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
