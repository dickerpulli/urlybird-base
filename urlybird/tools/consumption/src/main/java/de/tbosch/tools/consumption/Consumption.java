package de.tbosch.tools.consumption;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hauptklasse mit main-Methode.
 * @author Thomas Bosch
 */
public class Consumption {

	/**
	 * Startet den Spring-ApplicationContext.
	 * @param args [ungenutzt]
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new ClassPathXmlApplicationContext("/context.xml").start();
	}

}
