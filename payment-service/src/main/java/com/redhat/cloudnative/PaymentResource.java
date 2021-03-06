package com.redhat.cloudnative;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletionStage;

import javax.enterprise.event.Observes;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.kafka.KafkaMessage;
import io.vertx.core.json.JsonObject;

@Path("/")
public class PaymentResource {

    @ConfigProperty(name = "mp.messaging.outgoing.payments.bootstrap.servers")
    public String bootstrapServers;

    @ConfigProperty(name = "mp.messaging.outgoing.payments.topic")
    public String paymentsTopic;

    @ConfigProperty(name = "mp.messaging.outgoing.payments.value.serializer")
    public String paymentsTopicValueSerializer;

    @ConfigProperty(name = "mp.messaging.outgoing.payments.key.serializer")
    public String paymentsTopicKeySerializer;

    private Producer<String, String> producer;

    public static final Logger log = LoggerFactory.getLogger(PaymentResource.class);

    @POST
    // @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public void handleCloudEvent(String cloudEventJson) {
        log.info("In handleCloudEvent with event:" + cloudEventJson);

        String orderId = "unknown";
        String paymentId = "" + ((int) (Math.floor(Math.random() * 100000)));

        // There is a potential race condition, particularly if we're not waiting for 
        // 5 seconds to model payment processing delay.  Thus wait some random number of microseconds
        // until the producer is done being initialized from the init method below
        while (producer == null)
        {
            log.info("Producer is null, waiting for it to be set");
            try{
                Thread.sleep((int) ((Math.random()*100)%1000));
            } 
            catch (Exception ex) 
            {
                log.info("could not sleep!  Error: " + ex.getMessage());
            }
        }

        try {
            log.info("received event: " + cloudEventJson);
            JsonObject event = new JsonObject(cloudEventJson);
            orderId = event.getString("orderId");
            String total = event.getString("total");
            JsonObject ccDetails = event.getJsonObject("creditCard");
            String name = event.getString("name");

            // fake processing time
            // Thread.sleep(5000);
            if (!ccDetails.getString("number").startsWith("4")) {
                fail(orderId, paymentId, "Invalid Credit Card: " + ccDetails.getString("number"));
            }
            pass(orderId, paymentId,
                    "Payment of " + total + " succeeded for " + name + " CC details: " + ccDetails.toString());
        } catch (Exception ex) {
            fail(orderId, paymentId, "Unknown error: " + ex.getMessage() + " for payment: " + cloudEventJson);
        }
    }

    private void pass(String orderId, String paymentId, String remarks) {

        JsonObject payload = new JsonObject();
        payload.put("orderId", orderId);
        payload.put("paymentId", paymentId);
        payload.put("remarks", remarks);
        payload.put("status", "COMPLETED (Quarkus Build)");
        log.info("Sending payment success: " + payload.toString());
        producer.send(new ProducerRecord<String, String>(paymentsTopic, payload.toString()));
    }

    private void fail(String orderId, String paymentId, String remarks) {
        JsonObject payload = new JsonObject();
        payload.put("orderId", orderId);
        payload.put("paymentId", paymentId);
        payload.put("remarks", remarks);
        payload.put("status", "FAILED");
        log.info("Sending payment failure: " + payload.toString());
        producer.send(new ProducerRecord<String, String>(paymentsTopic, payload.toString()));
    }

    public void init(@Observes StartupEvent ev) {
        Properties props = new Properties();

        log.info("In init..");
        props.put("bootstrap.servers", bootstrapServers);
        props.put("value.serializer", paymentsTopicValueSerializer);
        props.put("key.serializer", paymentsTopicKeySerializer);
        producer = new KafkaProducer<String, String>(props);
    }

}