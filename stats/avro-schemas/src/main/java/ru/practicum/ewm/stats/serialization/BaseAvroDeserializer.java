package ru.practicum.ewm.stats.serialization;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.exception.KafkaSerializationException;

import java.io.IOException;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    protected final DecoderFactory decoderFactory;
    protected final DatumReader<T> reader;
    protected BinaryDecoder decoder;

    public BaseAvroDeserializer(final Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(final DecoderFactory decoderFactory, final Schema schema) {
        this.decoderFactory = decoderFactory;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(final String topic, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        decoder = decoderFactory.binaryDecoder(bytes, decoder);
        try {
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new KafkaSerializationException("Deserialization error: topic = %s".formatted(topic), e);
        }
    }
}
