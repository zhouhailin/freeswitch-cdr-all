package link.thingscloud.freeswitch.cdr.common;

import link.thingscloud.freeswitch.cdr.domain.Cdr;
import link.thingscloud.freeswitch.cdr.util.CdrDecodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>CdrHelper class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
@Slf4j
public class CdrHelper {

    private static final String SIP_H_USER_TO_USER = "sip_h_User-to-User";


    private CdrHelper() {
    }

    /**
     * 获取 AVAYA UUI 随路数据
     * <p>
     * 04FA08006424BC5CF89291C808
     * 00FA080064006F5CFF915E
     * <p>
     * &lt;sip_h_User-to-User&gt;00FA080064006F5CFF915E;encoding=hex&lt;/sip_h_User-to-User&gt;
     * &lt;sip_h_User-to-User&gt;00FA080064007C5CFF92E2%3Bencoding%3Dhex&lt;/sip_h_User-to-User&gt;
     *
     * @param cdr a {@link link.thingscloud.freeswitch.cdr.domain.Cdr} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getUserData(Cdr cdr) {
        String userToUser = cdr.getVariables().getVariableTable().get(SIP_H_USER_TO_USER);
        if (StringUtils.isBlank(userToUser)) {
            return StringUtils.EMPTY;
        }
        String userDataHex = StringUtils.substring(StringUtils.replace(userToUser, "%3Bencoding%3Dhex", StringUtils.EMPTY),
                "04FA08006424BC5CF89291C808".length());
        try {
            return new String(Hex.decodeHex(userDataHex));
        } catch (DecoderException e) {
            log.warn("getUserData decodeHex userDataHex : [{}]", userDataHex, e);
            return StringUtils.EMPTY;
        }
    }


    /**
     * <p>decode.</p>
     *
     * @param str a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String decode(String str) {
        return CdrDecodeUtil.decode(str);
    }
}
