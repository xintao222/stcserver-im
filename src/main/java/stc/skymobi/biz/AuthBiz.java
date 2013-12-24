/**
 * 
 */
package stc.skymobi.biz;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stc.skymobi.bean.*;
import stc.skymobi.fsm.FiniteStateMachine;
import stc.skymobi.fsm.event.UUIDTimeoutEvent;
import stc.skymobi.fsm.tmpl.annotation.OnAccept;
import stc.skymobi.fsm.tmpl.annotation.OnEnter;
import stc.skymobi.fsm.tmpl.annotation.OnTimeout;
import stc.skymobi.fsm.tmpl.annotation.StateTemplate;
import stc.skymobi.netty.handler.codec.http.response.SendbackResponseHelper;
import stc.skymobi.netty.transport.TransportUtils;
import stc.skymobi.netty.transport.Sender;


/**
 * @author jason.zheng
 *
 */
public class AuthBiz {
	
    private static final Logger logger = 
    	LoggerFactory.getLogger(AuthBiz.class);
    
    private	SendbackResponseHelper	sendbackHelper;
    
    private int logTimeout = 0;
	
	public void setLogTimeout(int logTimeout) {
		this.logTimeout = logTimeout;
	}
	
	public void setSendbackHelper(SendbackResponseHelper sendbackHelper) {
		this.sendbackHelper = sendbackHelper;
	}	
    
	@StateTemplate(init = true)
	class RecvReq {
		@OnAccept
		Class<?> accept(FiniteStateMachine fsm, AuthCtx ctx, AuthRequest req) {

			logger.info("received request {}", req);
			AuthResponse resp = new AuthResponse();
			resp.setIdentification(req.getIdentification());
			resp.setResult(new Result(404));
			ctx.setRequest(req);
			ctx.setResponse(resp);
			if (req.getSkyId() < 20000000) {
				return ToLog.class;
			} else {
				return ToService.class;
			}
		}
	}
	
	@StateTemplate
	class ToLog {
		
		@OnEnter
		boolean enter(FiniteStateMachine fsm, AuthCtx ctx) {
			
			LogRequest req = new LogRequest();
			req.skyId = ctx.getRequest().getSkyId();
			req.setIdentification((UUID)ctx.getKey());
			
			ctx.fireEventWithTimeout(new UUIDTimeoutEvent(req.getIdentification()), logTimeout, "com.skymobi.event.app.log", req);
			return	true;
		}
		
		@OnAccept
		Class<?> accept(FiniteStateMachine fsm, AuthCtx ctx, LogResponse resp) {

			logger.info("recv log resp {}", resp);
			ctx.getResponse().setResult(new Result(200));
			return SendResp.class;
		}
		
		@OnTimeout
		Class<?> onTimeout(FiniteStateMachine fsm, AuthCtx ctx) {
			ctx.getResponse().setResult(new Result(500));
	        return SendResp.class;
		}		
	}
	
	@StateTemplate
	class ToService {
		
		@OnEnter
		boolean enter(FiniteStateMachine fsm, AuthCtx ctx) {
			
			ctx.fireEventWithTimeout(new UUIDTimeoutEvent(ctx.getRequest().getIdentification()), logTimeout, "event.service.req", ctx.getRequest());
			return	true;
		}
		
		@OnAccept
		Class<?> accept(FiniteStateMachine fsm, AuthCtx ctx, AuthResponse resp) {
			
			logger.info("recv service resp {}", resp);
			ctx.getResponse().setResult(new Result(201));
			return SendResp.class;
		}
		
		@OnTimeout
		Class<?> onTimeout(FiniteStateMachine fsm, AuthCtx ctx) {
			
			ctx.getResponse().setResult(new Result(501));
	        return SendResp.class;
		}		
	}
	
	@StateTemplate
	class SendResp {
				
		@OnEnter
		boolean enter(FiniteStateMachine fsm, AuthCtx ctx) {		
			
			  // send response
            Sender sender = TransportUtils.getSenderOf(ctx.getRequest());
            if (logger.isDebugEnabled()) {
                logger.debug("SendClientRespState={}", ctx.getResponse());
            }

            if (sender != null) {
            	ctx.getResponse().getResult().setErrorMessage("TCP response");
                sender.send(ctx.getResponse());
            } else {
            	ctx.getResponse().getResult().setErrorMessage("HTTP response");
            	sendbackHelper.makeAndSendback(ctx.getResponse(), ctx.getRequest());
                logger.error("getSender error!");
            }
                       
			return	false;
		}
	}
}

