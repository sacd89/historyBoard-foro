package com.example.hector.history;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class RabbitController {
    Thread subscribeThread;
    Thread publishThread;

    ConnectionFactory factory = new ConnectionFactory();
    RabbitController() {
        String uri = "amqp://dtnjyulk:yJ27rs4eQUW1ek6URIP35iR6MDgpgSvT@wasp.rmq.cloudamqp.com/dtnjyulk";
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }
    private BlockingDeque<String> queue = new LinkedBlockingDeque<String>();
    void publishMessage(String message) {
        try {
            queue.putLast(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void publishToAMQP() {
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel ch = connection.createChannel();
                        ch.confirmSelect();

                        while (true) {
                            String message = queue.takeFirst();
                            try{
                                ch.basicPublish("amq.fanout", "chat", null, message.getBytes());
                                Log.d("", "[s] " + message);
                                ch.waitForConfirmsOrDie();
                            } catch (Exception e){
                                Log.d("","[f] " + message);
                                queue.putFirst(message);
                                throw e;
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        Log.d("", "Connection broken: " + e.getClass().getName());
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e1) {
                            break;
                        }
                    }
                }
            }
        });
        publishThread.start();
    }
    void subscribe(final Handler handler)
    {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel();
                        channel.basicQos(1);
                        AMQP.Queue.DeclareOk q = channel.queueDeclare();
                        channel.queueBind(q.getQueue(), "amq.fanout", "chat");
                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume(q.getQueue(), true, consumer);

                        while (true) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            String message = new String(delivery.getBody());
                            Log.d("","[r] " + message);
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("msg", message);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e1) {
                        Log.d("", "Connection broken: " + e1.getClass().getName());
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        });
        subscribeThread.start();
    }

}
