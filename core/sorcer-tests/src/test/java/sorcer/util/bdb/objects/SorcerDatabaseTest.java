package junit.sorcer.util.bdb.objects;

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.list;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import sorcer.core.SorcerConstants;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.util.ModelTable;
import sorcer.util.Sorcer;
import sorcer.util.SorcerUtil;
import sorcer.util.bdb.objects.UuidKey;
import sorcer.util.bdb.objects.UuidObject;

import com.sleepycat.collections.StoredMap;

/**
 * @author Mike Sobolewski
 */

public class SorcerDatabaseTest implements SorcerConstants {

	private final static Logger logger = Logger
			.getLogger(SorcerDatabaseTest.class.getName());
	
	static {
		System.setProperty("java.security.policy", Sorcer.getHome()
				+ "/configs/policy.all");
		System.setSecurityManager(new SecurityManager());
	}
	
	private static SorcerDatabaseRunner runner;
	private static File dbDir;
	
	@BeforeClass 
	public static void setUpOnce() throws Exception {
		dbDir = new File("./tmp/ju-sorcer-db");
		SorcerUtil.deleteDir(dbDir);
		System.out.println("Sorcer DB dir: " + dbDir.getCanonicalPath());
		dbDir.mkdirs();
		String homeDir = "./tmp/ju-sorcer-db";
		runner = new SorcerDatabaseRunner(homeDir);
        runner.run();
	}
	
	@AfterClass 
	public static void cleanup() throws Exception {
		if (runner != null) {
            try {
                // Always attempt to close the database cleanly.
                runner.close();
            } catch (Exception e) {
                System.err.println("Exception during database close:");
                e.printStackTrace();
            }
        }
		// delete database home directory and close database
		SorcerUtil.deleteDir(dbDir);
	}
	
	@Test
	public void storedcContextSetTest() throws Exception {
        // get from the database three contexts persisted   
		List<String> names = runner.returnContextNames();
        List<String> ln = list("c1", "c2", "c3");
		Collections.sort(names);
		logger.info("names: " + names);

        assertEquals(ln, names);
	}
	
	@Test
	public void storedContextMapTest() throws Exception {
		StoredMap<UuidKey, Context> sm = runner.getViews()
				.getContextMap();
		
		Iterator<Map.Entry<UuidKey, Context>> mei = sm
				.entrySet().iterator();
				
		List<String> names = new ArrayList<String>();
		Map.Entry<UuidKey, Context> entry = null;

		while (mei.hasNext()) {
			entry = mei.next();
			names.add(entry.getValue().getName());
		}
        List<String> ln = list("c1", "c2", "c3");
		Collections.sort(names);
		logger.info("names: " + names);

        assertEquals(ln, names);
	}
	
	@Test
	public void storedTableSetTest() throws Exception {
        // get from the database three tables persisted twice
		List<String> names = runner.returnTableNames();
		List<String> ln = list("undefined0", "undefined1", "undefined2");
		Collections.sort(names);
		logger.info("table names: " + names);

        assertEquals(ln, names);
	}
	
	@Test
	public void storedTableMapTest() throws Exception {
		StoredMap<UuidKey, ModelTable> sm = runner.getViews()
				.getTableMap();
		
		Iterator<Map.Entry<UuidKey, ModelTable>> it = sm
				.entrySet().iterator();
				
		List<String> names = new ArrayList<String>();
		Map.Entry<UuidKey, ModelTable> entry = null;

		while (it.hasNext()) {
			entry = it.next();
			names.add(entry.getValue().getName());
		}
		List<String> ln = list("undefined0", "undefined1", "undefined2");
		Collections.sort(names);
		logger.info("table names: " + names);

        assertEquals(ln, names);
	}
	
	@Test
	public void storedExertionSetTest() throws Exception {
        // get from the database two exertions persisted twice
		List<String> names = runner.returnExertionNames();
        List<String> ln = list("f1", "f4");
		Collections.sort(names);
		logger.info("names: " + names);

        assertEquals(ln, names);
	}
	
	@Test
	public void storedExertionMapTest() throws Exception {
		StoredMap<UuidKey, Exertion> sm = runner.getViews()
				.getExertionMap();
		
		Iterator<Map.Entry<UuidKey, Exertion>> it = sm
				.entrySet().iterator();
				
		List<String> names = new ArrayList<String>();
		Map.Entry<UuidKey, Exertion> entry = null;

		while (it.hasNext()) {
			entry = it.next();
			names.add(entry.getValue().getName());
		}
        List<String> ln = list("f1", "f4");
		Collections.sort(names);
		logger.info("names: " + names);

        assertEquals(ln, names);
	}
	
	@Test
	public void storedUuidObjectSetTest() throws Exception {
        // get from the database three sessions persisted with three tasks
		List<String> names = runner.returnUuidObjectNames();
		List<String> ln = list("Mike", "Sobolewski");
		Collections.sort(names);
		logger.info("names: " + names);

        assertEquals(ln, names);
	}
	
	@Test
	public void storedUuidObjectMapTest() throws Exception {
		StoredMap<UuidKey, UuidObject> sm = runner.getViews()
				.getUuidObjectMap();
		
		Iterator<Map.Entry<UuidKey, UuidObject>> it = sm
				.entrySet().iterator();
				
		List<String> names = new ArrayList<String>();
		Map.Entry<UuidKey, UuidObject> entry = null;

		while (it.hasNext()) {
			entry = it.next();
			names.add(entry.getValue().getObject().toString());
		}
		List<String> ln = list("Mike", "Sobolewski");
		Collections.sort(names);
		logger.info("names: " + names);

        assertEquals(ln, names);
	}
	
	//@Test
	public void sdbURL() throws Exception {
		URL sbdUrl = new URL("sbd://myIterface/name#context=2345");
		Object obj = sbdUrl.openConnection().getContent();
	}
}
