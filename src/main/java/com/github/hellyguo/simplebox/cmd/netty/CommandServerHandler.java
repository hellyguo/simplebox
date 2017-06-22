/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.github.hellyguo.simplebox.cmd.netty;

import com.github.hellyguo.simplebox.app.AppHolder;
import com.github.hellyguo.simplebox.app.AppStatus;
import com.github.hellyguo.simplebox.cmd.SupportCommand;
import com.github.hellyguo.simplebox.monitor.Monitor;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Arrays;
import java.util.Date;

/**
 * Handles a server-side channel.
 */
@Sharable
class CommandServerHandler extends SimpleChannelInboundHandler<String> {
    private static final String HELP_MSG;
    private static final String WELCOME_MSG;

    private AppHolder holder = AppHolder.getHolder();
    private Monitor monitor = Monitor.getMonitor();

    static {
        StringBuilder builder = new StringBuilder();
        builder.append("support command:\r\n");
        Arrays.stream(SupportCommand.values()).forEach(cmd -> {
            String cmdName = cmd.cmd();
            String splitSpace;
            if (cmdName.length() >= 7) {
                splitSpace = ":\t";
            } else {
                splitSpace = ":\t\t";
            }
            builder.append('\t').append(cmdName).append(splitSpace).append(cmd.desc()).append("\r\n");
        });
        builder.append("\r\n")
                .append("please notice: when connection by telnet, the input area for command is not support backspace.\r\n")
                .append("\r\n")
                .append("#> ");
        HELP_MSG = builder.toString();
        builder.setLength(0);
        builder.append("======simplebox console======\r\n")
                .append("current time:").append(new Date()).append("\r\n")
                .append(HELP_MSG);
        WELCOME_MSG = builder.toString();
    }

    CommandServerHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        ctx.write(WELCOME_MSG);
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        // Generate and write a response.
        ResponseInfo info;
        if (request.isEmpty()) {
            info = new ResponseInfo("\r\n#> ", false, false);
        } else {
            try {
                SupportCommand cmd = SupportCommand.valueOf(request.toUpperCase());
                info = performCommand(cmd);
            } catch (Exception e) {
                info = new ResponseInfo("unknown command\r\n#> ", false, false);
            }
        }

        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        ChannelFuture future = ctx.write(info.response);

        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'shutdown'.
        if (info.closeConn) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
        if (info.shutdown) {
            ctx.channel().parent().close();
        }
    }

    private ResponseInfo performCommand(SupportCommand cmd) {
        switch (cmd) {
            case NOW: {
                return new ResponseInfo("current time:" + new Date() + "\r\n#> ", false, false);
            }
            case HELP: {
                return new ResponseInfo(HELP_MSG, false, false);
            }
            case DISCONN: {
                return new ResponseInfo("the connection will be end soon.\r\n", true, false);
            }
            case STATUS: {
                return new ResponseInfo("app's current status is " + holder.getStatus() + ".\r\n#> ", false, false);
            }
            case START: {
                if (AppStatus.STOPPED.equals(holder.getStatus())) {
                    holder.boot();
                    return new ResponseInfo("app was started.\r\n#> ", false, false);
                } else {
                    return new ResponseInfo("app was already started\r\n#> ", false, false);
                }
            }
            case RESTART: {
                if (AppStatus.RUNNING.equals(holder.getStatus())) {
                    holder.shutdown();
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        //
                    }
                    holder.boot();
                } else {
                    holder.boot();
                }
                return new ResponseInfo("app was restarted.\r\n#> ", false, false);
            }
            case STOP: {
                if (AppStatus.RUNNING.equals(holder.getStatus())) {
                    holder.shutdown();
                    return new ResponseInfo("app was stopped.\r\n#> ", false, false);
                } else {
                    return new ResponseInfo("app was already stopped\r\n#> ", false, false);
                }
            }
            case SHUTDOWN: {
                return new ResponseInfo("app and simplebox will shutdown soon.\r\n", true, true);
            }
            case THREAD: {
                StringBuilder builder = Monitor.getMonitor().getThreadsInfo();
                builder.append("#> ");
                return new ResponseInfo(builder.toString(), false, false);
            }
            case MEM: {
                StringBuilder builder = Monitor.getMonitor().getMemInfo();
                builder.append("#> ");
                return new ResponseInfo(builder.toString(), false, false);
            }
            default: {
                return new ResponseInfo("unknown command\r\n#> ", false, false);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private class ResponseInfo {
        String response;
        boolean closeConn;
        boolean shutdown;

        ResponseInfo(String response, boolean closeConn, boolean shutdown) {
            this.response = response;
            this.closeConn = closeConn;
            this.shutdown = shutdown;
        }
    }
}
