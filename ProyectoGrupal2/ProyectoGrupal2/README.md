# ProyectoGrupal2 - Cajero Automatico JSF

Aplicativo JavaServer Faces que simula la logica basica de un cajero automatico con estilo inspirado en Banpais: menu principal, depositos, retiros, consulta de saldo e historial de transacciones.

## Requisitos

- Java 17 o superior.
- Maven.
- Servidor compatible con Jakarta Faces 4, por ejemplo Apache Tomcat 10.1.

## Archivos CSV

Los clientes de prueba estan en:

```text
src/main/webapp/WEB-INF/data/clientes.csv
```

Estructura:

```text
numeroCuenta,saldo,pin
1002003001,12800.75,1234
```

El historial de operaciones esta en:

```text
src/main/webapp/WEB-INF/data/historial.csv
```

Estructura:

```text
fecha,tipo,numeroCuenta,monto,saldoFinal
2026-07-05 19:35:46,Deposito,1002003001,300.00,12800.75

## Clientes de prueba

- Cuenta `1002003001`, PIN `1234`
- Cuenta `1002003002`, PIN `4321`
- Cuenta `1002003003`, PIN `2468`
- Cuenta `1002003004`, PIN `1357`
- Cuenta `1002003005`, PIN `9876`

## Paginas

Las paginas que se abren desde el navegador son:

```text
index.xhtml
deposito.xhtml
retiro.xhtml
saldo.xhtml
```

`src/main/webapp/WEB-INF/templates/layout.xhtml` no se abre directamente. Esta dentro de `WEB-INF` porque es una plantilla compartida usada por las paginas publicas.

## Estructura principal

- `src/main/java/hn/uth/atm/CajeroBean.java`: logica de negocio, validaciones, carga de clientes y escritura del historial.
- `src/main/webapp/index.xhtml`: menu principal.
- `src/main/webapp/deposito.xhtml`: pantalla de depositos.
- `src/main/webapp/retiro.xhtml`: pantalla de retiros.
- `src/main/webapp/saldo.xhtml`: consulta de saldo e historial.
- `src/main/webapp/WEB-INF/templates/layout.xhtml`: plantilla base compartida.
- `src/main/webapp/resources/css/styles.css`: estilos del cajero.
