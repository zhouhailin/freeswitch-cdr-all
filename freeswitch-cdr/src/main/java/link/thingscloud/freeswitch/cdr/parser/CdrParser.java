package link.thingscloud.freeswitch.cdr.parser;

import com.alibaba.fastjson.JSON;
import link.thingscloud.freeswitch.cdr.domain.*;
import link.thingscloud.freeswitch.cdr.exception.ParserException;
import link.thingscloud.freeswitch.cdr.util.CdrDecodeUtil;
import link.thingscloud.freeswitch.cdr.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * <p>CdrParser class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
@Slf4j
public class CdrParser {

    private static final String CORE_UUID = "core-uuid";
    private static final String SWITCHNAME = "switchname";

    private static final String CHANNEL_DATA = "channel_data";
    private static final String STATE = "state";
    private static final String DIRECTION = "direction";
    private static final String STATE_NUMBER = "state_number";
    private static final String FLAGS = "flags";
    private static final String CAPS = "caps";

    private static final String VARIABLES = "variables";

    private static final String APP_LOG = "app_log";

    private static final String CALLFLOW = "callflow";


    private static ThreadLocal<String> local = new ThreadLocal<>();

    private static boolean isTraceEnabled = log.isTraceEnabled();

    private CdrParser() {
    }

    /**
     * xml_cdr.conf.xml
     * <p>
     * &lt;param name=&quot;encode&quot; value=&quot;true&quot;/&gt;
     * <p>
     * <p>
     * cdr=&lt;?xml&nbsp;version=&quot;1.0&quot;?&gt;
     * <p>
     * uuid=a_12d714e6-3c49-463a-8965-755b8f598032&amp;cdr=&lt;?xml version=&quot;1.0&quot;?&gt;
     *
     * @param reqText req xml content
     * @return cdr
     * @throws link.thingscloud.freeswitch.cdr.exception.ParserException if any.
     */
    public static Cdr decodeThenParse(String reqText) throws ParserException {
        String decodeText = CdrDecodeUtil.decode(reqText);
        String decodeXml = StringUtils.substringAfter(decodeText, "cdr=");
        return parse(decodeXml);
    }

    /**
     * <p>parse.</p>
     *
     * @param decodeXml a {@link java.lang.String} object.
     * @return a {@link link.thingscloud.freeswitch.cdr.domain.Cdr} object.
     * @throws link.thingscloud.freeswitch.cdr.exception.ParserException if any.
     */
    public static Cdr parse(String decodeXml) throws ParserException {
        if (StringUtils.isBlank(decodeXml)) {
            throw new ParserException("cdr parse xml failed, strXml is blank.");
        }
        local.set(decodeXml);
        try {
            Document document = DocumentHelper.parseText(decodeXml);
            Element rootElement = document.getRootElement();

            Cdr cdr = new Cdr();
            assignCdrElement(cdr, rootElement);

            if (isTraceEnabled) {
                log.trace("cdr parse : [{}]", JSON.toJSONString(cdr, true));
            }
            return cdr;
        } catch (Exception e) {
            throw new ParserException("cdr parse xml failed.", e);
        } finally {
            local.remove();
        }
    }


    private static void assignCdrElement(Cdr cdr, Element rootElement) {
        // cdr 节点属性赋值
        attributes(rootElement, (name, value) -> {
            if (CORE_UUID.equals(name)) {
                cdr.setCoreUuid(value);
            }else if (SWITCHNAME.equals(name)) {
                cdr.setSwitchname(value);
            } else {
                log.warn("assignCdrElement found other attribute name : [{}], value : [{}], xml : [{}]", name, value, local.get());
            }
        });

        // cdr 节点下所有元素
        elements(rootElement, (name, element) -> {
            switch (name) {
                case CHANNEL_DATA:
                    ChannelData channelData = new ChannelData();
                    cdr.setChannelData(channelData);
                    assignChannelDataElement(channelData, element);
                    break;
                case VARIABLES:
                    Variables variables = new Variables();
                    cdr.setVariables(variables);
                    assignVariablesElement(variables, element);
                    break;
                case APP_LOG:
                    AppLog appLog = new AppLog();
                    cdr.setAppLog(appLog);
                    assignAppLogElement(appLog, element);
                    break;
                case CALLFLOW:
                    Callflow callflow = new Callflow();
                    cdr.addCallflow(callflow);
                    assignCallflowElement(callflow, element);
                    break;
                default:
                    log.warn("assignCdrElement found other element name : [{}], xml : [{}]", name, local.get());
                    break;
            }
        });
    }


