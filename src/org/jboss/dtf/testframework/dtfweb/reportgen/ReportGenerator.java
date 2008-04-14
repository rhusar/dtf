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
// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ReportGenerator.java

package org.jboss.dtf.testframework.dtfweb.reportgen;

import org.jboss.dtf.testframework.dtfweb.*;
import org.jboss.dtf.testframework.dtfweb.utils.DateUtils;
import org.jboss.dtf.testframework.testnode.RunUID;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;

public class ReportGenerator
{
    private class OverAllResults
    {

        public int _numberOfPasses;
        public int _numberOfFailures;
        public int _numberOfTimeouts;

        private OverAllResults()
        {
        }

    }


    public ReportGenerator()
    {
        _archive = null;
    }

    private void createTitlePage(Document doc)
        throws DocumentException
    {
        Paragraph title = new Paragraph();
        title.add(new Chunk(_archive.getTitle(), FontFactory.getFont("Helvetica-Bold", 18F)));
        title.add(new Chunk("\nDistributed Testing Framework\n", FontFactory.getFont("Helvetica-Bold", 14F)));
        title.add(new Chunk("Arjuna Technologies Ltd.\n", FontFactory.getFont("Helvetica-Bold", 12F)));
        doc.add(title);
        Paragraph intro = new Paragraph();
        intro.add(new Chunk(_archive.getComments()));
        intro.add(new Chunk("\nThe following run's results are included in this report:"));
        doc.add(intro);
        PdfPTable runTable = new PdfPTable(1);
        PdfPCell cell = new PdfPCell(new Phrase("Test Run Information", FontFactory.getFont("Courier", 10F)));
        cell.setBackgroundColor(Color.lightGray);
        runTable.addCell(cell);
        RunUID runIds[] = _archive.getRunIds();
        for(int count = 0; count < runIds.length; count++)
        {
            RunUID runId = runIds[count];
            PdfPTable runInformationTable = new PdfPTable(2);
            runInformationTable.setWidths(new int[] {
                25, 75
            });
            WebRunInformation runInfo = ResultsManager.getTestRunInformation(runId.getUID());
            PdfPCell nameCell = new PdfPCell(new Phrase("Run Id.", FontFactory.getFont("Courier-Bold", 10F)));
            PdfPCell valueCell = new PdfPCell(new Phrase("" + runId.getUID(), FontFactory.getFont("Courier", 10F)));
            nameCell.setBorder(0);
            valueCell.setBorder(0);
            runInformationTable.addCell(nameCell);
            runInformationTable.addCell(valueCell);
            nameCell = new PdfPCell(new Phrase("Date Time Started:", FontFactory.getFont("Courier-Bold", 10F)));
            valueCell = new PdfPCell(new Phrase(runInfo.dateTimeStarted.toString(), FontFactory.getFont("Courier", 10F)));
            nameCell.setBorder(0);
            valueCell.setBorder(0);
            runInformationTable.addCell(nameCell);
            runInformationTable.addCell(valueCell);
            nameCell = new PdfPCell(new Phrase("Date Time Finished:", FontFactory.getFont("Courier-Bold", 10F)));
            valueCell = new PdfPCell(new Phrase(DateUtils.displayDate(runInfo.dateTimeFinished), FontFactory.getFont("Courier", 10F)));
            nameCell.setBorder(0);
            valueCell.setBorder(0);
            runInformationTable.addCell(nameCell);
            runInformationTable.addCell(valueCell);
            nameCell = new PdfPCell(new Phrase("Test Definitions:", FontFactory.getFont("Courier-Bold", 10F)));
            valueCell = new PdfPCell(new Phrase(runInfo.testDefinitionsDescription + " (" + runInfo.testDefinitionsURL + ")", FontFactory.getFont("Courier", 10F)));
            nameCell.setBorder(0);
            valueCell.setBorder(0);
            runInformationTable.addCell(nameCell);
            runInformationTable.addCell(valueCell);
            nameCell = new PdfPCell(new Phrase("Test Selections:", FontFactory.getFont("Courier-Bold", 10F)));
            valueCell = new PdfPCell(new Phrase(runInfo.testSelectionDescription + " (" + runInfo.testSelectionURL + ")", FontFactory.getFont("Courier", 10F)));
            nameCell.setBorder(0);
            valueCell.setBorder(0);
            runInformationTable.addCell(nameCell);
            runInformationTable.addCell(valueCell);
            nameCell = new PdfPCell(new Phrase("Software Version:", FontFactory.getFont("Courier-Bold", 10F)));
            valueCell = new PdfPCell(new Phrase(runInfo.softwareVersion, FontFactory.getFont("Courier", 10F)));
            nameCell.setBorder(0);
            valueCell.setBorder(0);
            runInformationTable.addCell(nameCell);
            runInformationTable.addCell(valueCell);
            nameCell = new PdfPCell(new Phrase("Passes:", FontFactory.getFont("Courier-Bold", 10F)));
            valueCell = new PdfPCell(new Phrase(runInfo.getPercentagePassed() + "%", FontFactory.getFont("Courier", 10F)));
            nameCell.setBorder(0);
            valueCell.setBorder(0);
            runInformationTable.addCell(nameCell);
            runInformationTable.addCell(valueCell);
            nameCell = new PdfPCell(new Phrase("Failures:", FontFactory.getFont("Courier-Bold", 10F)));
            valueCell = new PdfPCell(new Phrase(runInfo.getPercentageFailed() + "%", FontFactory.getFont("Courier", 10F)));
            nameCell.setBorder(0);
            valueCell.setBorder(0);
            runInformationTable.addCell(nameCell);
            runInformationTable.addCell(valueCell);
            runTable.addCell(runInformationTable);
        }

        doc.add(runTable);
    }

