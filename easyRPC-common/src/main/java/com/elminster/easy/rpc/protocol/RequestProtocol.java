package com.elminster.easy.rpc.protocol;

import com.elminster.easy.rpc.data.Request;

/**
 * The Rpc request protocol:
 * <i> | size | magic number | version | request id | async flag | service name | method name | args | </i>
 * 
 * @author jinggu
 * @version 1.0
 */
public interface RequestProtocol extends Protocol<Request> {
}
