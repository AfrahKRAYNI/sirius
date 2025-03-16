package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Medicament;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class SelectAllMedicamentsClientRequest extends ClientRequest<Object, Medicament> {

    public SelectAllMedicamentsClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Object info, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public Medicament readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Medicament medicaments = mapper.readValue(body, Medicament.class);
        return medicaments;
    }
}
