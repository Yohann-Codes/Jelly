package service;

import org.apache.log4j.Logger;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

/**
 * 业务分发器.
 *
 * @author Yohann.
 */
public class Dispatcher {
    private static final Logger logger = Logger.getLogger(Dispatcher.class);

    public static void dispatch(MessageHolder messageHolder) {
        if (messageHolder.getSign() != ProtocolHeader.REQUEST) {
            // 请求错误
            return;
        }
        switch (messageHolder.getType()) {
        }
    }
}
