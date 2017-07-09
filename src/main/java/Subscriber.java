import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by umit on 09/07/2017.
 */
public class Subscriber extends JedisPubSub {
    private static Logger logger = LoggerFactory.getLogger(Subscriber.class);
    @Override
    public void onMessage(String channel,String message){
        logger.info("Message received from channel {} : {}",channel,message);
    }


}
