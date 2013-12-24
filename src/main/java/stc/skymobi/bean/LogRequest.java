package stc.skymobi.bean;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


public class LogRequest extends AbstractCommonBean{
	
    public int skyId;

    public String toString() {
        return  ToStringBuilder.reflectionToString(this, 
                            ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
