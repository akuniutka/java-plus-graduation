package ru.practicum.ewm.stats.serialization;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Serializer;
import ru.practicum.ewm.stats.exception.KafkaSerializationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private BinaryEncoder encoder;

    @Override
    public byte[] serialize(final String topic, final SpecificRecordBase datum) {
        if (datum == null) {
            return null;
        }
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            encoder = encoderFactory.binaryEncoder(out, encoder);
            final DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(datum.getSchema());
            writer.write(datum, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new KafkaSerializationException("Message serialization error", e);
        }

    }
}
