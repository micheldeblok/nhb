package nl.mdb.nhb.nhclient;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

class MessageConverter extends GsonHttpMessageConverter {
	
	public MessageConverter() {
        List<MediaType> types = Arrays.asList(
                new MediaType("text", "html", DEFAULT_CHARSET),
                new MediaType("application", "json", DEFAULT_CHARSET),
                new MediaType("application", "*+json", DEFAULT_CHARSET)
        );
        super.setSupportedMediaTypes(types);
    }
}
