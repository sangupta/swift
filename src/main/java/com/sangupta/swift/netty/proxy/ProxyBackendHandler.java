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

package com.sangupta.swift.netty.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
 * @author sangupta
 *
 */
public class ProxyBackendHandler extends ChannelInboundHandlerAdapter {

    private final Channel inboundChannel;

    public ProxyBackendHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) {
        channelHandlerContext.read();
        channelHandlerContext.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(final ChannelHandlerContext channelHandlerContext, Object message) {
        inboundChannel.writeAndFlush(message).addListener(new ChannelFutureListener() {
        	
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    channelHandlerContext.channel().read();
                } else {
                    future.channel().close();
                }
            }
            
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        ProxyFrontendHandler.closeOnFlush(this.inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        cause.printStackTrace();
        ProxyFrontendHandler.closeOnFlush(channelHandlerContext.channel());
    }
}