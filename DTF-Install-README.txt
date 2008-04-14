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

Instructions follow showing how to install the DTF from Branch_DTF_3_9 on a laptop.
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
- I am using  Ver 14.12 Distrib 5.0.27, installed in default Fedora Core 6 location
- I am also using mysql-connector-java-5.0.5-bin.jar as the MySQL Connector/J JDBC driver,
which needs to be made available to both the DTF Runtime (in $DTF_HOME/lib/ext) and Tomcat
(in $CATALINA_HOME/common/lib and $CATALINA_HOME/webapps/dtf/WEB-INF/lib)

* Tomcat
- I am using version 5.5.20, installed in /opt/apache-tomcat-5.5.20
- old instructions for use of Tomcat 4 with DTF appear in Appendix

* ant
- I am using version 1.6.5, installed in /opt/apache-ant-1.6.5


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
(i) modify $CATALINA_HOME/conf/tomcat-users.xml to add enable the managet webapp
- add a user of your choice with manager role to tomcat-users:
<tomcat-users>
  <role rolename="manager"/>
  <user usename="someuser" password="somepassword" roles="manager"/>
</tomcat-users>
The manager webapp will be used to deploy the dtf.war web application which defines the DTF GUI.

(ii) modify $CATALINA_HOME/conf/web.xml to turn on the default servlet invoker
- the two sections which need to be uncommented are:
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

    ...

    <!-- The mapping for the invoker servlet -->
    <servlet-mapping>
        <servlet-name>invoker</servlet-name>
        <url-pattern>/servlet/*</url-pattern>
    </servlet-mapping>

(iii) Setup libraries required by Tomcat when using the dtf.war web application:
- copy the MySQL Connector/J driver mysql-connector-java-5.0.5-bin.jar to $CATALINA_HOME/common/lib
- copy the JavaMail library activation.jar to $CATALINA_HOME/common/lib
- NOTE: may also need xerces.jar, mail.jar, commons-logging-1.1.jar and log4j-1.2.8.jar

The DTFweb GUI makes JDBC connections to the MySQL database, and so Tomcat needs access to the
Connector/J driver. The DTFweb GUI also needs to use JavaMail to notify users that test runs have
completed.

(iv) The dtf.war web application includes the following Resource definition in META-INF/context.xml:

 <!-- the Tomcat 5 Context for the DTF web application -->
<Context>

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

This Resource is used to define access to the JDBC database from Tomcat. This context definition differs
in format and placement from the equivalent definition in Tomcat 4. You will need to adjust the url to
point to your MySQL database.

 The dtf.war web application accesses the defined resource via its WEB-INF/web.xml descriptor:

 <web-app>
    <display-name>DTF Web Interface</display-name>
    <description>GUI access to the DTF</description>

	<!--
	<servlet-mapping>
	  <servlet-name>invoker</servlet-name>
	  <url-pattern>/servlet/*</url-pattern>
	</servlet-mapping>
	-->

	<welcome-file-list>
	  <welcome-file>default.jsp</welcome-file>
	</welcome-file-list>

	<resource-ref>
		<description>
			Resource reference to a factory for java.sql.Connection
			instances that may be used for talking to the MySQL database.
		</description>
		<res-ref-name>jdbc/ResultsDB</res-ref-name>
		<res-type>javax.sql.DataSource</res-type>
		<res-auth>Container</res-auth>
	</resource-ref>

    <env-entry>
      <description>
        String value of SMTP server used to e-mail results via JavaMail
      </description>
      <env-entry-name>dtf/smtp_server</env-entry-name>
      <env-entry-value>localhost</env-entry-value>
      <env-entry-type>java.lang.String</env-entry-type>
    </env-entry>

</web-app>



(v) use the Tomcat manager web application to install the dtf.war web application correctly within
Tomcat. (This step can be completed after you have built and installed the DTF as described in 3.)

The manager application is accessible from the main Tomcat web page, or via the URL
http://locahost:8080/manager/html. You will be prompted for the username and password you defined
earlier in tomcat-users.xml when you access this page. At the bottom of the Manager page is a deployment
section where you may locate the dtf.war file and deploy it to the Tomcat instance.

(v) Once these steps have been completed, the DTF is then accessed via http://localhost:8080/dtf (or similar,
depending on your configured Tomcat host and port)

Generally, i've found that Tomcat can give problems of various types, and I found myself looking in
$CATALINA_HOME/logs/catalina.out too frequently. Common problems relate to
(i) the MySQL JDBC driver not being found
(ii) starting up Tomcat with different Java versions which seem to corrupt it and force reinstallation
of Tomcat and the DTF stuff.

3. Build the DTF from source and install
----------------------------------------
- I check out Branch_DTF_3_9 as a basic project in the workspace, not as a Java project using the
New Project Wizard. One of the tasks on my TODO list is to learn how to set it up as a Java project.

- the Branch_DTF_3_9 distribution should build cleanly under Eclipse, producing a 'dist' directory
containing the dtf distribution file adtf.zip and an installer script, among other stuff

-  to install the DTF, open the installer.xml file and set the properties:
* install.dir,  where you want the DTF software to reside,
* webapps.dir, where you want the webapp deployed
and then run the 'full' target of the install script:

> ant -f install.xml full

- the distribution is configured to run by default on a single host (called localhost), with the following settings:

* the DTFweb application installed at http://localhost:8080/dtf
* product definitions in $DTF_HOME/testenv/services/products
* product builds in $CATALINA_HOME/webapps/dtf/productbuilds
* product installers in $CATALINA_HOME/webapps/dtf/productinstallers
* product tests and selections in $CATALINA_HOME/webapps/dtf/producttests

- the installation as describes above has no products, no installers, no tests, but should start cleanly in
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
> ./run_testnode.sh

* point your browser to http://localhost:8080/dtf

- you should see the DTFweb GUI appear
- check that the DTFweb setup is correct. Click on the menu item "setup".
The resulting page of input fields should look something like this:
DefaultNameService URI	      //localhost:1099/NameService
UploadDirectory		      /opt/jakarta-tomcat-4.1.31/dtf/producttests
UploadWebDirectory	      http://localhost:8080/dtf/producttests
Root URL		      http://localhost:8080/dtf
- you may receive Tomcat exceptions upon startup if the installation has not been
performed correctly. Check the Tomcat logs in $TOMCAT_HOME/logs.


5. Run a test case against a product
------------------------------------
- as mentioned earlier, this running DTF instance has no products, test cases or test selections defined
yet
- to install a sample product, product installer, and sample test definition and selection, run the 'install-sample'
target of the installer.xml file. In order to run this example, you will need to do a couple of things:
(i) add an OS (e.g. Linux) in the Setup menu
(ii) associate the TestProduct with the TestProductInstaller in the Deployment menu (using the fully qualified name
of the installer as http://localhost:8080/dtf/productinstallers/TestProductInstaller.xml)
You should then be able to:
(i) deploy the product on a single test node from the Deployment menu
(ii) import the product definition, and run the sample test from the Management menu



Appendix
---------
These are instructions for using Tomcat 4 with the DTF.

2. Setup Tomcat for DTF use
---------------------------
- you need to several things to set up Tomcat:
1. modify $CATALINA_HOME/conf/server.xml to add a DTF context called dtf
2. modify $CATALINA_HOME/conf/web.xml to turn on the default servlet invoker
3. Copy the MySQL Connector/J driver mysql-connector-java-3.1.12-bin.jar to $CATALINA_HOME/common/lib
Although this driver also appears in the web application's local library WEB-INF/lib, not having it
in one of the other place seems to cause Tomcat problems.
4. copy the DTFweb web application war dtf.war in exploded form to $CATALINA_HOME/webapps (this step is
part of the general DTF installation described below)
5. adjust access permissions on the new web application directory, if desired. I set up a group
called tomcat which allows write access to the directories where users need to place installer scripts,
etc. and only read access elsewhere.

The DTF is then accessed via http://localhost:8080/dtf (or similar, depending on your configured host and port)

Here are diffs of the changes, with respect to the original server.xml and web.xml:

1. Add the dtf context to Tomcat

diff -r jakarta-tomcat-4.1.31/conf/server.xml /opt/jakarta-tomcat-4.1.31/conf/server.xml
380a381,484
>       <!-- Context for DTF -->
>
>       <Context path="/dtf" docBase="dtf">
>
>         <Logger className="org.apache.catalina.logger.FileLogger"
>                 prefix="localhost_DTF_log." suffix=".txt" timestamp="true"/>
>
>         <!-- set up JDBC access to the DTF database for the DTF web application -->
>         <Resource name="jdbc/ResultsDB" auth="Container" type="javax.sql.DataSource"/>
>
>           <ResourceParams name="jdbc/ResultsDB">
>
>           <parameter>
>             <name>username</name>
>             <value>dtfuser</value>
>           </parameter>
>
>           <parameter>
>             <name>password</name>
>             <value>dtfuser</value>
>           </parameter>
>
>           <parameter>
>             <name>driverClassName</name>
>             <value>org.gjt.mm.mysql.Driver</value>
>           </parameter>
>
>           <parameter>
>             <name>url</name>
>             <value>jdbc:mysql://localhost:3306/dtf</value>
>           </parameter>
>
>           <parameter>
>             <name>maxActive</name>
>             <value>8</value>
>           </parameter>
>
>           <parameter>
>             <name>maxIdle</name>
>             <value>4</value>
>           </parameter>
>
>       </ResourceParams>
>
>       </Context>


2. Turn on the servlet invoker - the DTFweb JSP pages sometimes call servlets with URLs like
http://localhost:8080/servlet/<servlet_name>. This involves removing comments surropunding the
servlet invoker definition.

diff -r jakarta-tomcat-4.1.31/conf/web.xml /opt/jakarta-tomcat-4.1.31/conf/web.xml
281d280
< <!--
286d284
< -->

Generally, i've found that Tomcat can give problems of various types, and I found myself looking in
$CATALINA_HOME/logs/catalina.out too frequently. Common problems relate to
(i) the MySQL JDBC driver not being found
(ii) starting up Tomcat with different Java versions which seem to corrupt it and force reinstallation
of Tomcat and the DTF stuff.