    private static void assignChannelDataElement(ChannelData channelData, Element rootElement) {
        // channel_data 节点属性赋值
        elements(rootElement, (name, element) -> {
            String value = element.getTextTrim();
            switch (name) {
                case STATE:
                    channelData.setState(value);
                    break;
                case DIRECTION:
                    channelData.setDirection(value);
                    break;
                case STATE_NUMBER:
                    channelData.setStateNumber(value);
                    break;
                case FLAGS:
                    channelData.setFlags(value);
                    break;
                case CAPS:
                    channelData.setCaps(value);
                    break;
                default:
                    log.warn("assignChannelDataElement found other element name : [{}], xml : [{}]", name, local.get());
                    break;
            }
        });
    }

    private static void assignVariablesElement(Variables variables, Element rootElement) {
        elements(rootElement, (name, element) -> variables.putVariable(name, element.getTextTrim()));
    }

    private static final String APPLICATION = "application";

    private static void assignAppLogElement(AppLog appLog, Element rootElement) {
        List<Application> applications = new ArrayList<>();
        appLog.setApplications(applications);
        elements(rootElement, (name, element) -> {
            if (APPLICATION.equals(name)) {
                assignApplicationElement(applications, element);
            } else {
                log.warn("assignAppLogElement found other element name : [{}], xml : [{}]", name, local.get());
            }
        });
    }

    private static final String APP_NAME = "app_name";
    private static final String APP_DATA = "app_data";
    private static final String APP_STAMP = "app_stamp";


    private static void assignApplicationElement(final List<Application> applications, final Element rootElement) {
        Application application = new Application();
        attributes(rootElement, (name, value) -> {
            switch (name) {
                case APP_NAME:
                    application.setAppName(value);
                    break;
                case APP_DATA:
                    application.setAppData(value);
                    break;
                case APP_STAMP:
                    application.setAppStamp(NumberUtil.toLong(value));
                    break;
                default:
                    log.warn("assignApplicationElement found other attribute name : [{}], value : [{}], xml : [{}]", name, value, local.get());
                    break;
            }
        });
        applications.add(application);
    }

    private static final String DIALPLAN = "dialplan";
    private static final String UNIQUE_ID = "unique-id";
    private static final String CLONE_OF = "clone-of";
    private static final String PROFILE_INDEX = "profile_index";
    private static final String EXTENSION = "extension";
    private static final String CALLER_PROFILE = "caller_profile";
    private static final String TIMES = "times";

    private static void assignCallflowElement(Callflow callflow, Element rootElement) {
        // 属性
        attributes(rootElement, (name, value) -> {
            switch (name) {
                case DIALPLAN:
                    callflow.setDialplan(value);
                    break;
                case UNIQUE_ID:
                    callflow.setUniqueId(value);
                    break;
                case CLONE_OF:
                    callflow.setCloneOf(value);
                    break;
                case PROFILE_INDEX:
                    callflow.setProfileIndex(value);
                    break;
                default:
                    log.warn("assignCallflowElement found other attribute name : [{}], value : [{}], xml : [{}]", name, value, local.get());
                    break;
            }
        });

        // 子元素
        elements(rootElement, (name, element) -> {
            switch (name) {
                case EXTENSION:
                    Extension extension = new Extension();
                    callflow.setExtension(extension);
                    assignExtensionElement(extension, element);
                    break;
                case CALLER_PROFILE:
                    CallerProfile callerProfile = new CallerProfile();
                    callflow.setCallerProfile(callerProfile);
                    assignCallerProfileElement(callerProfile, element);
                    break;
                case TIMES:
                    Times times = new Times();
                    callflow.setTimes(times);
                    assignTimesElement(times, element);
                    break;
                default:
                    log.warn("assignCallflowChildElement found other element name : [{}]], xml : [{}]", name, local.get());
                    break;
            }

        });
    }

