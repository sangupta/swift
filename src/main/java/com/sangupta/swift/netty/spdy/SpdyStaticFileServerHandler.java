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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Date;

import com.sangupta.swift.SwiftServer;
import com.sangupta.swift.netty.NettyUtils;

/**
 * 
 * @author sangupta
 *
 */
@Sharable
public class SpdyStaticFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private final File documentRoot;
	
	private final SwiftServer swiftServer;
	
	private final boolean checkServerName;

	public SpdyStaticFileServerHandler(SwiftServer swiftServer) {
		this.swiftServer = swiftServer;
		this.documentRoot = this.swiftServer.getDocumentRoot();
		this.checkServerName = this.swiftServer.getServerName() != null;
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext context, final FullHttpRequest request) throws Exception {
		if(!request.getDecoderResult().isSuccess()) {
			NettyUtils.sendError(context, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		
		// check for server name
		if(this.checkServerName) {
			String host = request.headers().get(HttpHeaders.Names.HOST);
			if(!host.startsWith(this.swiftServer.getServerName())) {
				NettyUtils.sendError(context, HttpResponseStatus.BAD_REQUEST);
				return;
			}
		}
		
		// check method
		if(request.getMethod() != HttpMethod.GET) {
			NettyUtils.sendError(context, HttpResponseStatus.METHOD_NOT_ALLOWED);
			return;
		}
		
		// check for SPDY support
		final boolean spdyRequest = request.headers().contains(NettyUtils.SPDY_STREAM_ID);
		
		// check for URI path to be proper
		final String uri = request.getUri();
		final String path = NettyUtils.sanitizeUri(uri);
		if (path == null) {
			NettyUtils.sendError(context, HttpResponseStatus.FORBIDDEN);
			return;
		}
		
		File file = new File(documentRoot, path);
		if (file.isHidden() || !file.exists()) {
			NettyUtils.sendError(context, HttpResponseStatus.NOT_FOUND);
			return;
		}
		
		if (file.isDirectory()) {
			if (uri.endsWith("/")) {
				NettyUtils.sendListing(context, file, request.headers().get(NettyUtils.SPDY_STREAM_ID));
				return;
			}
			
			// redirect to the listing page
			NettyUtils.sendRedirect(context, uri + '/');
			return;
		}
		
		if (!file.isFile()) {
			NettyUtils.sendError(context, HttpResponseStatus.FORBIDDEN);
			return;
		}
		
		// Cache Validation
		String ifModifiedSince = request.headers().get(HttpHeaders.Names.IF_MODIFIED_SINCE);
		if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
			Date ifModifiedSinceDate = NettyUtils.parseDateHeader(ifModifiedSince);
			
			if(ifModifiedSinceDate != null) {
				// Only compare up to the second because the datetime format we send to the client
				// does not have milliseconds
				long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
				long fileLastModifiedSeconds = file.lastModified() / 1000;
				
				if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
					NettyUtils.sendNotModified(context);
					return;
				}
			}
		}
		
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
		} catch(FileNotFoundException ignore) {
			NettyUtils.sendError(context, HttpResponseStatus.NOT_FOUND);
			return;
		}
		
		final long fileLength = file.length();
		
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		HttpHeaders.setContentLength(response, fileLength);
		NettyUtils.setContentTypeHeader(response, file);
		NettyUtils.setDateAndCacheHeaders(response, file, 3600); // cache for an hour
		
		// check for keep alive
		if (HttpHeaders.isKeepAlive(request)) {
			response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		} else {
			context.write(response).addListener(ChannelFutureListener.CLOSE);
		}
		
		// Write the initial line and the header.
		context.write(response);
		
		// Write the content.
		ChannelFuture sendFileFuture;
		if (context.pipeline().get(SslHandler.class) == null) {
			sendFileFuture = context.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), context.newProgressivePromise());
		} else {
			sendFileFuture = context.write(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)), context.newProgressivePromise());
		}
		
//		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
//
//			@Override
//			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
//				if (total < 0) { // total unknown
//					System.err.println(future.channel() + " Transfer progress: " + progress);
//				} else {
//					System.err.println(future.channel() + " Transfer progress: " + progress + " / " + total);
//				}
//			}
//
//			@Override
//			public void operationComplete(ChannelProgressiveFuture future) {
//				System.err.println(future.channel() + " Transfer complete.");
//			}
//			
//		});

		// Write the end marker
		ChannelFuture lastContentFuture = context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

		// Decide whether to close the connection or not.
		if (!HttpHeaders.isKeepAlive(request)) {
			// Close the connection when the whole content is written out.
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}


}
