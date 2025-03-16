package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class DiagnostiquerClientRequest extends ClientRequest<Integer, List<String>> {

    public DiagnostiquerClientRequest(NetworkConfig networkConfig, int attempt, Request request, Integer idPatient, byte[] requestBytes) throws IOException {
        super(networkConfig, attempt, request, idPatient, requestBytes);
    }

    @Override
    public List<String> readResult(final String responseBody) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseBody, new TypeReference<List<String>>() {});
    }
}
