package com.iflytek.kafka.fileSink;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.kafka.bean.JsonBean;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @author cyh
 * @Date 16:06 2019/7/30
 * @description
 * @since 2.0
 */
public class LogContentToKafka extends AbstractSink implements Configurable {
    private KafkaProducer<String, String> producer;
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(LogContentToKafka.class);
    private static final String TOPIC = "kafka.topic";
    private static final String SERVERS = "kafka.bootstrap.servers";
    private String topic;
    private String servers;
    @Override
    public Status process() throws EventDeliveryException {
        Status status = null;
        Channel ch = getChannel();
        Transaction txn = ch.getTransaction();
        txn.begin();
        try {

            Event event = ch.take();
            if(event == null) {
                status = Status.BACKOFF;
            }
            byte[] byte_message = event.getBody();
            logger.info("当前日志内容"+new String(byte_message));
            //生产者
            String result = buildLogContent(new String(byte_message));
            logger.info("json result is "+ result);
            ProducerRecord<String, String> record =new ProducerRecord<>(topic, result);
            System.out.println("record is:"+record.toString());
            Future<RecordMetadata> future = producer.send(record);
            System.out.println("return future is:"+future.get().toString());
            txn.commit();
            status = Status.READY;
        } catch (Throwable t) {
            txn.rollback();
            status = Status.BACKOFF;
            if (t instanceof Error) {
                throw (Error)t;
            }
        }finally {
            txn.close();
        }
        return status;
    }

    @Override
    public void configure(Context context) {
        topic = context.getString(TOPIC);
        servers = context.getString(SERVERS);
        Properties originalProps = new Properties();
        originalProps.put("bootstrap.servers", servers);
        originalProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        originalProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        originalProps.put("custom.encoding", "UTF-8");
        producer = new KafkaProducer<String,String>(originalProps);
    }

    private static String buildLogContent(String body){
        List list = new ArrayList();
        JsonBean bean = new JsonBean();
        String res[] = body.split("@");
        bean.setHostIp(res[1]);
        bean.setLevel(res[2]);
        bean.setAppName(res[4]);
        bean.setThreadName(res[5]);
        bean.setLogCaller(res[6]);
        bean.setAppLog(res[7]);
        bean.setLogTime(formatLogTime(res[0]));
        bean.setSystemCode(res[3]);
        list.add(bean);
        return  JSONObject.toJSONString(list);

    }

    private static Long formatLogTime(String date){
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(new SimpleDateFormat("yyyyMMddHHmmssSSS").parse(date));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return (c.getTimeInMillis());
    }
}
