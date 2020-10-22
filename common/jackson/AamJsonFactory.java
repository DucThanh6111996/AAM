package com.viettel.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.viettel.controller.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Created by quan on 4/9/2017.
 */
public class AamJsonFactory extends JsonFactory {
    @Override
    protected JsonParser _createParser(byte[] data, int offset, int len, IOContext ctxt) throws IOException {
        return new DowncasingParser(super._createParser(data, offset, len, ctxt));
    }

    @Override
    protected JsonParser _createParser(InputStream in, IOContext ctxt) throws IOException {
        return new DowncasingParser(super._createParser(in, ctxt));
    }

    @Override
    protected JsonParser _createParser(Reader r, IOContext ctxt) throws IOException {
        return new DowncasingParser(super._createParser(r, ctxt));
    }

    @Override
    protected JsonParser _createParser(char[] data, int offset, int len, IOContext ctxt, boolean recyclable)
            throws IOException {
        return new DowncasingParser(super._createParser(data, offset, len, ctxt, recyclable));
    }

    private static final class DowncasingParser extends JsonParserDelegate {
        private DowncasingParser(JsonParser d) {
            super(d);
        }

        @Override
        public String getCurrentName() throws IOException {
            if (hasTokenId(JsonTokenId.ID_FIELD_NAME)) {
                return TextUtils.toJavaFieldName(delegate.getCurrentName());
            }
            return delegate.getCurrentName();
        }

        @Override
        public String getText() throws IOException {
            if (hasTokenId(JsonTokenId.ID_FIELD_NAME)) {
                return TextUtils.toJavaFieldName(delegate.getText());
            }
            return delegate.getText();
        }
    }
}


