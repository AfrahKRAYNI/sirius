package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Ordonnances;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class SelectAllOrdonnancesClientRequest extends ClientRequest<Object, Ordonnances> {

    public SelectAllOrdonnancesClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Object info, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public Ordonnances readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Ordonnances ordonnances = mapper.readValue(body, Ordonnances.class);
        return ordonnances;
    }
}
