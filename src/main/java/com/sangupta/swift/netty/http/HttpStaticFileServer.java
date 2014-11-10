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

package com.sangupta.swift.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.sangupta.swift.SwiftServer;
import com.sangupta.swift.netty.NettyServer;

/**
 * A simple file server
 * 
 * @author sangupta
 *
 */
public class HttpStaticFileServer extends NettyServer {
	
	public HttpStaticFileServer(SwiftServer server) {
		if(server.isSslEnabled()) {
			// TODO: fix this
		} else {
			this.sslContext = null;
		}
		
		this.bossGroup = new NioEventLoopGroup(1);
		this.workerGroup = new NioEventLoopGroup();
		
		try {
			this.serverBootstrap = new ServerBootstrap();
			
			HttpStaticFileServerHandler fileServerHandler = new HttpStaticFileServerHandler(server.getDocumentRoot());
			
			this.serverBootstrap.group(this.bossGroup, this.workerGroup)
						  .channel(NioServerSocketChannel.class)
						  .childHandler(new HttpStaticFileServerInitializer(this.sslContext, fileServerHandler));
						  
			this.channel = this.serverBootstrap.bind(server.getListenPort()).sync().channel();
			
			System.out.println("Listening on port " + server.getListenPort());
			
			this.channel.closeFuture().awaitUninterruptibly();
		} catch(InterruptedException e) {
			// TODO: think what we can do with this
		} finally {
			this.shutdownGracefully();
		}
	}

}
