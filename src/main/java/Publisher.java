import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by umit on 09/07/2017.
 */
public class Publisher {
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);

    private final Jedis publisherJedis;
    private final String channel;

    public Publisher(Jedis publisherJedis,String channel){
        this.publisherJedis = publisherJedis;
        this.channel = channel;
    }
    public void start(){
        logger.info("type your message");
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true){
                String line = reader.readLine();
                if(line.equals("quit")){
                    break;
                }else{
                    publisherJedis.publish(channel,line);
                }
            }
        }catch (Exception e){
            logger.error("Exception ",e);
        }

    }
}
