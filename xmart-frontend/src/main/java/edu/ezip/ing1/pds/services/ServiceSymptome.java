package edu.ezip.ing1.pds.services;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.ezip.commons.LoggingUtils;
import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.DeleteSymptomeClientRequest;
import edu.ezip.ing1.pds.requests.DiagnostiquerClientRequest;
import edu.ezip.ing1.pds.requests.InsertSymptomeClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllSymptomesClientRequest;
import edu.ezip.ing1.pds.requests.UpdateSymptomeClientRequest;

public class ServiceSymptome {

    private final static String LoggingLabel = "FrontEnd - ServiceSymptome";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    final String insertRequestOrder = "INSERT_SYMPTOMES";
    final String selectRequestOrder = "SELECT_ALL_SYMPTOMES";
    final String deleteRequestOrder = "DELETE_SYMPTOMES";
    final String updateRequestOrder = "UPDATE_SYMPTOMES";
    

    private final NetworkConfig networkConfig;

    public ServiceSymptome(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void insertSymptome(Symptomes symptome) throws InterruptedException, IOException {
        processSymptome(symptome, insertRequestOrder);
    }

    public void deleteSymptome(Symptomes symptome) throws InterruptedException, IOException {
        processSymptome(symptome, deleteRequestOrder);
    }

    public void updateSymptome(Symptomes symptome, String nouveauNom) throws InterruptedException, IOException {
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(updateRequestOrder);

        String jsonifiedUpdate = objectMapper.writeValueAsString(new String[]{symptome.getNom(), nouveauNom});
        request.setRequestContent(jsonifiedUpdate);

        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);

        final UpdateSymptomeClientRequest symptomeRequest = new UpdateSymptomeClientRequest(
                networkConfig, 0, request, symptome, requestBytes);

        symptomeRequests.push(symptomeRequest);

        while (!symptomeRequests.isEmpty()) {
            final ClientRequest processedRequest = symptomeRequests.pop();
            processedRequest.join();
            logger.debug("Thread {} terminé : Symptôme {} modifié en {}",
                    processedRequest.getThreadName(),
                    symptome.getNom(), nouveauNom);
        }
    }

    private void processSymptome(Symptomes symptome, String requestOrder) throws InterruptedException, IOException {
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();

        final ObjectMapper objectMapper = new ObjectMapper();
        final String jsonifiedSymptome = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(symptome);
        logger.trace("Symptome en JSON : {}", jsonifiedSymptome);

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(requestOrder);
        request.setRequestContent(jsonifiedSymptome);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        ClientRequest symptomeRequest = null;

        if (requestOrder.equals(insertRequestOrder)) {
            symptomeRequest = new InsertSymptomeClientRequest(networkConfig, 0, request, symptome, requestBytes);
        } else if (requestOrder.equals(deleteRequestOrder)) {
            symptomeRequest = new DeleteSymptomeClientRequest(networkConfig, 0, request, symptome, requestBytes);
        }

        if (symptomeRequest != null) {
            symptomeRequests.push(symptomeRequest);

            while (!symptomeRequests.isEmpty()) {
                final ClientRequest processedRequest = symptomeRequests.pop();
                processedRequest.join();
                logger.debug("Thread {} terminé : Symptôme {} --> {}",
                        processedRequest.getThreadName(),
                        symptome.getNom(), processedRequest.getResult());
            }
        } else {
            logger.error("Requête invalide pour le symptôme.");
        }
    }

    public List<Symptomes> selectSymptomes() throws InterruptedException, IOException {
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(selectRequestOrder);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);

        final SelectAllSymptomesClientRequest symptomeRequest = new SelectAllSymptomesClientRequest(
                networkConfig, 0, request, null, requestBytes);
        symptomeRequests.push(symptomeRequest);

        if (!symptomeRequests.isEmpty()) {
            final ClientRequest joinedSymptomeRequest = symptomeRequests.pop();
            joinedSymptomeRequest.join();
            logger.debug("Thread {} terminé.", joinedSymptomeRequest.getThreadName());
            return (List<Symptomes>) joinedSymptomeRequest.getResult();
        } else {
            logger.error("Aucun symptôme trouvé !");
            return null;
        }
    }

    public List<String> diagnostiquer(int idPatient) throws InterruptedException, IOException {
    final Deque<ClientRequest> diagnostiqueRequests = new ArrayDeque<>();
    final ObjectMapper objectMapper = new ObjectMapper();

    final String requestId = UUID.randomUUID().toString();
    final Request request = new Request();
    request.setRequestId(requestId);
    request.setRequestOrder("DIAGNOSTIC_PATIENT");

    // Convertir l'ID du patient en JSON et l'envoyer dans la requête
    String jsonifiedPatientId = objectMapper.writeValueAsString(idPatient);
    request.setRequestContent(jsonifiedPatientId);

    objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
    LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);

    // Création de la requête de diagnostic
    final DiagnostiquerClientRequest diagnostiqueRequest = new DiagnostiquerClientRequest(
            networkConfig, 0, request, idPatient, requestBytes);

    diagnostiqueRequests.push(diagnostiqueRequest);

    if (!diagnostiqueRequests.isEmpty()) {
        final ClientRequest joinedRequest = diagnostiqueRequests.pop();
        joinedRequest.join();
        logger.debug("Thread {} terminé.", joinedRequest.getThreadName());

        return (List<String>) joinedRequest.getResult();
    } else {
        logger.error("Aucune maladie diagnostiquée !");
        return new ArrayList<>();
    }
}

}
