package com.viettel.it.webservice;

import javax.jws.WebService;

//Service Implementation Bean

@WebService(endpointInterface = "com.viettel.webservice.HelloWorld")
public class HelloWorldImpl implements HelloWorld{

	@Override
	public String getHelloWorldAsString() {
		return "Hello World JAX-WS";
	}
}