package com.nikonhacker;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import java.io.*;
import java.nio.charset.Charset;

public class XStreamUtils {
    public static void save(Object object, OutputStream outputStream) {
        save(object, outputStream, getBaseXStream());
    }

    public static void save(Object object, OutputStream outputStream, XStream xStream) {
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
            xStream.toXML(object, writer);
        }
        finally {
            try {
                if (writer!= null) writer.close();
                if (outputStream!=null) outputStream.close();
            } catch (IOException e) {
                //noop
            }
        }
    }

    public static Object load(InputStream inputStream) {
        return load(inputStream, getBaseXStream());
    }

    public static Object load(InputStream inputStream, XStream xStream) {
        Reader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        return xStream.fromXML(reader);
    }

    public static XStream getBaseXStream() {
        return new XStream(new StaxDriver()) {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        //noinspection SimplifiableIfStatement
                        if (definedIn == Object.class) {
                            return false;
                        }
                        return super.shouldSerializeMember(definedIn, fieldName);
                    }
                };
            }
        };
    }

}
