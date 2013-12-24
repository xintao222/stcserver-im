/**
 * 
 */
package stc.skymobi.biz;

import stc.skymobi.bean.AuthRequest;
import stc.skymobi.bean.AuthResponse;
import stc.skymobi.fsm.FiniteStateMachine;
import stc.skymobi.fsm.core.SmartContextImpl;

/**
 * @author jason.zheng
 *
 */
public class AuthCtx extends SmartContextImpl {

	private	AuthRequest		request;
	private AuthResponse	response;
	
	public AuthCtx(FiniteStateMachine fsm) {
		super(fsm);
	}

	public AuthRequest getRequest() {
		return request;
	}

	public void setRequest(AuthRequest request) {
		this.request = request;
	}

	public AuthResponse getResponse() {
		return response;
	}

	public void setResponse(AuthResponse response) {
		this.response = response;
	}
}