    private OverAllResults createResultsPage(Document doc, RunUID runId)
        throws DocumentException, IOException
    {
        OverAllResults oar = new OverAllResults();
        doc.newPage();
        WebRunInformation runInfo = ResultsManager.getTestRunInformation(runId.getUID());
        PdfPTable runTable = new PdfPTable(6);
        PdfPCell cell = new PdfPCell(new Phrase("Run Id.", FontFactory.getFont("Courier", 10F)));
        cell.setBackgroundColor(Color.lightGray);
        runTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Date/Time Started", FontFactory.getFont("Courier", 10F)));
        cell.setBackgroundColor(Color.lightGray);
        runTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Date/Time Finished", FontFactory.getFont("Courier", 10F)));
        cell.setBackgroundColor(Color.lightGray);
        runTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Test Definitions", FontFactory.getFont("Courier", 10F)));
        cell.setBackgroundColor(Color.lightGray);
        runTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Test Selections", FontFactory.getFont("Courier", 10F)));
        cell.setBackgroundColor(Color.lightGray);
        runTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Software Version", FontFactory.getFont("Courier", 10F)));
        cell.setBackgroundColor(Color.lightGray);
        runTable.addCell(cell);
        runTable.addCell(new Phrase("" + runInfo.runId, FontFactory.getFont("Courier", 10F)));
        runTable.addCell(new Phrase(runInfo.dateTimeStarted.toString(), FontFactory.getFont("Courier", 10F)));
        runTable.addCell(new Phrase(DateUtils.displayDate(runInfo.dateTimeFinished), FontFactory.getFont("Courier", 10F)));
        runTable.addCell(new Phrase(runInfo.testDefinitionsDescription + "(" + runInfo.testDefinitionsURL + ")", FontFactory.getFont("Courier", 10F)));
        runTable.addCell(new Phrase(runInfo.testSelectionDescription + "(" + runInfo.testSelectionURL + ")", FontFactory.getFont("Courier", 10F)));
        runTable.addCell(new Phrase(runInfo.softwareVersion, FontFactory.getFont("Courier", 10F)));
        doc.add(runTable);
        TestResultInformation results[] = ResultsManager.getResultsForTestRun(runId.getUID(), "DateTimeStarted");
        int numberOfTests = 0;
        int numberOfPasses = 0;
        int numberOfFails = 0;
        int numberOfTimeouts = 0;
        for(int count = 0; count < results.length; count++)
        {
            numberOfTests++;
            if(results[count].hasTestPassed())
                numberOfPasses++;
            else
            if(results[count].hasTestFailed())
                numberOfFails++;
            if(results[count].hasTestTimedout())
                numberOfTimeouts++;
        }

        oar._numberOfPasses = numberOfPasses;
        oar._numberOfFailures = numberOfFails;
        oar._numberOfTimeouts = numberOfTimeouts;
        Table chartTable = new Table(1);
        chartTable.setPadding(5F);
        Cell infoCell = new Cell(new Phrase("Number of tests: " + numberOfTests + "\nNumber of passes: " + numberOfPasses + "\nNumber of fails: " + numberOfFails, FontFactory.getFont("Courier", 10F)));
        infoCell.setBorder(0);
        chartTable.addCell(infoCell);
        doc.add(chartTable);
        PdfPTable resultsTable = new PdfPTable(3);
        resultsTable.setWidths(new int[] {
            70, 20, 10
        });
        cell = new PdfPCell(new Phrase("Test Name", FontFactory.getFont("Courier", 10F)));
        cell.setBackgroundColor(Color.lightGray);
        resultsTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Permutation", FontFactory.getFont("Courier", 10F)));
        cell.setBackgroundColor(Color.lightGray);
        resultsTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Results", FontFactory.getFont("Courier", 10F)));
        cell.setBackgroundColor(Color.lightGray);
        resultsTable.addCell(cell);
        for(int count = 0; count < results.length; count++)
        {
            resultsTable.addCell(new Phrase(results[count].testName, FontFactory.getFont("Courier", 10F)));
            resultsTable.addCell(new Phrase(ResultsManager.getOSProductCombination(results[count].permutationCode, "<br>"), FontFactory.getFont("Courier", 10F)));
            cell = new PdfPCell(new Phrase(results[count].overAllResult, FontFactory.getFont("Courier", 10F, 0, Color.decode(results[count].getColor()))));
            resultsTable.addCell(cell);
        }

        doc.add(resultsTable);
        return oar;
    }