    private static final String NAME = "name";
    private static final String NUMBER = "number";


    private static void assignExtensionElement(Extension extension, Element rootElement) {
        // 属性
        attributes(rootElement, (name, value) -> {
            switch (name) {
                case NAME:
                    extension.setName(value);
                    break;
                case NUMBER:
                    extension.setNumber(value);
                    break;
                default:
                    log.warn("assignExtensionElement found other attribute name : [{}], value : [{}], xml : [{}]", name, value, local.get());
                    break;
            }
        });

        List<Application> applications = new ArrayList<>();
        extension.setApplications(applications);
        elements(rootElement, (name, element) -> {
            if (APPLICATION.equals(name)) {
                assignApplicationElement(applications, element);
            } else {
                log.warn("assignExtensionElement found other element name : [{}], xml : [{}]", name, local.get());
            }
        });
    }

    private static final String USERNAME = "username";
    private static final String CALLER_ID_NAME = "caller_id_name";
    private static final String CALLER_ID_NUMBER = "caller_id_number";
    private static final String CALLEE_ID_NAME = "callee_id_name";
    private static final String CALLEE_ID_NUMBER = "callee_id_number";
    private static final String ANI = "ani";
    private static final String ANIII = "aniii";
    private static final String NETWORK_ADDR = "network_addr";
    private static final String RDNIS = "rdnis";
    private static final String DESTINATION_NUMBER = "destination_number";
    private static final String UUID = "uuid";
    private static final String SOURCE = "source";
    private static final String TRANSFER_SOURCE = "transfer_source";
    private static final String CONTEXT = "context";
    private static final String CHAN_NAME = "chan_name";
    private static final String ORIGINATION = "origination";
    private static final String ORIGINATEE = "originatee";

    private static void assignCallerProfileElement(CallerProfile callerProfile, Element rootElement) {

        elements(rootElement, (name, element) -> {
            String value = element.getTextTrim();
            switch (name) {
                case USERNAME:
                    callerProfile.setUsername(value);
                    break;
                case DIALPLAN:
                    callerProfile.setDialplan(value);
                    break;
                case CALLER_ID_NAME:
                    callerProfile.setCallerIdName(value);
                    break;
                case CALLER_ID_NUMBER:
                    callerProfile.setCallerIdNumber(value);
                    break;
                case CALLEE_ID_NAME:
                    callerProfile.setCalleeIdName(value);
                    break;
                case CALLEE_ID_NUMBER:
                    callerProfile.setCalleeIdNumber(value);
                    break;
                case ANI:
                    callerProfile.setAni(value);
                    break;
                case ANIII:
                    callerProfile.setAniii(value);
                    break;
                case NETWORK_ADDR:
                    callerProfile.setNetworkAddr(value);
                    break;
                case RDNIS:
                    callerProfile.setRdnis(value);
                    break;
                case DESTINATION_NUMBER:
                    callerProfile.setDestinationNumber(value);
                    break;
                case UUID:
                    callerProfile.setUuid(value);
                    break;
                case SOURCE:
                    callerProfile.setSource(value);
                    break;
                case TRANSFER_SOURCE:
                    callerProfile.setTransferSource(value);
                    break;
                case CONTEXT:
                    callerProfile.setContext(value);
                    break;
                case CHAN_NAME:
                    callerProfile.setChanName(value);
                    break;
                case ORIGINATION:
                    Origination origination = new Origination();
                    callerProfile.setOrigination(origination);
                    assignOriginationElement(origination, element);
                    break;
                case ORIGINATEE:
                    Originatee originatee = new Originatee();
                    callerProfile.setOriginatee(originatee);
                    assignOriginateeElement(originatee, element);
                    break;
                default:
                    log.warn("assignCallerProfileElement found other element name : [{}], xml : [{}]", name, local.get());
                    break;
            }
        });

    }

