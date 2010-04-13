package com.eharmony.capsule.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.CatalogBase;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.chain.impl.ContextBase;

import com.eharmony.capsule.chain.Base64Command;
import com.eharmony.capsule.chain.CryptoTranscoderCommand;
import com.eharmony.capsule.chain.HeaderCommand;
import com.eharmony.capsule.chain.JavaSerializerCommand;
import com.eharmony.capsule.chain.Mode;

public class LogAnalyzer {
	
	private Chain chain;
	private Catalog catalog;

	public LogAnalyzer() {
		catalog = new CatalogBase();
		catalog.addCommand("base64", new Base64Command());
		catalog.addCommand("header", new HeaderCommand());
		catalog.addCommand("crypto", new CryptoTranscoderCommand());
		catalog.addCommand("javaser", new JavaSerializerCommand());
		buildChainFromString("base64,header,crypto,javaser");
	}
	
	public LogAnalyzer(String chain) {
		this();
		buildChainFromString(chain);
	}
	
	private void buildChainFromString(String s) {
		String[] commands = s.split(",");
		List<String> commandList = Arrays.asList(commands);
		chain = new ChainBase();
		for (String name : commandList) {
			chain.addCommand(catalog.getCommand(name));
		}
	}
	
	public Object analyze(String encoded) throws Exception {
		Context context = new ContextBase();
		context.put("mode", Mode.IN);
		context.put("bytes", encoded.getBytes());
		chain.execute(context);
		return context.get("attributes");
	}
	
	public static void main(String[] args) {
		LogAnalyzer me = new LogAnalyzer();
	}

}