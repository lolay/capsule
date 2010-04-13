package com.eharmony.capsule.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppInitializer implements ServletContextListener {

	
	public void contextDestroyed(ServletContextEvent sce) {
	}

	
	public void contextInitialized(ServletContextEvent sce) {

		// actiavete capsule
		System.setProperty("capsule.session.analysis.enabled", "true");

	}

}
