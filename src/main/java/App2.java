import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by umit on 09/07/2017.
 */
public class App2 {
    private static Logger logger = LoggerFactory.getLogger(App2.class);
    private static final String CHANNEL_NAME = "test";
    public static void main(String[] args) {

        logger.info("Starting App2...");
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestWhileIdle(false);
        poolConfig.setMaxTotal(30);
        poolConfig.setMinIdle(3);
        poolConfig.setMaxIdle(30);

        final JedisPool jedisPool = new JedisPool(poolConfig,"localhost",6379,0);
        final Jedis subscriberJedis = jedisPool.getResource();
        final Subscriber subscriber = new Subscriber();

        new Thread(() ->
        {
            logger.info("Subscribing to channel test. This thread will be blocked...");
            subscriberJedis.subscribe(subscriber,CHANNEL_NAME);
            logger.info("Subscription ended...");
        }).start();

        final Jedis publisherJedis = jedisPool.getResource();

        new Publisher(publisherJedis,CHANNEL_NAME).start();
        logger.info("Will unsubscribe...");
        subscriber.unsubscribe();
        jedisPool.returnResource(subscriberJedis);
        jedisPool.returnResource(publisherJedis);
    }
}
