package com.linkedin.pinot.core.realtime.impl.kafka;

import java.util.List;
import java.util.Map;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import com.linkedin.pinot.core.data.GenericRow;
import com.linkedin.pinot.core.realtime.StreamProvider;
import com.linkedin.pinot.core.realtime.StreamProviderConfig;


/**
 *
 */
public class KafkaHighLevelConsumerStreamProvider implements StreamProvider {
  private KafkaHighLevelStreamProviderConfig streamProviderConfig;
  private KafkaMessageDecoder decoder;

  private ConsumerConfig kafkaConsumerConfig;
  private ConsumerConnector consumer;
  private KafkaStream<byte[], byte[]> kafkaStreams;
  private ConsumerIterator<byte[], byte[]> kafkaIterator;

  @Override
  public void init(StreamProviderConfig streamProviderConfig) throws Exception {
    this.streamProviderConfig = (KafkaHighLevelStreamProviderConfig) streamProviderConfig;
    this.kafkaConsumerConfig = this.streamProviderConfig.getKafkaConsumerConfig();
    this.decoder = this.streamProviderConfig.getDecoder();
  }

  @Override
  public void start() throws Exception {
    consumer = kafka.consumer.Consumer.createJavaConsumerConnector(this.kafkaConsumerConfig);
    Map<String, Integer> topicsMap = streamProviderConfig.getTopicMap(1);

    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicsMap);
    this.kafkaStreams = consumerMap.get(streamProviderConfig.getTopicName()).get(0);
    kafkaIterator = kafkaStreams.iterator();
  }

  @Override
  public void setOffset(long offset) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GenericRow next() {
    if (kafkaIterator.hasNext()) {
      return decoder.decode(kafkaIterator.next().message());
    }
    return null;
  }

  @Override
  public GenericRow next(long offset) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long currentOffset() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void commit() {
    consumer.commitOffsets();
  }

  @Override
  public void commit(long offset) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void shutdown() throws Exception {
    if (consumer != null) {
      consumer.shutdown();
    }
  }

}