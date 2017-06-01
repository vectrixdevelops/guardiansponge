/*
 * MIT License
 *
 * Copyright (c) 2017 Connor Hartley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.connorhartley.guardian.storage.configuration;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.util.Tuple;

import java.lang.reflect.ParameterizedType;

public class TupleSerializer implements TypeSerializer<Tuple<?, ?>> {

    @Override
    @SuppressWarnings("unchecked")
    public Tuple<?, ?> deserialize(TypeToken<?> typeToken, ConfigurationNode configurationNode) throws ObjectMappingException {
        if (!(typeToken.getType() instanceof ParameterizedType)) {
            throw new ObjectMappingException("Raw types are not supported for tuples.");
        }

        TypeToken<?> first = typeToken.resolveType(Tuple.class.getTypeParameters()[0]);
        TypeToken<?> second = typeToken.resolveType(Tuple.class.getTypeParameters()[1]);
        TypeSerializer firstSerial = configurationNode.getOptions().getSerializers().get(first);
        TypeSerializer secondSerial = configurationNode.getOptions().getSerializers().get(second);

        if (firstSerial == null) {
            throw new ObjectMappingException("No type serializer available for type " + first);
        }

        if (secondSerial == null) {
            throw new ObjectMappingException("No type serializer available for type " + second);
        }

        String tupleCollection = configurationNode.getString();

        if (tupleCollection == null) {
            throw new ObjectMappingException("No value present in node " + configurationNode);
        }

        String[] splitTupleCollection = StringUtils.split(tupleCollection, "+");

        if (splitTupleCollection.length != 2) {
            throw new ObjectMappingException("Too many or too little tuple items were present " + StringUtils.join(splitTupleCollection, ", "));
        }

        return Tuple.of(firstSerial.deserialize(first, SimpleConfigurationNode.root().setValue(splitTupleCollection[0])),
                secondSerial.deserialize(second, SimpleConfigurationNode.root().setValue(splitTupleCollection[1])));
    }

    @Override
    public void serialize(TypeToken<?> typeToken, Tuple<?, ?> tuple, ConfigurationNode configurationNode) throws ObjectMappingException {
        if (!(typeToken.getType() instanceof ParameterizedType)) {
            throw new ObjectMappingException("Raw types are not supported for tuples.");
        }

        String[] tupleCollection = new String[] { tuple.getFirst().toString(), tuple.getSecond().toString() };

        if (tupleCollection[0].contains("+") || tupleCollection[1].contains("+")) {
            throw new ObjectMappingException("A plus character was found in the serialized tuple values " + StringUtils.join(tupleCollection, ", "));
        }

        configurationNode.setValue(StringUtils.join(tupleCollection[0], "+", tupleCollection[1]));
    }
}
