package io.etip.sdk.hello;

public class JsonCodec {
    private JsonEncoder encoder;
    private JsonDecoder decoder;

    public JsonEncoder encoder() {
        return encoder;
    }


    public JsonDecoder decoder() {
        return decoder;
    }


    public static Builder newBuilder() {
        return new Builder();
    }

    static class Builder {
        private JsonEncoder encoder;
        private JsonDecoder decoder;

        public Builder encoder(JsonEncoder encoder) {
            this.encoder = encoder;
            return this;
        }
        public Builder decoder(JsonDecoder decoder) {
            this.decoder = decoder;
            return this;
        }
        public JsonCodec build() {
            var codec = new JsonCodec();
            codec.encoder = this.encoder;
            codec.decoder = this.decoder;
            return codec;
        }
    }
}
