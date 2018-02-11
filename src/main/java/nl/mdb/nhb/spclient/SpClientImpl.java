package nl.mdb.nhb.spclient;

import lombok.extern.slf4j.Slf4j;
import nl.mdb.nhb.NhbConfiguration;
import nl.mdb.nhb.nhclient.io.ApiVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
class SpClientImpl implements SpClient {

    @Autowired
    private NhbConfiguration config;

    private RestTemplate restTemplate;

    public SpClientImpl(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public SpStatistics getStatistics() {
        ResponseEntity<SpStatistics> response = restTemplate.getForEntity(config.getSpUrl(), SpStatistics.class);
        if (response.getStatusCodeValue() != 200) {
            log.warn("SP returned unexpected status code: {}", response.getStatusCodeValue());
        }
        return response.getBody();
    }
}
