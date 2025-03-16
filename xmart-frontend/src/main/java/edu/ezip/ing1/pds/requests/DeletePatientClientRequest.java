
package edu.ezip.ing1.pds.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ezip.ing1.pds.business.dto.Patient;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

import java.io.IOException;
import java.util.Map;

public class DeletePatientClientRequest extends ClientRequest<Patient, String> {

    public DeletePatientClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Patient patient, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, patient, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> responseMap = mapper.readValue(body, Map.class);
        return responseMap.get("message"); // Assurez-vous que le serveur renvoie un message dans la réponse
    }
}