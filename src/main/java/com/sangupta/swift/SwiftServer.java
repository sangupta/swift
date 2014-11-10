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

import java.io.File;
import java.util.UUID;

/**
 * A single endpoint to which we can listen.
 * 
 * @author sangupta
 *
 */
public class SwiftServer {
	
private final String serverID = UUID.randomUUID().toString();
	
	private int listenPort;
	
	private String serverName = null; // indicates that we accept all server names for now
	
	private File documentRoot = null;
	
	private boolean documentRootExists = false;
	
	private boolean spdyEnabled = false;
	
	private boolean sslEnabled = false;
	
	private boolean selfSignedSSL = true;
	
	private boolean proxyEnabled = false;

	public SwiftServer listen(int port) {
		this.listenPort = port;
		return this;
	}
	
	public SwiftServer withServerName(String name) {
		if(name == null) {
			this.serverName = null;
			return this;
		}
		
		this.serverName = name.toLowerCase();
		return this;
	}
	
	public SwiftServer proxy(String destination) {
		return this;
	}

	public SwiftServer addProxyHeader(String name, String value) {
		return this;
	}

	/**
	 * Setup the document root from where all files are served.
	 * 
	 * @param file
	 * @return
	 */
	public SwiftServer withDocumentRoot(File file) {
		this.documentRoot = file;
		if(file == null || !file.exists() || !file.isDirectory() || !file.canRead()) {
			this.documentRootExists = false;
			return this;
		}
		
		this.documentRootExists = true;
		return this;
	}

	public SwiftServer enableSpdySupport() {
		this.spdyEnabled = true;
		this.sslEnabled = true;
		return this;
	}
	
	public SwiftServer usingSelfSignedCert() {
		this.selfSignedSSL = true;
		return this;
	}
	
	public SwiftServer enableSSL() {
		this.sslEnabled = true;
		return this;
	}

	// Basic methods
	
	@Override
	public int hashCode() {
		return this.serverID.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		
		if(this == obj) {
			return true;
		}
		
		if(!(obj instanceof SwiftServer)) {
			return false;
		}
		
		SwiftServer server = (SwiftServer) obj;
		return this.serverID.equals(server.serverID); // serverID can never be null
	}
	
	// Usual accessors follow

	/**
	 * @return the documentRootExists
	 */
	public boolean isDocumentRootExists() {
		return documentRootExists;
	}

	/**
	 * @param documentRootExists the documentRootExists to set
	 */
	public void setDocumentRootExists(boolean documentRootExists) {
		this.documentRootExists = documentRootExists;
	}

	/**
	 * @return the spdyEnabled
	 */
	public boolean isSpdyEnabled() {
		return spdyEnabled;
	}

	/**
	 * @param spdyEnabled the spdyEnabled to set
	 */
	public void setSpdyEnabled(boolean spdyEnabled) {
		this.spdyEnabled = spdyEnabled;
	}

	/**
	 * @return the sslEnabled
	 */
	public boolean isSslEnabled() {
		return sslEnabled;
	}

	/**
	 * @param sslEnabled the sslEnabled to set
	 */
	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	/**
	 * @return the proxyEnabled
	 */
	public boolean isProxyEnabled() {
		return proxyEnabled;
	}

	/**
	 * @param proxyEnabled the proxyEnabled to set
	 */
	public void setProxyEnabled(boolean proxyEnabled) {
		this.proxyEnabled = proxyEnabled;
	}

	/**
	 * @return the serverID
	 */
	public String getServerID() {
		return serverID;
	}

	/**
	 * @return the listenPort
	 */
	public int getListenPort() {
		return listenPort;
	}

	/**
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * @return the documentRoot
	 */
	public File getDocumentRoot() {
		return documentRoot;
	}

	/**
	 * @return the selfSignedSSL
	 */
	public boolean isSelfSignedSSL() {
		return selfSignedSSL;
	}

}
