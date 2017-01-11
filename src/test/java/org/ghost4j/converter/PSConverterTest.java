/*
 * Ghost4J: a Java wrapper for Ghostscript API.
 *
 * Distributable under LGPL license.
 * See terms of license at http://www.gnu.org/licenses/lgpl.html.
 */

package org.ghost4j.converter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.ghost4j.Ghostscript;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.document.PSDocument;
import org.ghost4j.document.PaperSize;

public class PSConverterTest extends TestCase {

    public PSConverterTest(String testName) {
	super(testName);
    }

    @Override
    protected void setUp() throws Exception {
	super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
	super.tearDown();
    }

    public void testConvertWithPS() throws Exception {

	PSDocument document = new PSDocument();
	document.load(this.getClass().getClassLoader().getResourceAsStream("input.ps"));

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	PSConverter converter = new PSConverter();
	converter.convert(document, baos);

	assertTrue(baos.size() > 0);

	baos.close();
    }

    public void testConvertWithUnsupportedDocument() throws Exception {

	PDFDocument document = new PDFDocument();
	document.load(this.getClass().getClassLoader().getResourceAsStream("input.pdf"));

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	PSConverter converter = new PSConverter();
	converter.convert(document, baos);

	assertTrue(baos.size() > 0);

	baos.close();
    }

    public void testConvertWithPSMultiProcessLargeFile() throws Exception {

    File largeTestFile = createLargeTestPSFile();
    
    try {
    	
	final PSDocument document = new PSDocument();
	document.load(largeTestFile);
	
	final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
	final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
	final ByteArrayOutputStream baos3 = new ByteArrayOutputStream();

	final PSConverter converter = new PSConverter();
	converter.setMaxProcessCount(2);

	Thread thread1 = new Thread() {
	    @Override
	    public void run() {
		try {
		    System.out.println("START 1 " + Thread.currentThread());
		    converter.convert(document, baos1);
		    System.out.println("END 1 " + Thread.currentThread());
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    };
	};
	thread1.start();

	Thread thread2 = new Thread() {
	    @Override
	    public void run() {
		try {
		    System.out.println("START 2 " + Thread.currentThread());
		    converter.convert(document, baos2);
		    System.out.println("END 2 " + Thread.currentThread());
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    };
	};
	thread2.start();

	// the last one will block until a previous one finishes
	Thread thread3 = new Thread() {
	    @Override
	    public void run() {
		try {
		    System.out.println("START 3 " + Thread.currentThread());
		    converter.convert(document, baos3);
		    System.out.println("END 3 " + Thread.currentThread());
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    };
	};
	thread3.start();

	thread1.join();
	thread2.join();
	thread3.join();

	assertTrue(baos1.size() > 0);
	baos1.close();

	assertTrue(baos2.size() > 0);
	baos2.close();

	assertTrue(baos3.size() > 0);
	baos3.close();
	
    } finally {
		if (largeTestFile != null && largeTestFile.exists()) {
			largeTestFile.delete();
		}
    }
    
    }
    
    private File createLargeTestPSFile() throws Exception {
        // Merge input-2pages.ps multiple times to create a large file on demand, since github does not 
        // allow uploading files greater than 100MB.
        URL url = this.getClass().getClassLoader().getResource("input-2pages.ps");    	
        String sourceFile = new File(url.getFile()).getAbsolutePath();
        String tempDir = (System.getProperty("java.io.tmpdir").endsWith(File.separator)) ? System.getProperty("java.io.tmpdir") : System.getProperty("java.io.tmpdir") + File.separator;
        String outputFile = String.format("%s%s.ps", tempDir, UUID.randomUUID());
        
        List<String> gsArgs = new ArrayList<String>();
        gsArgs.add("-psconv");
        gsArgs.add("-dNOPAUSE");
        gsArgs.add("-dBATCH");
        gsArgs.add("-dSAFER");
        gsArgs.add("-dLanguageLevel=3");
        gsArgs.add("-dDEVICEWIDTHPOINTS=" + PaperSize.LETTER.getWidth());
        gsArgs.add("-dDEVICEHEIGHTPOINTS=" + PaperSize.LETTER.getHeight());
        gsArgs.add("-sDEVICE=ps2write");
        gsArgs.add("-sOutputFile="	+ outputFile);
        gsArgs.add("-q");
        gsArgs.add("-f");
        gsArgs.add("-q");
        for(int i = 0; i < 250; i++)  {
        	gsArgs.add(sourceFile);
        }
    	Ghostscript gs = Ghostscript.getInstance();
        // execute and exit interpreter
        synchronized (gs) {
    	gs.initialize(gsArgs.toArray(new String[0]));
    	gs.exit();
        }

        return new File(outputFile);
    }
    
    public void testConvertWithPSMultiProcess() throws Exception {

	final PSDocument document = new PSDocument();
	document.load(this.getClass().getClassLoader().getResourceAsStream("input.ps"));

	final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
	final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
	final ByteArrayOutputStream baos3 = new ByteArrayOutputStream();

	final PSConverter converter = new PSConverter();
	converter.setMaxProcessCount(2);

	Thread thread1 = new Thread() {
	    @Override
	    public void run() {
		try {
		    System.out.println("START 1 " + Thread.currentThread());
		    converter.convert(document, baos1);
		    System.out.println("END 1 " + Thread.currentThread());
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    };
	};
	thread1.start();

	Thread thread2 = new Thread() {
	    @Override
	    public void run() {
		try {
		    System.out.println("START 2 " + Thread.currentThread());
		    converter.convert(document, baos2);
		    System.out.println("END 2 " + Thread.currentThread());
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    };
	};
	thread2.start();

	// the last one will block until a previous one finishes
	Thread thread3 = new Thread() {
	    @Override
	    public void run() {
		try {
		    System.out.println("START 3 " + Thread.currentThread());
		    converter.convert(document, baos3);
		    System.out.println("END 3 " + Thread.currentThread());
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    };
	};
	thread3.start();

	thread1.join();
	thread2.join();
	thread3.join();

	assertTrue(baos1.size() > 0);
	baos1.close();

	assertTrue(baos2.size() > 0);
	baos2.close();

	assertTrue(baos3.size() > 0);
	baos3.close();
    }
    
}