    private static final String ORIGINATION_CALLER_PROFILE = "origination_caller_profile";

    private static void assignOriginationElement(Origination origination, Element rootElement) {
        elements(rootElement, (name, element) -> {
            if (ORIGINATION_CALLER_PROFILE.equals(name)) {
                OriginationCallerProfile originationCallerProfile = new OriginationCallerProfile();
                origination.setOriginationCallerProfile(originationCallerProfile);
                assignOriginationCallerProfileElement(originationCallerProfile, element);
            } else {
                log.warn("assignOriginationElement found other element name : [{}], xml : [{}]", name, local.get());
            }
        });

    }

    private static void assignOriginationCallerProfileElement(OriginationCallerProfile originationCallerProfile, Element rootElement) {
        elements(rootElement, (name, element) -> {
            String value = element.getTextTrim();
            switch (name) {
                case USERNAME:
                    originationCallerProfile.setUsername(value);
                    break;
                case DIALPLAN:
                    originationCallerProfile.setDialplan(value);
                    break;
                case CALLER_ID_NAME:
                    originationCallerProfile.setCallerIdName(value);
                    break;
                case CALLER_ID_NUMBER:
                    originationCallerProfile.setCallerIdNumber(value);
                    break;
                case CALLEE_ID_NAME:
                    originationCallerProfile.setCalleeIdName(value);
                    break;
                case CALLEE_ID_NUMBER:
                    originationCallerProfile.setCalleeIdNumber(value);
                    break;
                case ANI:
                    originationCallerProfile.setAni(value);
                    break;
                case ANIII:
                    originationCallerProfile.setAniii(value);
                    break;
                case NETWORK_ADDR:
                    originationCallerProfile.setNetworkAddr(value);
                    break;
                case RDNIS:
                    originationCallerProfile.setRdnis(value);
                    break;
                case DESTINATION_NUMBER:
                    originationCallerProfile.setDestinationNumber(value);
                    break;
                case UUID:
                    originationCallerProfile.setUuid(value);
                    break;
                case SOURCE:
                    originationCallerProfile.setSource(value);
                    break;
                case CONTEXT:
                    originationCallerProfile.setContext(value);
                    break;
                case CHAN_NAME:
                    originationCallerProfile.setChanName(value);
                    break;
                default:
                    log.warn("assignOriginationCallerProfileElement found other element name : [{}], xml : [{}]", name, local.get());
                    break;

            }
        });
    }

    private static final String ORIGINATEE_CALLER_PROFILE = "originatee_caller_profile";

    private static void assignOriginateeElement(Originatee originatee, Element rootElement) {
        elements(rootElement, (name, element) -> {
            if (ORIGINATEE_CALLER_PROFILE.equals(name)) {
                OriginateeCallerProfile originateeCallerProfile = new OriginateeCallerProfile();
                originatee.setOriginateeCallerProfile(originateeCallerProfile);
                assignOriginateeCallerProfileElement(originateeCallerProfile, element);
            } else {
                log.warn("assignOriginationElement found other element name : [{}], xml : [{}]", name, local.get());
            }
        });

    }

