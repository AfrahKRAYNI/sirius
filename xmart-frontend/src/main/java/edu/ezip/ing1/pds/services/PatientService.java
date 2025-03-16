package edu.ezip.ing1.pds.services;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.ezip.commons.LoggingUtils;
import edu.ezip.ing1.pds.business.dto.Patient;
import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.DeletePatientClientRequest;
import edu.ezip.ing1.pds.requests.InsertPatientClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllPatientsClientRequest; // Import de la requête pour la suppression

public class PatientService {

    private final static String LoggingLabel = "FrontEnd - PatientService";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    final String insertRequestOrder = "INSERT_PATIENT";
    final String selectRequestOrder = "SELECT_ALL_PATIENTS";
    final String deleteRequestOrder = "DELETE_PATIENT"; // Ajout de l'ordre de requête pour la suppression

    private final NetworkConfig networkConfig;

    public PatientService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void InsertPatient(Patient patient) throws InterruptedException, IOException {
        processPatient(patient, insertRequestOrder);
    }

    public void DeletePatient(Patient patient) throws InterruptedException, IOException { // Nouvelle méthode pour
                                                                                          // supprimer un patient
        processPatient(patient, deleteRequestOrder);
    }

    private void processPatient(Patient patient, String requestOrder) throws InterruptedException, IOException {
        final Deque<ClientRequest> patientRequests = new ArrayDeque<>();

        final ObjectMapper objectMapper = new ObjectMapper();
        final String jsonifiedPatient = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(patient);
        logger.trace("Patient with its JSON face : {}", jsonifiedPatient);

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(requestOrder);
        request.setRequestContent(jsonifiedPatient);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        ClientRequest patientRequest = null;

        // Sélectionner le bon type de requête selon l'opération
        if (requestOrder.equals(insertRequestOrder)) {
            patientRequest = new InsertPatientClientRequest(networkConfig, 0, request, patient, requestBytes);
        } else if (requestOrder.equals(deleteRequestOrder)) {
            patientRequest = new DeletePatientClientRequest(networkConfig, 0, request, patient, requestBytes); // Requête
                                                                                                               // pour
                                                                                                               // supprimer
        }

        patientRequests.push(patientRequest);

        while (!patientRequests.isEmpty()) {
            final ClientRequest processedRequest = patientRequests.pop();
            processedRequest.join();
            final Patient processedPatient = (Patient) processedRequest.getInfo();
            logger.debug("Thread {} complete : {} {} --> {}",
                    processedRequest.getThreadName(),
                    processedPatient.getNom(), processedPatient.getPrenom(), processedPatient.getAge(),
                    processedRequest.getResult());
        }
    }

    public Patients selectPatients() throws InterruptedException, IOException {
        final Deque<ClientRequest> patientRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(selectRequestOrder);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);

        final SelectAllPatientsClientRequest patientRequest = new SelectAllPatientsClientRequest(
                networkConfig, 0, request, null, requestBytes);
        patientRequests.push(patientRequest);

        if (!patientRequests.isEmpty()) {
            final ClientRequest joinedPatientRequest = patientRequests.pop();
            joinedPatientRequest.join();
            logger.debug("Thread {} complete.", joinedPatientRequest.getThreadName());
            return (Patients) joinedPatientRequest.getResult();
        } else {
            logger.error("No patients found");
            return null;
        }
    }
}
