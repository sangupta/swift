/**
 *
 * swift - Netty based HTTP Server
 * Copyright (c) 2014, Sandeep Gupta
 * 
 * http://sangupta.com/projects/swift
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.sangupta.swift;


/**
 * A simple class to test swift server.
 * 
 * @author sangupta
 *
 */
public class SwiftServerTest {
	
	public static void main(String[] args) {
		SwiftServer server = new SwiftServer().listen(23232)
										  .withServerName("swift")
										  .proxy("test.multiplx.com")
										  .addProxyHeader("Host", "$host")
										  .addProxyHeader("X-Real-IP", "$remote_addr")
										  .addProxyHeader("X-Forwarded-For", "$remote_addr");
		
//		SwiftServer httpServer = new SwiftServer().listen(8080)
//												  .withServerName("swift")
//												  .enableSSL()
//												  .usingSelfSignedCert()
//												  .enableSpdySupport()
//												  .withDocumentRoot(new File("c:/logs"));
		
//		SwiftServer httpOrSpdyServer = new SwiftServer().listen(8082)
//														.withServerName("swift.com")
//														.enableSpdySupport()
//														.withDocumentRoot(new File("/Users/sangupta/logs"));
		
		Swift swift = new Swift()
						.addServer(server)
//						.addServer(proxyServer)
//						.addServer(httpServer)
//						.addServer(httpOrSpdyServer)
						;
		
		swift.start();
		
		swift.join();
	}

}
