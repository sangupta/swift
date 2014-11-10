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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sangupta.swift.netty.NettyServer;
import com.sangupta.swift.netty.http.HttpStaticFileServer;
import com.sangupta.swift.netty.proxy.ReverseProxyServer;
import com.sangupta.swift.netty.spdy.SpdyStaticFileServer;

/**
 * Swift is the main server, that runs multiple servers
 * in a single swift instance.
 * 
 * @author sangupta
 *
 */
public class Swift {
	
	private Set<SwiftServer> registeredServers = new HashSet<SwiftServer>();
	
	public Swift addServer(SwiftServer server) {
		if(server == null) {
			throw new IllegalArgumentException("SwiftServer to be added cannot be null");
		}
		
		boolean added = this.registeredServers.add(server);
		if(!added) {
			throw new IllegalStateException("SwiftServer has already been added before");
		}
		
		return this;
	}

	/**
	 * Start operating
	 * 
	 */
	public void start() {
		if(registeredServers.isEmpty()) {
			System.out.println("No server configuration has been created, nothing to do... exiting!");
			return;
		}
		
		List<NettyServer> nettyServers = new ArrayList<NettyServer>();
		for(SwiftServer server : registeredServers) {
			NettyServer ns = initializeServer(server);
			if(ns == null) {
				// TODO: throw some exception
				return;
			}
			
			nettyServers.add(ns);
		}
		
		System.out.println("All servers have been configured and started!");
	}

	/**
	 * Hold this thread till swift execution ends
	 * 
	 */
	public void join() {
		System.out.println("Waiting for server to shutdown!");
		do {
			
		} while(true);
	}

	// Internal methods follow
	
	/**
	 * Initialize a swift server
	 * 
	 * @param server
	 */
	private NettyServer initializeServer(SwiftServer server) {
		if(server.isDocumentRootExists()) {
			// check for spdy
			if(server.isSpdyEnabled()) {
				return new SpdyStaticFileServer(server);
			}
			
			// normal plain vanilla http server
			return new HttpStaticFileServer(server);
		}
		
		if(server.isProxyEnabled()) {
			return new ReverseProxyServer(server);
		}
		
		return null;
	}

}
