package junit.sorcer.core.exertion;

//import com.gargoylesoftware,base,testing,TestUtil;
import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.from;
import static sorcer.eo.operator.*;

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import junit.sorcer.core.provider.AdderImpl;
import junit.sorcer.core.provider.MultiplierImpl;
import junit.sorcer.core.provider.SubtractorImpl;

import org.junit.Test;

import sorcer.service.Signature;
import sorcer.service.Signature.Direction;
import sorcer.service.Task;
import sorcer.util.Sorcer;

/**
 * @author Mike Sobolewski
 */

public class BatchTaskTest {
	private final static Logger logger = Logger.getLogger(TaskTest.class
			.getName());

	static {
		System.setProperty("java.util.logging.config.file",
				Sorcer.getHome() + "/configs/sorcer.logging");
		System.setProperty("java.security.policy", Sorcer.getHome()
				+ "/configs/policy.all");
		System.setSecurityManager(new RMISecurityManager());
		Sorcer.setCodeBase(new String[] { "arithmetic-beans.jar" });
	}
	
	@Test
	public void batchTask3Test() throws Exception {
		// batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
		// shared context with named paths
		Task batch3 = task("batch3",
				type(sig("multiply", MultiplierImpl.class, result("subtract/x1", Direction.IN)), Signature.PRE),
				type(sig("add", AdderImpl.class, result("subtract/x2", Direction.IN)), Signature.PRE),
				sig("subtract", SubtractorImpl.class, result("result/y", from("subtract/x1", "subtract/x2"))),
				context(in("multiply/x1", 10.0), in("multiply/x2", 50.0), 
						in("add/x1", 20.0), in("add/x2", 80.0)));
		
		logger.info("task getSignatures:" + batch3.getFidelity());
				
		batch3 = exert(batch3);
//		//logger.info("task result/y: " + get(batch3, "result/y"));
//		assertEquals("Wrong value for 400.0", get(batch3, "result/y"), 400.0);
	}
	
	
	@Test
	public void batchTask4Test() throws Exception {
		// batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
		// shared context with prefixed paths
		Task batch3 = task("batch3",
				type(sig("multiply#op1", MultiplierImpl.class, result("op3/x1", Direction.IN)), Signature.PRE),
				type(sig("add#op2", AdderImpl.class, result("op3/x2", Direction.IN)), Signature.PRE),
				sig("subtract", SubtractorImpl.class, result("result/y", from("op3/x1", "op3/x2"))),
				context(in("op1/x1", 10.0), in("op1/x2", 50.0), 
						in("op2/x1", 20.0), in("op2/x2", 80.0)));
		
		batch3 = exert(batch3);
		//logger.info("task result/y: " + get(batch3, "result/y"));
		assertEquals("Wrong value for 400.0", get(batch3, "result/y"), 400.0);
	}
}
	
