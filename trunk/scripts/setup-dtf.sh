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
if test "'x$DTF_HOME'" = "'x'"
then

  echo DTF_HOME environment variable not set

else

CLASSPATH=$CLASSPATH":$DTF_HOME/lib/ext/jdom.jar"
CLASSPATH=$CLASSPATH":$DTF_HOME/lib/ext/xerces.jar"
CLASSPATH=$CLASSPATH":$DTF_HOME/lib/ext/servlet.jar"
CLASSPATH=$CLASSPATH":$DTF_HOME/lib/ext/activation.jar"
CLASSPATH=$CLASSPATH":$DTF_HOME/lib/ext/mailapi.jar"
CLASSPATH=$CLASSPATH":$DTF_HOME/lib/ext/mail.jar"
CLASSPATH=$CLASSPATH":$DTF_HOME/lib/ext/struts.jar"
CLASSPATH=$CLASSPATH":$DTF_HOME/lib/ext/smtp.jar"
CLASSPATH=$CLASSPATH":$DTF_HOME/lib/ext/jdbc2_0-stdext.jar"
CLASSPATH=$CLASSPATH":$DTF_HOME/lib/TestingFramework.jar"
CLASSPATH=$CLASSPATH":$DTF_HOME/lib/DTFTools.jar"
export CLASSPATH

PATH=$PATH":$DTF_HOME/bin"
export PATH

fi
