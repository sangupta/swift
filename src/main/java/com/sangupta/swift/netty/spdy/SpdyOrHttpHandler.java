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

package com.sangupta.swift.netty.spdy;

import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.spdy.SpdyOrHttpChooser;

/**
 * 
 * @author sangupta
 *
 */
public class SpdyOrHttpHandler extends SpdyOrHttpChooser {
	
	private static final int MAX_CONTENT_LENGTH = 1024 * 1024;
	
	private final SpdyStaticFileServerHandler fileServerHandler;
	
	public SpdyOrHttpHandler(SpdyStaticFileServerHandler fileServerHandler) {
		this(MAX_CONTENT_LENGTH, MAX_CONTENT_LENGTH, fileServerHandler);
	}
	
	protected SpdyOrHttpHandler(int maxSpdyContentLength, int maxHttpContentLength, SpdyStaticFileServerHandler fileServerHandler) {
		super(maxSpdyContentLength, maxHttpContentLength);
		this.fileServerHandler = fileServerHandler;
	}
	
	@Override
	protected ChannelInboundHandler createHttpRequestHandlerForHttp() {
		return this.fileServerHandler;
	}

}
