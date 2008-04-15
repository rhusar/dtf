#
# JBoss, Home of Professional Open Source
# Copyright 2008, Red Hat Middleware LLC, and individual contributors
# as indicated by the @author tags.
# See the copyright.txt in the distribution for a
# full listing of individual contributors.
# This copyrighted material is made available to anyone wishing to use,
# modify, copy, or redistribute it subject to the terms and conditions
# of the GNU Lesser General Public License, v. 2.1.
# This program is distributed in the hope that it will be useful, but WITHOUT A
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
# You should have received a copy of the GNU Lesser General Public License,
# v.2.1 along with this distribution; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA  02110-1301, USA.
#
# (C) 2008,
# @author JBoss Inc.
#

Installing the DTF
==================

Instructions follow showing how to install the DTF on a laptop.
In this configuration, you can start the DTF services, one test node, and the DTFweb GUI.

This document includes the following sections:
0. Base software you will need
1. Setup MySQL for DTF use
2. Setup Tomcat for DTF use
3. Build the DTF from source and install
4. Start a DTF instance
5. Run an example test case against a sample product

0. Base software you will need
------------------------------
* MySQL
- You need access to a MySQL 5.0 (or later) server.
- The MySQL JDBC driver is provided in the DTF lib/ or you can you use a later version.
- The driver needs to be installed in tomcat, see below

* Tomcat
- You need access to a tomcat server. We use 6.0 but 5.5 may also work.
- copy the mysql drivers into tomcat. On 6.0 they go in lib/

* ant
- We use version 7.

* JDK. Minimum Java 1.5
- compile with the earliest jdk you want to execute tests on or you'll have .class file version problems.

1. Setup MySQL for DTF use
--------------------------

- you just need to create a database called dtf and an account for a user called dtfuser:
- create a database called dtf under control of MySQL

mysql -u root -p
> create database dtf

- create a (user,password) combination (dtfuser,dtfuser) with full
access privilidges for the database dtf, which hold when logging
on from any machine:

> grant select, insert, update, delete, create, drop
  on dtf.* to 'dtfuser'@'localhost' identified by 'dtfuser' ;

> grant select, insert, update, delete, create, drop
  on dtf.* to 'dtfuser'@'%' identified by 'dtfuser' ;

- test logon and access to the database by (dtfuser,dtfuser)

- once you start up the DTFweb GUI for the first time, DTFResultsLogger will check that the required
tables are created and if not, create them

2. Setup Tomcat for DTF use
---------------------------
- you need to several things to set up Tomcat:

(i) modify $CATALINA_HOME/conf/web.xml to turn on (uncomment) the default servlet invoker

    <servlet>
        <servlet-name>invoker</servlet-name>
        <servlet-class>
          org.apache.catalina.servlets.InvokerServlet
        </servlet-class>
        <init-param>
            <param-name>debug</param-name>
            <param-value>0</param-value>
        </init-param>
        <load-on-startup>2</load-on-startup>
    </servlet>

(ii) Setup libraries required by Tomcat when using the dtf.war web application:
- copy the MySQL Connector/J driver mysql-connector-java-5.1.16-bin.jar to $CATALINA_HOME/lib

- TODO copy the JavaMail library activation.jar to $CATALINA_HOME/common/lib ??
- TODO obsolete? NOTE: may also need xerces.jar, mail.jar, commons-logging-1.1.jar and log4j-1.2.8.jar

The DTFweb GUI makes JDBC connections to the MySQL database, and so Tomcat needs access to the
Connector/J driver. The DTFweb GUI also needs to use JavaMail to notify users that test runs have
completed.

(iii) The dtf.war web application includes the following Resource definition in META-INF/context.xml:

 <!-- the Tomcat 6 Context for the DTF web application -->
<Context privileged="true">

  <!-- set up JDBC access to the DTF database for the DTF web application -->
  <Resource name="jdbc/ResultsDB"
            auth="Container"
	    type="javax.sql.DataSource"
	    username="dtfuser"
	    password="dtfuser"
	    driverClassName="com.mysql.jdbc.Driver"
	    url="jdbc:mysql://localhost:3306/dtf"
	    maxActive="20"
	    maxIdle="6"
	    maxWait="10000"
	    testOnBorrow="true"
	    validationQuery="select 1"
	    timeBetweenEvictionRunsMillis="30000"
	    />
