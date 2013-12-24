/**
 * 
 */
package stc.skymobi.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stc.skymobi.bean.LogRequest;
import stc.skymobi.bean.LogResponse;
import stc.skymobi.fsm.FSMContext;
import stc.skymobi.fsm.FiniteStateMachine;
import stc.skymobi.fsm.tmpl.annotation.OnAccept;
import stc.skymobi.fsm.tmpl.annotation.StateTemplate;
import stc.skymobi.service.IFirstService;
import stc.skymobi.service.ISecondService;

/**
 * @author jason.zheng
 *
 */
public class LogBiz {
	
    private static final Logger logger = 
    	LoggerFactory.getLogger(LogBiz.class);
    
    private String event = "";
    
    private IFirstService firstService = null;
    private ISecondService secondService = null;
    
    public void setFirstService(IFirstService firstService) {
		this.firstService = firstService;
	}

	public void setSecondService(ISecondService secondService) {
		this.secondService = secondService;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	@StateTemplate(init = true)
	class RecvReq {
		@OnAccept
		Class<?> accept(FiniteStateMachine fsm, FSMContext ctx, LogRequest req) {
			logger.info("received request {}", req);
			for (int i=0; i<5; i++) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				logger.info("sleep 1 second!");
			}
			logger.info("sent event {}", event);
			
			LogResponse resp = new LogResponse();
			resp.setIdentification(req.getIdentification());
			resp.skyId = req.skyId;
			
			logger.info("FirstService:"+firstService.methodFirst());
			logger.info("SecondService:"+secondService.methodSecond());
			
			ctx.fireEventWithTimeout(null, 0, event, resp);
	
			return null;
		}
	}
}

