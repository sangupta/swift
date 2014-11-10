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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.spdy.SpdyOrHttpChooser.SelectedProtocol;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.SSLException;

import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.swift.SwiftServer;
import com.sangupta.swift.netty.NettyServer;

/**
 * A simple file server that handles static file serving
 * 
 * @author sangupta
 *
 */
public class SpdyStaticFileServer extends NettyServer {
	
	public SpdyStaticFileServer(SwiftServer server) {
		if(server.isSpdyEnabled() && !server.isSslEnabled()) {
			throw new IllegalStateException("SPDY can only be enabled along with SSL");
		}
		
		if(server.isSslEnabled()) {
			if(server.isSelfSignedSSL()) {
				SelfSignedCertificate ssc;
				
				if(server.isSpdyEnabled()) {
					try {
						ssc = new SelfSignedCertificate();
						this.sslContext = SslContext.newServerContext(ssc.certificate(), ssc.privateKey(), null, null, 
													Arrays.asList(SelectedProtocol.SPDY_3_1.protocolName(), SelectedProtocol.HTTP_1_1.protocolName()), 0, 0);
					} catch (CertificateException e) {
						throw new RuntimeException("Unable to initialize self-signed SSL certificate");
					} catch(SSLException e) {
						throw new RuntimeException("Unable to initialize self-signed SSL certificate");
					}
				} else {
					// basic self signed cert
					try {
						ssc = new SelfSignedCertificate();
						this.sslContext = SslContext.newServerContext(SslProvider.JDK, ssc.certificate(), ssc.privateKey());
					} catch (CertificateException e) {
						e.printStackTrace();
						throw new RuntimeException("Unable to initialize self-signed SSL certificate");
					} catch(SSLException e) {
						e.printStackTrace();
						throw new RuntimeException("Unable to initialize self-signed SSL certificate");
					}
				}
			}
		} else {
			this.sslContext = null;
		}
		
		this.bossGroup = new NioEventLoopGroup(1);
		this.workerGroup = new NioEventLoopGroup();
		
		try {
			this.serverBootstrap = new ServerBootstrap();
			if(server.isSpdyEnabled()) {
				this.serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
			}
			
			SpdyStaticFileServerHandler fileServerHandler = new SpdyStaticFileServerHandler(server);
			
			this.serverBootstrap.group(this.bossGroup, this.workerGroup)
						  		.channel(NioServerSocketChannel.class)
						  		.childHandler(new SpdyStaticFileServerInitializer(this.sslContext, fileServerHandler));
						  
			if(AssertUtils.isNotEmpty(server.getServerName())) {
				this.channel = this.serverBootstrap.bind(server.getServerName(), server.getListenPort()).sync().channel();
			} else {
				this.channel = this.serverBootstrap.bind(server.getListenPort()).sync().channel();
			}
			
			System.out.println("Listening on port " + server.getListenPort());
			
			this.channel.closeFuture().sync();
		} catch(InterruptedException e) {
			// TODO: think what we can do with this
		} finally {
			this.shutdownGracefully();
		}
	}

}
