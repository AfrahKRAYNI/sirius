package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Prescription;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class InsertMedicamentClientRequest extends ClientRequest<Prescription, String> {

    public InsertMedicamentClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Prescription prescription, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, prescription, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, Integer> prescriptionIdMap = mapper.readValue(body, Map.class);
        final String result = prescriptionIdMap.get("prescription_id").toString();
        return result;
    }
}