</Context>

You will need to adjust the url to point to your MySQL database.

 The dtf.war web application accesses the defined resource via its WEB-INF/web.xml descriptor.
 You may need to change the mail server config.

 <web-app>
    ....

    <env-entry>
      <description>
        String value of SMTP server used to e-mail results via JavaMail
      </description>
      <env-entry-name>dtf/smtp_server</env-entry-name>
      <env-entry-value>localhost</env-entry-value>
      <env-entry-type>java.lang.String</env-entry-type>
    </env-entry>

</web-app>

(iv) After installation (see below), the DTF can be accessed via http://localhost:8080/dtf (or similar,
depending on your configured Tomcat host and port)

For debugging, $CATALINA_HOME/logs/catalina.out is useful


3. Build the DTF from source and install
----------------------------------------

- run 'ant'. The dist dir will now contain a DTF binary distribution.

-  to install the DTF, open the dist/install.xml file and set the properties:
* install.dir,  where you want the DTF software to reside,
* webapps.dir, where you want the webapp deployed
and then run the 'full' target of the install script:

> cd dist; ant -f install.xml full

- the distribution is configured to run by default on a single host (called localhost), with the following settings:

* the DTFweb application installed at http://localhost:8080/dtf
* product definitions in $DTF_HOME/testenv/services/products
* product builds in $CATALINA_HOME/webapps/dtf/productbuilds
* product installers in $CATALINA_HOME/webapps/dtf/productinstallers
* product tests and selections in $CATALINA_HOME/webapps/dtf/producttests

- the installation as described above has no products, no installers, no tests, but should start cleanly in
that state from DTFweb, at which time you can define products and write your own tests. Section 5 shows
how to do this.


4. Start a DTF instance
------------------------

- a DTF instance consists of an assembly of running processes: MySQL, Tomcat, DTF services,
DTF coordinator, and one or more DTF test nodes.

You should be able to run the DTF by doing the following:

* start up MySQL and Tomcat
* set DTF_HOME to point to the directory where the DTF distribution has been unpacked

* start services
> cd $DTF_HOME
> . setup-dtf.sh
> cd testenv/services
> ./run_services.sh

* start coordinator
> cd $DTF_HOME
> . setup-dtf.sh
> cd testenv/coordinator
> ./run_coordinator.sh

* start a testnode
> cd $DTF_HOME
> . setup-dtf.sh
> cd testenv/testnode

edit nodeconfig.xml jvm-definitions section to reflect
the location(s) of the jvm(s) installed on your system.

> ./run_testnode.sh

* point your browser to http://localhost:8080/dtf

- you should see the DTFweb GUI appear
- check that the DTFweb setup is correct. Click on the menu item "setup".
The resulting page of input fields should look something like this:
DefaultNameService URI	      //localhost:1099/NameService
UploadDirectory		      /opt/jakarta-tomcat-6.0.16/dtf/producttests
UploadWebDirectory	      http://localhost:8080/dtf/producttests
Root URL		      http://localhost:8080/dtf
- you may receive Tomcat exceptions upon startup if the installation has not been
performed correctly. Check the Tomcat logs in $CATALINA_HOME/logs.


5. Run a test case against a product
------------------------------------
- as mentioned earlier, this running DTF instance has no products, test cases or test selections defined
yet
- to install a sample product, product installer, and sample test definition and selection, run the 'install-sample'
target of the install.xml file. In order to run this example, you will need to do a couple of things:
(i) add an OS (e.g. Linux) in the Setup menu
(ii) associate the TestProduct with the TestProductInstaller in the Deployment menu (using the fully qualified name
of the installer as http://localhost:8080/dtf/productinstallers/TestProductInstaller.xml)
(iii) You may also need to edit TestProduct.xml to set DTF_HOME to the correct value.
You should then be able to:
(i) deploy the product on a single test node from the Deployment menu
(ii) import the product definition, and run the sample test from the Management menu

