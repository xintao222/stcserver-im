package stc.skymobi.bean;

import stc.skymobi.bean.AbstractCommonBean;
import stc.skymobi.bean.tlv.TLVSignal;
import stc.skymobi.bean.tlv.annotation.TLVAttribute;
import stc.skymobi.bean.xip.core.XipRequest;

/**
 * @author zhenyu.zheng
 * 
 */
@TLVAttribute(tag = 2110002)
public class AuthResponse extends AbstractCommonBean implements TLVSignal, XipRequest{
	@TLVAttribute(tag=11020001, description="")
	private Result result = new Result(200);

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}
}
