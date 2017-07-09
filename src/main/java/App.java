import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by umit on 08/07/2017.
 */
public class App {
    private final static String JEDIS_SERVER = "127.0.0.1";
    private ArrayList<String> messageContainer = new ArrayList<String>();
    private CountDownLatch messageReceivedLatch = new CountDownLatch(1);
    private CountDownLatch publishLatch = new CountDownLatch(1);
    private JedisPubSub jedisPubSub ;
    public static void main(String[] args) throws InterruptedException {
        new App().run();
    }
    private void run() throws InterruptedException {
        jedisPubSub = new JedisPubSub() {
            public void onMessage(String channel, String message) {
                super.onMessage(channel, message);
                messageContainer.add(message);
                log("Message received...");
                messageReceivedLatch.countDown();
            }

            public void onSubscribe(String channel, int subscribedChannels) {
                super.onSubscribe(channel, subscribedChannels);
                log("onSubscribe...");
            }

            public void onUnsubscribe(String channel, int subscribedChannels) {
                super.onUnsubscribe(channel, subscribedChannels);
                log("onUnsubscribe...");
            }
        };

        setupPublisher();
        setupSubscriber();

        Thread.sleep(2000L);
        publishLatch.countDown();
        messageReceivedLatch.await();
        Thread.sleep(1000L);
        log("Got message: %s ",messageContainer.iterator().next());
        jedisPubSub.unsubscribe();
    }

    private void setupPublisher(){
        new Thread(() ->
        {
            try {
                log("Publisher Connecting...");
                Jedis jedis = new Jedis(JEDIS_SERVER);
                log("Waiting to publish...");
                publishLatch.await();
                log("Ready to publish, waiting one sec...");
                //Thread.sleep(1000);
                log("Publishing...");
                jedis.publish("test","This is a message");
                log("published,closing publishing connection...");
                jedis.quit();
                log("publishing connection closed...");
            }catch(Exception e){
                log(">>> OH NOES Pub, " + e.getMessage() );
            }
        },"publisherThread"
        ).start();
    }

    private void setupSubscriber(){

        new Thread(() ->
        {
            try {
                log("Subscriber Connecting...");
                Jedis jedis = new Jedis(JEDIS_SERVER);
                log("subscribing...");
                jedis.subscribe(jedisPubSub, "test");
                log("subscribe returned, closing down...");
                jedis.quit();
            }catch (Exception e){
                log(">>> OH NOES Sub, " + e.getMessage() );
            }

        },"subscriberThread").start();
    }

    private static long startMillis = System.currentTimeMillis();

    private void log(String str,Object... args){
        long millisSinceStart = System.currentTimeMillis() - startMillis;
        System.out.printf("%20s %6d %s\n",Thread.currentThread().getName(),millisSinceStart,String.format(str,args));
    }
}
