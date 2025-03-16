package edu.ezip.ing1.pds.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

import java.io.IOException;

public class SelectAllPatientsClientRequest extends ClientRequest<Object, Patients> {

    public SelectAllPatientsClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Object info, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public Patients readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Patients Patients = mapper.readValue(body, Patients.class);
        return Patients;
    }
}