    private static void assignOriginateeCallerProfileElement(OriginateeCallerProfile originateeCallerProfile, Element rootElement) {
        elements(rootElement, (name, element) -> {
            String value = element.getTextTrim();
            switch (name) {
                case USERNAME:
                    originateeCallerProfile.setUsername(value);
                    break;
                case DIALPLAN:
                    originateeCallerProfile.setDialplan(value);
                    break;
                case CALLER_ID_NAME:
                    originateeCallerProfile.setCallerIdName(value);
                    break;
                case CALLER_ID_NUMBER:
                    originateeCallerProfile.setCallerIdNumber(value);
                    break;
                case CALLEE_ID_NAME:
                    originateeCallerProfile.setCalleeIdName(value);
                    break;
                case CALLEE_ID_NUMBER:
                    originateeCallerProfile.setCalleeIdNumber(value);
                    break;
                case ANI:
                    originateeCallerProfile.setAni(value);
                    break;
                case ANIII:
                    originateeCallerProfile.setAniii(value);
                    break;
                case NETWORK_ADDR:
                    originateeCallerProfile.setNetworkAddr(value);
                    break;
                case RDNIS:
                    originateeCallerProfile.setRdnis(value);
                    break;
                case DESTINATION_NUMBER:
                    originateeCallerProfile.setDestinationNumber(value);
                    break;
                case UUID:
                    originateeCallerProfile.setUuid(value);
                    break;
                case SOURCE:
                    originateeCallerProfile.setSource(value);
                    break;
                case CONTEXT:
                    originateeCallerProfile.setContext(value);
                    break;
                case CHAN_NAME:
                    originateeCallerProfile.setChanName(value);
                    break;
                default:
                    log.warn("assignOriginateeCallerProfileElement found other element name : [{}], xml : [{}]", name, local.get());
                    break;

            }
        });
    }

    private static final String CREATED_TIME = "created_time";
    private static final String PROFILE_CREATED_TIME = "profile_created_time";
    private static final String PROGRESS_TIME = "progress_time";
    private static final String PROGRESS_MEDIA_TIME = "progress_media_time";
    private static final String ANSWERED_TIME = "answered_time";
    private static final String BRIDGED_TIME = "bridged_time";
    private static final String LAST_HOLD_TIME = "last_hold_time";
    private static final String HOLD_ACCUM_TIME = "hold_accum_time";
    private static final String HANGUP_TIME = "hangup_time";
    private static final String RESURRECT_TIME = "resurrect_time";
    private static final String TRANSFER_TIME = "transfer_time";

    private static void assignTimesElement(Times times, Element rootElement) {
        elements(rootElement, (name, element) -> {
            String value0 = element.getTextTrim();
            Long value = NumberUtil.toLong(value0);
            switch (name) {
                case CREATED_TIME:
                    times.setCreatedTime(value);
                    break;
                case PROFILE_CREATED_TIME:
                    times.setProfileCreatedTime(value);
                    break;
                case PROGRESS_TIME:
                    times.setProgressTime(value);
                    break;
                case PROGRESS_MEDIA_TIME:
                    times.setProgressMediaTime(value);
                    break;
                case ANSWERED_TIME:
                    times.setAnsweredTime(value);
                    break;
                case BRIDGED_TIME:
                    times.setBridgedTime(value);
                    break;
                case LAST_HOLD_TIME:
                    times.setLastHoldTime(value);
                    break;
                case HOLD_ACCUM_TIME:
                    times.setHoldAccumTime(value);
                    break;
                case HANGUP_TIME:
                    times.setHangupTime(value);
                    break;
                case RESURRECT_TIME:
                    times.setResurrectTime(value);
                    break;
                case TRANSFER_TIME:
                    times.setTransferTime(value);
                    break;
                default:
                    log.warn("assignTimesElement found other element name : [{}], xml : [{}]", name, local.get());
                    break;
            }
        });

    }

    private static void attributes(Element element, BiConsumer<String, String> consumer) {
        List<Attribute> attributes = element.attributes();
        for (Attribute attribute : attributes) {
            attribute.getName();
            consumer.accept(attribute.getName(), attribute.getValue());
        }
    }

    private static void elements(Element rootElement, BiConsumer<String, Element> consumer) {
        List<Element> elements = rootElement.elements();
        for (Element element : elements) {
            consumer.accept(element.getName(), element);
        }
    }
}
