package cc.blynk.integration;

import cc.blynk.integration.model.tcp.ClientPair;
import cc.blynk.server.Holder;
import cc.blynk.server.core.BlockingIOProcessor;
import cc.blynk.server.core.SlackWrapper;
import cc.blynk.server.notifications.mail.MailWrapper;
import cc.blynk.server.notifications.push.GCMWrapper;
import cc.blynk.server.notifications.sms.SMSWrapper;
import cc.blynk.server.notifications.twitter.TwitterWrapper;
import cc.blynk.server.servers.BaseServer;
import cc.blynk.server.servers.application.AppAndHttpsServer;
import cc.blynk.server.servers.hardware.HardwareAndHttpAPIServer;
import cc.blynk.utils.properties.ServerProperties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

/**
 * use when you need only 1 server instance per test class and not per test method
 */
public abstract class StaticServerBase extends CounterBase {

    protected static ServerProperties properties;
    protected static BaseServer appServer;
    protected static BaseServer hardwareServer;
    protected static Holder holder;
    protected ClientPair clientPair;

    @BeforeClass
    public static void init() throws Exception {
        properties = new ServerProperties(Collections.emptyMap());
        properties.setProperty("data.folder", TestUtil.getDataFolder());
        BlockingIOProcessor blockingIOProcessor = new BlockingIOProcessor(
                    properties.getIntProperty("blocking.processor.thread.pool.limit", 5),
                    properties.getIntProperty("notifications.queue.limit", 2000)
            );
        holder = new Holder(properties, mock(TwitterWrapper.class),
                mock(MailWrapper.class), mock(GCMWrapper.class),
                mock(SMSWrapper.class), mock(SlackWrapper.class),
                blockingIOProcessor,
                "no-db.properties");
        hardwareServer = new HardwareAndHttpAPIServer(holder).start();
        appServer = new AppAndHttpsServer(holder).start();
    }

    @AfterClass
    public static void shutdown() {
        appServer.close();
        hardwareServer.close();
        holder.close();
    }

    @After
    public void closeClients() {
        this.clientPair.stop();
    }

    @Before
    public void resetBeforeTest() throws Exception {
        this.clientPair = initClientPair();
        reset(holder.mailWrapper);
        reset(holder.twitterWrapper);
        reset(holder.gcmWrapper);
        reset(holder.smsWrapper);
        reset(holder.slackWrapper);
    }

    public ClientPair initAppAndHardPair() throws Exception {
        return TestUtil.initAppAndHardPair("localhost",
                properties.getHttpsPort(), properties.getHttpPort(),
                getUserName(), "1", changeProfileTo(), properties, 10000);
    }

    protected String changeProfileTo() {
        return "user_profile_json.txt";
    }

    protected ClientPair initClientPair() throws Exception {
        return initAppAndHardPair();
    }

}
