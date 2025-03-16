package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class InsertOrdonnanceClientRequest extends ClientRequest<Ordonnance, String> {

    public InsertOrdonnanceClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Ordonnance ordonnance, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, ordonnance, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, Integer> ordonnanceIdMap = mapper.readValue(body, Map.class);
        final String result = ordonnanceIdMap.get("ordonnance_id").toString();
        return result;
    }
}
