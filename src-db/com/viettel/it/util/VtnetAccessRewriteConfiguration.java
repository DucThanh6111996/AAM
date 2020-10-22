package com.viettel.it.util;

import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.rule.Join;

import javax.servlet.ServletContext;

@RewriteConfiguration
public class VtnetAccessRewriteConfiguration extends HttpConfigurationProvider {
	@Override
	public Configuration getConfiguration(ServletContext context) {
		return ConfigurationBuilder
				.begin()
				.addRule(Join.path("/").to("/index.xhtml"))
				.addRule(Join.path("/index").to("/faces/home/index.xhtml"))
				.addRule(Join.path("/home").to("/faces/home/index.xhtml"))
				.addRule(Join.path("/error").to("/error.xhtml"));
	}

	@Override
	public int priority() {
		return 0;
	}
}