    private void createOverviewPage(OverAllResults results[], Document doc)
        throws DocumentException
    {
        OverAllResults oar = new OverAllResults();
        for(int count = 0; count < results.length; count++)
        {
            oar._numberOfFailures += results[count]._numberOfFailures;
            oar._numberOfPasses += results[count]._numberOfPasses;
            oar._numberOfTimeouts += results[count]._numberOfTimeouts;
        }

        Table resultsTable = new Table(1);
        resultsTable.setPadding(5F);
        int numberOfTests = oar._numberOfFailures + oar._numberOfPasses;
        Cell infoCell = new Cell(new Phrase("Number of tests: " + numberOfTests + "\nNumber of passes: " + oar._numberOfPasses + " (" + ((float)oar._numberOfPasses / (float)numberOfTests) * 100F + "%)\nNumber of fails: " + oar._numberOfFailures + " (" + ((float)oar._numberOfFailures / (float)numberOfTests) * 100F + "%)\nNumber of timeouts: " + oar._numberOfTimeouts + " (" + ((float)oar._numberOfTimeouts / (float)numberOfTests) * 100F + "%)", FontFactory.getFont("Courier", 10F)));
        resultsTable.addCell(infoCell);
        doc.add(resultsTable);
    }

    public void generateReport(ArchiveInformation archive, OutputStream out)
    {
        try
        {
            _archive = archive;
            Document pdfDoc = new Document();
            PdfWriter.getInstance(pdfDoc, out);
            pdfDoc.addTitle("DTF Report");
            pdfDoc.addAuthor("DTF-Report-Generator");
            pdfDoc.addCreationDate();
            pdfDoc.open();
            createTitlePage(pdfDoc);
            RunUID runIds[] = _archive.getRunIds();
            OverAllResults results[] = new OverAllResults[runIds.length];
            for(int count = 0; count < runIds.length; count++)
                results[count] = createResultsPage(pdfDoc, runIds[count]);

            createOverviewPage(results, pdfDoc);
            pdfDoc.close();
        }
        catch(Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    private static DTFResultsManager ResultsManager = new DTFResultsManager();
    private ArchiveInformation _archive;

}
