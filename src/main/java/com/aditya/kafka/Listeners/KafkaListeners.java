package com.aditya.kafka.Listeners;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

    @KafkaListener(
            topics="adityaTopic",
            groupId="groupId"
    )
    void listener(String data){
        System.out.println("Listener received data "+data+" 🎉");
    }
}
