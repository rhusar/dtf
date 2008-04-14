/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2008,
 * @author JBoss Inc.
 */
//
// Copyright (C) 2001,
//
// HP Arjuna Labs
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: UploadFileDescriptions.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import java.util.ArrayList;

public class UploadFileDescriptions
{
    private ArrayList _uploadFiles = new ArrayList();

    public void addFile(String cvsModule, String cvsTag, String product, String os, String dateTime, String filename)
    {
        UploadFile uf = new UploadFile();

        uf._cvsModule = cvsModule;
        uf._cvsTag = cvsTag;
        uf._product = product;
        uf._os = os;
        uf._dateTime = dateTime;
        uf._filename = filename;

        System.out.println("Adding descriptor for "+cvsModule+","+filename+","+product+","+os);
        _uploadFiles.add(uf);
    }

    public UploadFile getFileDescriptor(String cvsModule, String cvsTag, String product, String os)
    {
        for (int count=0;count<_uploadFiles.size();count++)
        {
            UploadFile uf = (UploadFile)_uploadFiles.get(count);

			System.out.println("CVSModule:"+uf._cvsModule+" CVSTag:"+uf._cvsTag+" product:"+uf._product+" OS:"+uf._os);
            if ( (uf._cvsModule.equalsIgnoreCase(cvsModule)) &&
                 (uf._cvsTag.equalsIgnoreCase(cvsTag)) &&
                 (uf._product.equalsIgnoreCase(product)) &&
                 (uf._os.equalsIgnoreCase(os)) )
            {
                return(uf);
            }
       }

       return(null);
   }
}
