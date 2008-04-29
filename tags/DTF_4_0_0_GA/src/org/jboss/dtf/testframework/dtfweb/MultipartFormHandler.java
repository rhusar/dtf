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
package org.jboss.dtf.testframework.dtfweb;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.util.*;
import java.io.*;

import javax.servlet.http.*;

public class MultipartFormHandler
{
	protected Map<String, Object> _formData = new HashMap<String, Object>();
	protected ArrayList			_files = new ArrayList();

	public Object getFormDataParameter(String key)
	{
		return _formData.get(key);
	}

	public void uploadFiles(HttpServletRequest request, String destDir)
	{
		try
		{
            if(!ServletFileUpload.isMultipartContent(request)) {
                return;
            }

            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(1024*1024);
            upload.setSizeMax(1024*1024*20);
            List<FileItem> items = upload.parseRequest(request);

            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();

                if (item.isFormField()) {
                    _formData.put(item.getFieldName(), item.getString());
                } else {
                    File file = new File(destDir+"/"+item.getName());
                    item.write(file);
                    _formData.put("upload", file);
                }
            }
		}
		catch (Exception e)
		{
			System.err.println("ERROR - While retrieving client-side files");
			e.printStackTrace(System.err);
		}
	}
}
