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

package com.sangupta.swift.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;

/**
 * The netty server created as part of the swift server configurations
 * 
 * @author sangupta
 *
 */
public abstract class NettyServer {
	
	protected SslContext sslContext;
	
	protected EventLoopGroup bossGroup;
	
	protected EventLoopGroup workerGroup;
	
	protected ServerBootstrap serverBootstrap;
	
	protected Channel channel;

	/**
	 * Shutdown this netty implementation gracefully
	 * 
	 */
	public void shutdownGracefully() {
		if(this.bossGroup != null) {
			this.bossGroup.shutdownGracefully();
		}
		
		if(this.workerGroup != null) {
			this.workerGroup.shutdownGracefully();
		}
	}
}
